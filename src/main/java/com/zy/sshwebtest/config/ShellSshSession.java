package com.zy.sshwebtest.config;

import java.io.Closeable;
import java.io.IOException;
import java.security.PublicKey;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Data;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Shell;
import net.schmizz.sshj.transport.verification.HostKeyVerifier;
import net.sf.expectit.Expect;
import net.sf.expectit.ExpectBuilder;

@Data
public class ShellSshSession implements Closeable {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private SSHClient sshClient;
	
	private Session session;
	
	private Shell shell;
	
	private Expect expect;
	
	/**
	 * 默认创建本机sshSession
	 * <p/>http://www.ruanyifeng.com/blog/2011/12/ssh_remote_login.html
	 * @throws Exception 
	 */
	public ShellSshSession() throws Exception {
		this.sshClient = new SSHClient();
		this.sshClient.loadKnownHosts();
		this.sshClient.connect("localhost");
		this.sshClient.authPublickey(System.getProperty("user.name"));
		
		this.session = sshClient.startSession();
		
		this.shell = this.session.startShell();
		this.shell.setAutoExpand(true);
		
		this.expect = new ExpectBuilder()
				.withOutput(shell.getOutputStream())
				.withInputs(shell.getInputStream(), shell.getErrorStream())
				.withTimeout(5, TimeUnit.SECONDS)
				.withCombineInputs(true)
				.withAutoFlushEcho(true)
				.build();
	}
	
	public ShellSshSession(String host, int port, String username, String password) throws Exception{
		this.sshClient = new SSHClient();
		this.sshClient.addHostKeyVerifier(new HostKeyVerifier() {
			
			@Override
			public boolean verify(String paramString, int paramInt, PublicKey paramPublicKey) {
				return true;
			}
		});
		this.sshClient.connect(host, port);
		this.sshClient.authPassword(username, password);
		
		this.session = sshClient.startSession();
		
		this.shell = this.session.startShell();
		this.shell.setAutoExpand(true);
		
		this.expect = new ExpectBuilder()
				.withOutput(shell.getOutputStream())
				.withInputs(shell.getInputStream(), shell.getErrorStream())
				.withTimeout(5, TimeUnit.SECONDS)
				.withCombineInputs(true)
				.withAutoFlushEcho(true)
				.build();
		
	}
	
	public void send(String message) throws Exception{
		if (message.equals("CTRLC")) { //ctrl+c处理
			this.shell.getOutputStream().write("\u0003".getBytes());
			this.shell.getOutputStream().flush();
		} else {
			this.expect.sendLine(message);
		}
	}

	@Override
	public void close() throws IOException {
		if (this.shell != null) {
			this.shell.close();
			this.logger.debug("ShellSshSession shell closed");
			this.shell = null;
		}
		if (this.expect != null) {
			this.expect.close();
			this.logger.debug("ShellSshSession expect closed");
			this.expect = null;
		}
		if (this.session != null) {
			this.session.close();
			this.logger.debug("ShellSshSession session closed");
			this.session = null;
		}
		if (this.sshClient != null) {
			this.sshClient.disconnect();
			this.sshClient.close();
			this.logger.debug("ShellSshSession sshClient closed");
			this.sshClient = null;
		}
	}

}
