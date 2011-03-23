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

import java.io.ByteArrayOutputStream;

import org.globus.gsi.GlobusCredential;

import de.uni_luebeck.inb.knowarc.grid.GridFtpConnection;
import de.uni_luebeck.inb.knowarc.grid.ProgressDisplay;

public class OnDemandDownloadGridftp implements OnDemandDownload {
	private final String gsiftp;
	private final String filepath;
	private final boolean binary;
	private final ProgressDisplay progress;
	private final GlobusCredential certificateData;

	public OnDemandDownloadGridftp(ProgressDisplay progress, GlobusCredential certificateData, String gsiftp, String filepath, boolean binary) {
		this.progress = progress;
		this.certificateData = certificateData;
		this.gsiftp = gsiftp;
		this.filepath = filepath;
		this.binary = binary;
	}

	public String getReferenceURL() {
		return gsiftp + "/" + filepath;
	}

	public Object download() {
		Object msg = progress.getMessage("Downloading " + filepath + " from grid.");
		Object ret = downloadInner();
		progress.removeMessage(msg);
		return ret;
	}

	private Object downloadInner() {
		progress.log(2, "on-demand download: ");
		progress.log(2, "gsiftp: " + gsiftp);
		progress.log(2, "filepath: " + filepath);
		GridFtpConnection download;
		for (int retriesLeft = 10; retriesLeft >= 0; retriesLeft--) {
			try {
				download = new GridFtpConnection(progress, gsiftp, null, certificateData);
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				download.Download(out, filepath);
				if (binary)
					return out.toByteArray();
				else
					return out.toString();
			} catch (Throwable e) {
				if (retriesLeft == 0)
					progress.logTrace(0, e);
				else
					progress.logTrace(1, e);
			}
		}
		return null;
	}

	public void copyToFile(String targetPath) {
		Object msg = progress.getMessage("Downloading " + filepath + " from grid to " + targetPath + ".");

		progress.log(2, "on-demand download: ");
		progress.log(2, "gsiftp: " + gsiftp);
		progress.log(2, "filepath: " + filepath);
		GridFtpConnection download;
		for (int retriesLeft = 10; retriesLeft >= 0; retriesLeft--) {
			try {
				download = new GridFtpConnection(progress, gsiftp, null, certificateData);
				download.Download(targetPath, filepath);
				break;
			} catch (Throwable e) {
				if (retriesLeft == 0)
					progress.logTrace(0, e);
				else
					progress.logTrace(1, e);
			}
		}

		progress.removeMessage(msg);
	}
}
