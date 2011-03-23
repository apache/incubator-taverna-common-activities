/**
 * 
 */
package de.uni_luebeck.inb.knowarc.usecases.invocation.ssh;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import de.uni_luebeck.inb.knowarc.usecases.invocation.AskUserForPw;

/**
 * @author alanrw
 *
 */
public class SshPool {
	
	private static JSch jsch = new JSch();
	
    private static int CONNECT_TIMEOUT = 10000; // milliseconds

	private static Map<SshNode, Session> sessionMap = Collections.synchronizedMap(new HashMap<SshNode, Session> ());
	private static Map<Session, ChannelSftp> sftpGetMap = Collections.synchronizedMap(new HashMap<Session, ChannelSftp> ());
	private static Map<Session, ChannelSftp> sftpPutMap = Collections.synchronizedMap(new HashMap<Session, ChannelSftp> ());
	
	public static Session getSshSession(final SshUrl sshUrl, final AskUserForPw askUserForPw) throws JSchException {
		return getSshSession(sshUrl.getSshNode(), askUserForPw);
	}
	
	public static synchronized Session getSshSession(final SshNode sshNode, final AskUserForPw askUserForPw) throws JSchException {

		Session s = sessionMap.get(sshNode);
		if ((s != null) && s.isConnected()) {
			System.err.println("Reusing session");
			return s;
		}
		if (s != null) {
			System.err.println("Session was not connected");
		}
		if (s == null) {
			System.err.println("No session found for " + sshNode.toString());
		}

		if (askUserForPw.getKeyfile().length() > 0) {
			jsch.addIdentity(askUserForPw.getKeyfile());
		}
		System.err.println("Using host is " + sshNode.getHost() + " and port " + sshNode.getPort());
		Session sshSession = jsch.getSession(askUserForPw.getUsername(), sshNode.getHost(), sshNode.getPort());
		sshSession.setUserInfo(new SshAutoLoginTrustEveryone(askUserForPw));
		sshSession.connect(CONNECT_TIMEOUT);
		
		askUserForPw.authenticationSucceeded();
		sessionMap.put(sshNode, sshSession);
		if (sshSession == null) {
			System.err.println("Returning a null session");
		}
		return sshSession;
	}
	
	public static ChannelSftp getSftpGetChannel(SshNode sshNode, final AskUserForPw askUserForPw) throws JSchException {
		return getSftpGetChannel(getSshSession(sshNode, askUserForPw));
	}

	private static synchronized ChannelSftp getSftpGetChannel(Session session) throws JSchException {
		ChannelSftp result = sftpGetMap.get(session);
		if (!session.isConnected()) {
			System.err.println("Session is not connected");
		}
		if (result == null) {
			System.err.println("Creating new sftp channel");
			result = (ChannelSftp) session.openChannel("sftp");
			sftpGetMap.put(session, result);
		}
		else {
			System.err.println("Reusing sftp channel");			
		}
		if (!result.isConnected()) {
			System.err.println("Connecting");
			result.connect();
		} else {
			System.err.println("Already connected");
		}
		return result;
	}
	
	public static ChannelSftp getSftpPutChannel(SshNode sshNode, final AskUserForPw askUserForPw) throws JSchException {
		return getSftpPutChannel(getSshSession(sshNode, askUserForPw));
	}

	private static synchronized ChannelSftp getSftpPutChannel(Session session) throws JSchException {
	    ChannelSftp result = null;
	    synchronized(sftpPutMap) {
		result = sftpPutMap.get(session);
		if (!session.isConnected()) {
			System.err.println("Session is not connected");
		}
		if (result == null) {
			System.err.println("Creating new sftp channel");
			result = (ChannelSftp) session.openChannel("sftp");
			sftpPutMap.put(session, result);
		}
		else {
			System.err.println("Reusing sftp channel");			
		}
	    }
	    if (!result.isConnected()) {
		System.err.println("Connecting");
		result.connect(CONNECT_TIMEOUT);
	    } else {
		System.err.println("Already connected");
	    }
	    return result;
	}
	
	public static synchronized ChannelExec openExecChannel(SshNode sshNode, final AskUserForPw askUserForPw) throws JSchException {
		return (ChannelExec) getSshSession(sshNode, askUserForPw).openChannel("exec");
	}

	private static synchronized ChannelExec openExecChannel(Session session) throws JSchException {
		return (ChannelExec) session.openChannel("exec");
	}

}













