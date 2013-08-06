package net.sf.taverna.t2.activities.interaction.jetty;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.abdera.Abdera;
import org.apache.abdera.i18n.templates.Template;
import org.apache.abdera.i18n.text.Normalizer;
import org.apache.abdera.i18n.text.Sanitizer;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;
import org.apache.abdera.protocol.server.ProviderHelper;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;
import org.apache.abdera.protocol.server.Target;
import org.apache.abdera.protocol.server.provider.managed.FeedConfiguration;
import org.apache.abdera.protocol.server.provider.managed.ManagedCollectionAdapter;

/**
 * Simple Filesystem Adapter that uses a local directory to store Atompub
 * collection entries. As an extension of the ManagedCollectionAdapter class,
 * the Adapter is intended to be used with implementations of the
 * ManagedProvider and are configured using /abdera/adapter/*.properties files.
 * The *.properties file MUST specify the fs.root property to specify the root
 * directory used by the Adapter.
 */
public class HackedFilesystemAdapter extends ManagedCollectionAdapter {

	private final File root;
	private final static FileSorter sorter = new FileSorter();
	private final static Template paging_template = new Template(
			"?{-join|&|count,page}");

	public HackedFilesystemAdapter(final Abdera abdera,
			final FeedConfiguration config) {
		super(abdera, config);
		this.root = this.getRoot();
	}

	private File getRoot() {
		return InteractionJetty.getFeedDirectory();
	}

	private Entry getEntry(final File entryFile) {
		if (!entryFile.exists() || !entryFile.isFile()) {
			throw new RuntimeException();
		}
		try {
			final FileInputStream fis = new FileInputStream(entryFile);
			final Document<Entry> doc = this.abdera.getParser().parse(fis);
			final Entry entry = doc.getRoot();
			return entry;
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void addPagingLinks(final RequestContext request, final Feed feed,
			final int currentpage, final int count) {
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("count", count);
		params.put("page", currentpage + 1);
		String next = paging_template.expand(params);
		next = request.getResolvedUri().resolve(next).toString();
		feed.addLink(next, "next");
		if (currentpage > 0) {
			params.put("page", currentpage - 1);
			String prev = paging_template.expand(params);
			prev = request.getResolvedUri().resolve(prev).toString();
			feed.addLink(prev, "previous");
		}
		params.put("page", 0);
		String current = paging_template.expand(params);
		current = request.getResolvedUri().resolve(current).toString();
		feed.addLink(current, "current");
	}

	private void getEntries(final RequestContext request, final Feed feed,
			final File root) {
		final File[] files = root.listFiles();
		Arrays.sort(files, sorter);
		final int length = ProviderHelper.getPageSize(request, "count", 25);
		final int offset = ProviderHelper.getOffset(request, "page", length);
		final String _page = request.getParameter("page");
		final int page = (_page != null) ? Integer.parseInt(_page) : 0;
		this.addPagingLinks(request, feed, page, length);
		if (offset > files.length) {
			return;
		}
		for (int n = offset; (n < (offset + length)) && (n < files.length); n++) {
			final File file = files[n];
			try {
				final Entry entry = this.getEntry(file);
				feed.addEntry((Entry) entry.clone());
			} catch (final Exception e) {
				// Do nothing
			}
		}
	}

	@Override
	public ResponseContext getFeed(final RequestContext request) {
		final Feed feed = this.abdera.newFeed();
		feed.setId(this.config.getServerConfiguration().getServerUri() + "/"
				+ this.config.getFeedId());
		feed.setTitle(this.config.getFeedTitle());
		feed.addAuthor(this.config.getFeedAuthor());
		feed.addLink(this.config.getFeedUri());
		feed.addLink(this.config.getFeedUri(), "self");
		feed.setUpdated(new Date());
		this.getEntries(request, feed, this.root);
		return ProviderHelper.returnBase(feed.getDocument(), 200, null);
	}

	@Override
	public ResponseContext deleteEntry(final RequestContext request) {
		final Target target = request.getTarget();
		final String key = target.getParameter("entry");
		final File file = this.getFile(key, false);
		if (file.exists()) {
			file.delete();
		}
		return ProviderHelper.nocontent();
	}

	@Override
	public ResponseContext getEntry(final RequestContext request) {
		final Target target = request.getTarget();
		final String key = target.getParameter("entry");
		final File file = this.getFile(key, false);
		final Entry entry = this.getEntry(file);
		if (entry != null) {
			return ProviderHelper.returnBase(entry.getDocument(), 200, null);
		} else {
			return ProviderHelper.notfound(request);
		}
	}

	@Override
	public ResponseContext postEntry(final RequestContext request) {
		if (request.isAtom()) {
			try {
				final Entry entry = (Entry) request.getDocument().getRoot()
						.clone();
				final String key = this.createKey(request);
				this.setEditDetail(request, entry, key);
				final File file = this.getFile(key);
				final FileOutputStream out = new FileOutputStream(file);
				entry.writeTo(out);
				final String edit = entry.getEditLinkResolvedHref().toString();
				return ProviderHelper
						.returnBase(entry.getDocument(), 201, null)
						.setLocation(edit);
			} catch (final Exception e) {
				return ProviderHelper.badrequest(request);
			}
		} else {
			return ProviderHelper.notsupported(request);
		}
	}

	private void setEditDetail(final RequestContext request, final Entry entry,
			final String key) throws IOException {
		final Target target = request.getTarget();
		final String feed = target.getParameter("feed");
		final String id = key;
		entry.setEdited(new Date());
		final Link link = entry.getEditLink();
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("feed", feed);
		params.put("entry", id);
		final String href = request.absoluteUrlFor("entry", params);
		if (link == null) {
			entry.addLink(href, "edit");
		} else {
			link.setHref(href);
		}
	}

	private File getFile(final String key) {
		return this.getFile(key, true);
	}

	private File getFile(final String key, final boolean post) {
		final File file = new File(this.root, key);
		if (post && file.exists()) {
			throw new RuntimeException("File exists");
		}
		return file;
	}

	private String createKey(final RequestContext request) throws IOException {
		String slug = request.getSlug();
		if (slug == null) {
			slug = ((Entry) request.getDocument().getRoot()).getTitle();
		}
		return Sanitizer.sanitize(slug, "", true, Normalizer.Form.D);
	}

	@Override
	public ResponseContext putEntry(final RequestContext request) {
		if (request.isAtom()) {
			try {
				final Entry entry = (Entry) request.getDocument().getRoot()
						.clone();
				final String key = request.getTarget().getParameter("entry");
				this.setEditDetail(request, entry, key);
				final File file = this.getFile(key, false);
				final FileOutputStream out = new FileOutputStream(file);
				entry.writeTo(out);
				final String edit = entry.getEditLinkResolvedHref().toString();
				return ProviderHelper
						.returnBase(entry.getDocument(), 200, null)
						.setLocation(edit);
			} catch (final Exception e) {
				return ProviderHelper.badrequest(request);
			}
		} else {
			return ProviderHelper.notsupported(request);
		}
	}

	private static class FileSorter implements Comparator<File> {
		@Override
		public int compare(final File o1, final File o2) {
			return o1.lastModified() > o2.lastModified() ? -1 : o1
					.lastModified() < o2.lastModified() ? 1 : 0;
		}
	}
}
