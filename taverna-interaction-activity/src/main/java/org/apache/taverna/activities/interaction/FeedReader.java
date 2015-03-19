/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.taverna.activities.interaction;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import org.apache.taverna.activities.interaction.preference.InteractionPreference;

import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.parser.ParseException;
import org.apache.abdera.parser.Parser;
import org.apache.abdera.parser.stax.FOMParser;
import org.apache.log4j.Logger;

public abstract class FeedReader extends Thread {

	static final Logger logger = Logger.getLogger(FeedReader.class);

	public FeedReader(final String name) {
		super(name);
		this.setDaemon(true);
	}

	protected abstract void considerEntry(Entry entry);

	@Override
	public void run() {
		try {
			final Parser parser = new FOMParser();
			Date lastCheckedDate = new Date();
			while (true) {
				try {
					sleep(5000);
				} catch (final InterruptedException e1) {
					logger.error(e1);
				}
				InputStream openStream = null;
				try {
					final Date newLastCheckedDate = new Date();
					final URL url = getInteractionPreference().getFeedUrl();
					openStream = url.openStream();
					final Document<Feed> doc = parser.parse(openStream,
							url.toString());
					final Feed feed = doc.getRoot().sortEntriesByEdited(true);

					for (final Entry entry : feed.getEntries()) {

						Date d = entry.getEdited();
						if (d == null) {
							d = entry.getUpdated();
						}
						if (d == null) {
							d = entry.getPublished();
						}
						 if (d.before(lastCheckedDate)) {
						 break;
						 }
						this.considerEntry(entry);
					}
					lastCheckedDate = newLastCheckedDate;
				} catch (final MalformedURLException e) {
					logger.error(e);
				} catch (final ParseException e) {
					logger.error(e);
				} catch (final IOException e) {
					logger.error(e);
				} finally {
					try {
						if (openStream != null) {
							openStream.close();
						}
					} catch (final IOException e) {
						logger.error(e);
					}
				}
			}
		} catch (final Exception e) {
			logger.error(e);
		}
	}

	protected abstract InteractionPreference getInteractionPreference();
}