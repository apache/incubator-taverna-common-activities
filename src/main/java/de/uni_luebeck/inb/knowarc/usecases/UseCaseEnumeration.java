/* Part of the KnowARC Janitor Use-case processor for taverna
 *  written 2007-2010 by Hajo Nils Krabbenhoeft and Steffen Moeller
 *  University of Luebeck, Institute for Neuro- and Bioinformatics
 *  University of Luebeck, Institute for Dermatolgy
 *
 *  This package is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This package is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this package; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301 USA
 */

package de.uni_luebeck.inb.knowarc.usecases;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class UseCaseEnumeration {

	private static Logger logger = Logger.getLogger(UseCaseEnumeration.class);

	public static List<UseCaseDescription> enumerateXmlFile(String xmlFileUrl) {

		ArrayList<UseCaseDescription> ret = new ArrayList<UseCaseDescription>();
		try {
			enumerateXmlInner(xmlFileUrl, ret);
		} catch (IOException ioe) {
			logger.error("Problem retrieving from " + xmlFileUrl);
			logger.error(ioe);
			return ret;
		} catch (JDOMException jdome) {
			logger.error("Problem with document retrieved from " + xmlFileUrl);
			logger.error(jdome);
			return ret;
		} catch (Exception e) {
			logger.error("Got this error for URL '" + xmlFileUrl + "'");
			logger.error(e);
		}

		return ret;

	}

	public static void enumerateXmlInner(String xmlFileUrl, ArrayList<UseCaseDescription> ret) throws MalformedURLException,
			IOException, JDOMException {
		URL url = new URL(xmlFileUrl);

		URLConnection con = url.openConnection();
		con.setConnectTimeout(4000);
		InputStream is = con.getInputStream();

		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(is);
		is.close();

		Element usecases = doc.getRootElement();
		for (Object ochild : usecases.getChildren()) {
			Element child = (Element) ochild;
			if (child.getName().equalsIgnoreCase("program")) {
				try {
					ret.add(new UseCaseDescription(child));
				} catch (Exception e) {
					String name = child.getAttributeValue("name");
					// TODO
				}
			}
		}
	}
}
