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

package org.apache.taverna.activities.interaction.atom;

import javax.xml.namespace.QName;

/**
 * @author alanrw
 * 
 */
public class AtomUtils {

	private static QName inputDataQName = new QName(
			"http://ns.taverna.org.uk/2012/interaction", "input-data",
			"interaction");
	private static QName resultDataQName = new QName(
			"http://ns.taverna.org.uk/2012/interaction", "result-data",
			"interaction");
	private static QName resultStatusQName = new QName(
			"http://ns.taverna.org.uk/2012/interaction", "result-status",
			"interaction");
	private static QName idQName = new QName(
			"http://ns.taverna.org.uk/2012/interaction", "id", "interaction");
	private static QName pathIdQName = new QName(
			"http://ns.taverna.org.uk/2012/interaction", "path",
			"interaction");
	private static QName countQName = new QName(
			"http://ns.taverna.org.uk/2012/interaction", "count",
			"interaction");
	private static QName runIdQName = new QName(
			"http://ns.taverna.org.uk/2012/interaction", "run-id",
			"interaction");
	private static QName inReplyToQName = new QName(
			"http://ns.taverna.org.uk/2012/interaction", "in-reply-to",
			"interaction");
	private static QName progressQName = new QName(
			"http://ns.taverna.org.uk/2012/interaction", "progress",
			"interaction");

	public static QName getInputDataQName() {
		return inputDataQName;
	}

	public static QName getIdQName() {
		return idQName;
	}

	public static QName getInReplyToQName() {
		return inReplyToQName;
	}

	public static QName getResultDataQName() {
		return resultDataQName;
	}

	public static QName getResultStatusQName() {
		return resultStatusQName;
	}

	/**
	 * @return the runIdQName
	 */
	public static QName getRunIdQName() {
		return runIdQName;
	}

	/**
	 * @return the progressQName
	 */
	public static QName getProgressQName() {
		return progressQName;
	}

	public static QName getPathIdQName() {
		return pathIdQName;
	}

	public static QName getCountQName() {
		return countQName;
	}

}
