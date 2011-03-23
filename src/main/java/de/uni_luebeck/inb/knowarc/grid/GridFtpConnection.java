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

package de.uni_luebeck.inb.knowarc.grid;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Vector;

import org.globus.ftp.DataSinkStream;
import org.globus.ftp.DataSourceStream;
import org.globus.ftp.GridFTPClient;
import org.globus.ftp.GridFTPSession;
import org.globus.ftp.Marker;
import org.globus.ftp.MarkerListener;
import org.globus.ftp.MlsxEntry;
import org.globus.ftp.exception.ClientException;
import org.globus.ftp.exception.ServerException;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.util.GlobusURL;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

/**
 * A connection to a remote site via the GridFTP protocoll is established.
 */
public class GridFtpConnection {
	private final ProgressDisplay progressDisplay;
	protected GridFTPClient ftpclient = null;

	private final String fullUrl;
	private final String urlPath;
	private final String queueToSubmitTo;

	/**
	 * The constructor
	 * 
	 * @param wholeUrl
	 *            - URL as a string
	 * @param GlobusCredential
	 *            - what was once created with grid-proxy-init
	 */
	public GridFtpConnection(ProgressDisplay progressDisplay, String wholeUrl, String queue, GlobusCredential cert) throws ServerException, IOException,
			GSSException {
		this.progressDisplay = progressDisplay;
		this.queueToSubmitTo = queue;
		if (null == cert) {
			progressDisplay.log(0, "GridFtpConnection: The certificate is null, which should not happen.");
			throw new RuntimeException("GridFtpConnection: the constructor was passed a cert that was 'null', which should not happen.");
		}
		progressDisplay.log(4, "Accessing the system with the following credential:");
		progressDisplay.log(4, cert.toString());

		fullUrl = wholeUrl;
		GlobusURL url = new GlobusURL(wholeUrl);
		urlPath = url.getPath();
		int port = url.getPort();
		if (port == -1) {
			port = GlobusURL.getPort(url.getProtocol());
		}
		progressDisplay.log(1, "GridFtpConnection: Connecting to " + wholeUrl);
		ftpclient = new GridFTPClient(url.getHost(), port);
		GlobusGSSCredentialImpl credentialImpl = new GlobusGSSCredentialImpl(cert, GSSCredential.INITIATE_AND_ACCEPT);
		if (null == credentialImpl) {
			progressDisplay.log(0, "Could not prepare GlobusGSSCredentialImpl.");
		} else {
			progressDisplay.log(2, "The GlobusGSSCredentialImpl derived from the prior GlobusCredential is:");
			progressDisplay.log(2, "Name:     " + credentialImpl.getName());
			progressDisplay.log(2, "Lifetime: " + credentialImpl.getRemainingLifetime());
		}
		ftpclient.authenticate(credentialImpl);
		ftpclient.changeDir("/" + urlPath);
	}

	@Override
	protected void finalize() {
		Disconnect();
	}

	public String getUrl() {
		return fullUrl;
	}

	public String getQueueToSubmitTo() {
		return queueToSubmitTo;
	}

	public GridFtpConnection reconnect(GlobusCredential cert) throws ServerException, IOException, GSSException {
		return new GridFtpConnection(progressDisplay, fullUrl, queueToSubmitTo, cert);
	}

	/**
	 * The connection is closed.
	 */
	public void Disconnect() {
		if (ftpclient == null)
			return;

		try {
			ftpclient.close();
		} catch (ServerException e) {
			progressDisplay.logTrace(1, e);
		} catch (IOException e) {

			if (e instanceof SocketException) {
				// stay quiet, we know the socket is closed ;)
			} else {
				progressDisplay.logTrace(1, e);
			}
		}
		ftpclient = null;
	}

	/**
	 * When changing into the new directory, the effective directory is a newly
	 * created directory, the name of which is the job ID.
	 */
	public String createNewFolderAndFetchId() throws ServerException, IOException {
		ftpclient.changeDir("new");
		String id = ftpclient.getCurrentDir();
		int p = id.lastIndexOf('/');
		if (p != -1) {
			id = id.substring(p + 1);
		}
		return id;
	}

	/**
	 * A file is deleted within the current directory of the gridftp server
	 * 
	 * @param filename
	 *            - relative location of file
	 */
	public void deleteFile(String filename) throws ServerException, IOException {
		ftpclient.deleteFile("/" + urlPath + "/" + filename);
	}

