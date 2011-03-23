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

package de.uni_luebeck.inb.knowarc.usecases.invocation;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class OnDemandDownloadLocalUrl implements OnDemandDownload {
	private String url;
	private boolean binary;

	public OnDemandDownloadLocalUrl(String url, boolean binary) {
		this.url = url;
		this.binary = binary;
	}

	public Object download() {
		try {
		    
			URL ur = new URL(url);
			InputStream str = ur.openStream();
			int length = 0;
			final int blocksize = 16 * 1024;
			byte[] targetBuffer = new byte[blocksize];
			while (true) {
				if (targetBuffer.length - length < blocksize) {
					int newsize = length + 2 * blocksize;
					byte[] newtarget = new byte[newsize];
					System.arraycopy(targetBuffer, 0, newtarget, 0, length);
					targetBuffer = newtarget;
				}
				int nr = str.read(targetBuffer, length, targetBuffer.length - length);
				if (nr == -1)
					break;
				length += nr;
			}
			str.close();

			byte[] dataBuffer = new byte[length];
			System.arraycopy(targetBuffer, 0, dataBuffer, 0, length);
			targetBuffer = null;

			Object ret = null;
			if (binary)
				ret = dataBuffer;
			else
				ret = Charset.forName("US-ASCII").decode(ByteBuffer.wrap(dataBuffer)).toString();
			return ret;
		} catch (Exception e) {
			// TODO
		    
			return null;
		}
	}

	public String getReferenceURL() {
		return url;
	}

	public void copyToFile(String path) {
		try {
			FileOutputStream output = new FileOutputStream(path);
			URL ur = new URL(url);
			InputStream str = ur.openStream();
			byte[] buffer = new byte[16 * 1024];
			while (true) {
				int nr = str.read(buffer);
				if (nr == -1)
					break;
				output.write(buffer, 0, nr);
			}
			str.close();
			output.close();
			
		} catch (Exception e) {
		    // TODO
		}
	}

}
