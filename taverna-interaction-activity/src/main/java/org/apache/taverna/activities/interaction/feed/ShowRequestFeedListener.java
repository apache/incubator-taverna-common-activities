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

package org.apache.taverna.activities.interaction.feed;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.taverna.activities.interaction.FeedReader;
import org.apache.taverna.activities.interaction.preference.InteractionPreference;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Link;
import org.apache.log4j.Logger;

/**
 * @author alanrw
 * 
 */
public class ShowRequestFeedListener extends FeedReader {
	
	private static ShowRequestFeedListener instance;

	private static Logger logger = Logger
			.getLogger(ShowRequestFeedListener.class);
	
	private static final String ignore_requests_property = System.getProperty("taverna.interaction.ignore_requests");

	private static boolean operational = (ignore_requests_property == null) || !Boolean.valueOf(ignore_requests_property);

	private InteractionPreference interactionPreference;
	
	private ShowRequestFeedListener() {
		super("ShowRequestFeedListener");
	}
	
			@Override
			protected void considerEntry(final Entry entry) {
				if (!operational) {
					return;
				}
				final Link presentationLink = entry.getLink("presentation");
				if (presentationLink != null) {
					try {
						Desktop.getDesktop().browse(
								presentationLink.getHref().toURI());
					} catch (final IOException e) {
						logger.error("Cannot open presentation");
					} catch (final URISyntaxException e) {
						logger.error("Cannot open presentation");
					}
				}
			}

			@Override
			protected InteractionPreference getInteractionPreference() {
				return this.interactionPreference;
			}

			public void setInteractionPreference(InteractionPreference interactionPreference) {
				this.interactionPreference = interactionPreference;
			}

}