	/**
	 * A whole folder is deleted in the current directory on the gridftp server
	 * 
	 * @param foldername
	 *            - relative location of folder
	 */
	public void deleteFolder(String foldername) throws ServerException, IOException {
		ftpclient.deleteDir("/" + urlPath + "/" + foldername);
	}

	/**
	 * An input stream is stored as a file in the remote directory.
	 * 
	 * @param in
	 *            - input stream of file to upload
	 * @param name
	 *            - name in destination directory
	 */
	public void Upload(InputStream in, String name) throws ServerException, ClientException, IOException {
		progressDisplay.log(2, "Uploading " + name + " ...");
		ftpclient.setType(GridFTPSession.TYPE_IMAGE);
		ftpclient.setPassiveMode(true);
		DataSourceStream stream = new DataSourceStream(in);
		ftpclient.put("/" + urlPath + "/" + name, stream, new MarkerListener() {
			public void markerArrived(Marker arg0) {
			}
		});
	}

	/**
	 * An local file is stored as a file in the remote directory.
	 * 
	 * @param source
	 *            - name of the local file to upload
	 * @param name
	 *            - name in destination directory
	 */
	public void Upload(String source, String name) throws ServerException, ClientException, IOException {
		progressDisplay.log(2, "Uploading " + name + " ...");
		ftpclient.setType(GridFTPSession.TYPE_IMAGE);
		ftpclient.setPassiveMode(true);
		URL sourceUrl = new URL(source);
		DataSourceStream dataSource = new DataSourceStream(new FileInputStream(sourceUrl.getPath()));
		ftpclient.put("/" + urlPath + "/" + name, dataSource, new MarkerListener() {
			public void markerArrived(Marker arg0) {
			}
		});
	}

	/**
	 * A file is downloaded from the remote directory.
	 * 
	 * @param out
	 *            - stream to write the file to
	 * @param name
	 *            - name of file to download (in current directory)
	 */
	public void Download(OutputStream out, String name) throws ServerException, ClientException, IOException {
		DataSinkStream stream = new DataSinkStream(out);
		ftpclient.setType(GridFTPSession.TYPE_IMAGE);
		ftpclient.setPassiveMode(true);
		ftpclient.get("/" + urlPath + "/" + name, stream, new MarkerListener() {
			public void markerArrived(Marker arg0) {
			}
		});
	}

	/**
	 * A file is downloaded from the remote directory to a local file.
	 * 
	 * @param outputFile
	 *            - name of file to the file to write to
	 * @param name
	 *            - name of file to download (in current directory)
	 */
	public void Download(String outputFile, String name) throws ServerException, ClientException, IOException {
		ftpclient.setType(GridFTPSession.TYPE_IMAGE);
		ftpclient.setPassiveMode(true);
		DataSinkStream dataSinkStream = new DataSinkStream(new FileOutputStream(outputFile));
		ftpclient.get("/" + urlPath + "/" + name, dataSinkStream, new MarkerListener() {
			public void markerArrived(Marker arg0) {
			}
		});
	}

	/**
	 * Retrieval of directory entries.
	 * 
	 * @param foldername
	 *            - either "" to identify the current directory or non-empty to
	 *            identify a subfolder of the current directory
	 * @param dirs
	 *            - filled with names of directories encountered in the folder
	 *            inspected
	 * @param files
	 *            - filled with names of files encountered in the folder
	 *            inspected
	 */
	public void List(String folderName, ArrayList<String> dirs, ArrayList<String> files) throws ServerException, ClientException, IOException {
		String fdir = new String("/" + urlPath);
		if (!folderName.equals(""))
			fdir += "/" + folderName;
		ftpclient.setPassiveMode(true);
		Vector<?> list = ftpclient.mlsd(fdir);
		for (int i = 0; i < list.size(); i++) {
			MlsxEntry entry = (MlsxEntry) (list.get(i));
			String type = entry.get("type");
			String name = entry.getFileName();
			if (name.equals("."))
				continue;
			if (name.equals(".."))
				continue;
			if (!folderName.equals(""))
				name = folderName + "/" + name;
			if (type.equals("dir")) {
				dirs.add(name);
			} else if (type.equals("file")) {
				files.add(name);
			}
		}
	}

}
