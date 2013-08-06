/**
 *
 */
package net.sf.taverna.t2.activities.interaction.atom;

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

}
