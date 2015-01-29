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

import net.sf.taverna.t2.workflowmodel.serialization.DeserializationException;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class UseCaseEnumeration {

	private static Logger logger = Logger.getLogger(UseCaseEnumeration.class);

	public static List<UseCaseDescription> readDescriptionsFromUrl(String xmlFileUrl) throws IOException {

		List<UseCaseDescription> ret = new ArrayList<UseCaseDescription>();
		URLConnection con = null;
		try {
			URL url = new URL(xmlFileUrl);

			con = url.openConnection();
			con.setConnectTimeout(4000);
			ret = readDescriptionsFromStream(con.getInputStream());
			
		} catch (IOException ioe) {
			logger.error("Problem retrieving from " + xmlFileUrl);
			logger.error(ioe);
			throw ioe;
		}
		finally {

		}

		return ret;

	}
	
	public static List<UseCaseDescription> readDescriptionsFromStream(InputStream is) {
		
		List<UseCaseDescription> ret = new ArrayList<UseCaseDescription>();

		SAXBuilder builder = new SAXBuilder();
		Document doc = null;
		try {
			doc = builder.build(is);
			is.close();
		} catch (JDOMException e1) {
			logger.error(e1);
			return ret;
		} catch (IOException e1) {
			logger.error(e1);
			return ret;
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				logger.error(e);
			}
		}

		Element usecases = doc.getRootElement();
		for (Object ochild : usecases.getChildren()) {
			Element child = (Element) ochild;
			if (child.getName().equalsIgnoreCase("program")) {
					try {
						ret.add(new UseCaseDescription(child));
					} catch (DeserializationException e) {
						logger.error(e);
					}
			}
		}
		return ret;
	}

	public static UseCaseDescription readDescriptionFromUrl(
			String repositoryUrl, String id) throws IOException {
		List<UseCaseDescription> descriptions = readDescriptionsFromUrl(repositoryUrl);
		for (UseCaseDescription usecase : descriptions) {
			if (usecase.getUsecaseid().equals(id)) {
				return usecase;
			}
		}
		return null;
	}
}
