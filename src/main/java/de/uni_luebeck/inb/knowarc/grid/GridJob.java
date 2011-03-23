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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.globus.ftp.exception.ClientException;
import org.globus.ftp.exception.ServerException;
import org.globus.gsi.GlobusCredential;
import org.ietf.jgss.GSSException;

/**
 * Internal representation of all what one possibly would want to know about a grid job.
 */
public class GridJob {
	
	/**
	 * With ARC, the job is submitted via gridftp.
	 */
	GridFtpConnection connection;
	/**
         * Constructor, only the connection is expected.
	 */
	public GridJob(GridFtpConnection connection)
	{
		this.connection = connection;
	}
	
	/*
	 * our connection might have failed, so assign a new one
	 */
	public void reconnect(GlobusCredential cert) throws GSSException, IOException, ServerException
	{
		connection = connection.reconnect(cert);
	}
	
	/**
	 * The job ID under which the job is identified on the grid.
	 */
	public String jobid = null;
	/**
 	 * The xrsl description of the job.
	 */
	public String xrsl = null;
	
	/**
	 * The job is submitted, i.e. the xrsl description of the job is
	 * uploaded into the new folder with gridftp
 	 */
    public String Submit() throws ServerException, IOException, ClientException {
    	jobid = connection.createNewFolderAndFetchId();
        
    	ByteArrayInputStream buffer = new ByteArrayInputStream(xrsl.getBytes("US-ASCII"));
    	connection.Upload(buffer,jobid+"/new");
    	return jobid;
    }


	/**
	 * Retrieve status information. The status file is retrieved from
	 * the gridftp server and represents the current state.
	 */
    public String State() throws ServerException, ClientException, IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        connection.Download(buffer,"info/"+jobid+"/status");
        String state = buffer.toString("US-ASCII").replace('\n', ' ').trim(); 
        return state;
    }
    
	/**
	 * A job that offers an entry 'failed' in its info directory
	 * has failed. Consequently, a ServerException is interpreted positively,
	 * i.e. the failed file is not present and the job must hence be
	 * good, still. Some more thoughts should go into this scenario
	 * to investigate if there are more cases to investigate, e.g. a site
	 * having gone down.
	 */
    public String Failed() throws ClientException, IOException {
    	try{ 
	        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	        connection.Download(buffer,"info/"+jobid+"/failed");
	        String state = buffer.toString("US-ASCII").replace('\n', ' ').trim(); 
	        return state;
    	} catch( ServerException ex ) {
    		return null;
    	}
    }
    
    /**
     * A stream is uploaded to the grid into the job directory.
     */
    public void Input(String name, ByteArrayInputStream stream) throws ServerException, ClientException, IOException {
    	connection.Upload(stream,jobid+"/"+name);
    }
    /**
     * A local file is uploaded to the grid into the job directory.
     */
    public void InputLocal(String name, String source) throws ServerException, ClientException, IOException {
    	connection.Upload(source,jobid+"/"+name);
    }
    
    /**
     * A file is read from the grid and offered as a stream.
     */
    public ByteArrayOutputStream Output(String name) throws ServerException, ClientException, IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        connection.Download(buffer,jobid+"/"+name);
        return buffer;
    }

	/**
	 * Kill. This is used, for example, if a grid job times out and we want to start a second instance.
	 */
    public void Kill() throws ServerException, IOException {
    	connection.deleteFile(jobid);
    	jobid = null;
    }
    
	/**
	 * Cleanup. The job folder at the grid site is removed.
	 */
    public void Clean() throws ServerException, IOException {
    	connection.deleteFolder(jobid);
    	jobid = null;
    }

	public boolean hasGramiFile() throws ClientException, IOException {
		try{ 
	        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	        connection.Download(buffer,"info/"+jobid+"/grami");
	        return true;
    	} catch( ServerException ex ) {
    		return false;
    	}
	}
}

