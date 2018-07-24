package com.zy.sshwebtest;

import java.io.InputStream;
import java.security.PublicKey;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import net.schmizz.sshj.connection.channel.direct.Session.Shell;
import net.schmizz.sshj.transport.verification.HostKeyVerifier;
import net.sf.expectit.Expect;
import net.sf.expectit.ExpectBuilder;
import net.sf.expectit.Result;
import net.sf.expectit.matcher.Matchers;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringBootSshApplicationTests {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Test
	public void test1() throws Exception {
		this.logger.info("测试开始。。。");
		String host = "123.206.231.25";
		int port = 22;
		String username = "mzadmin";
		String password = "mzadmin";
		//创建SSH客户端
		SSHClient sshClient = new SSHClient();
		sshClient.addHostKeyVerifier(new HostKeyVerifier() {
			
			@Override
			public boolean verify(String paramString, int paramInt, PublicKey paramPublicKey) {
				return true;
			}
		});
		sshClient.connect(host, port);
		sshClient.authPassword(username, password);
		Session session = sshClient.startSession();
		
		//执行命令Command
//		this.logger.info("执行命令:[pwd]");
//		Command cmd = session.exec("pwd");
//		cmd.join(10, TimeUnit.SECONDS);
//		explainCmd(cmd);
		
        //shell
		Shell shell = session.startShell();
		shell.setAutoExpand(true);
		Expect expect = new ExpectBuilder()
				.withOutput(shell.getOutputStream())
				.withInputs(shell.getInputStream(), shell.getErrorStream())
				.withTimeout(5, TimeUnit.SECONDS)
				.withCombineInputs(true)
				.withAutoFlushEcho(true)
				.build();
		Result result = expect.expect(Matchers.anyString());
		this.logger.info("服务端响应:{}", result.getInput());
		
//		result = expect.expect(Matchers.anyString());
//		this.logger.info("服务端响应:{}", result.getInput());
		
		this.logger.info("执行命令[pwd]");
		expect.sendLine("pwd");
		result = expect.expect(Matchers.anyString());
		this.logger.info("服务端响应:{}", result.getInput());
		
		this.logger.info("执行命令[date]");
		expect.sendLine("date");
		result = expect.expect(Matchers.anyString());
		this.logger.info("服务端响应:{}", result.getInput());
		
		this.logger.info("执行命令[cd ..]");
		expect.sendLine("cd ..");
		result = expect.expect(Matchers.eof());
		this.logger.info("服务端响应:{}", result.getInput());
		
		this.logger.info("执行命令[pwd]");
		expect.sendLine("pwd");
		result = expect.expect(Matchers.anyString());
		this.logger.info("服务端响应:{}", result.getInput());
		
		shell.close();
		expect.close();
		
        //关闭
        session.close();
        sshClient.close();
        
	}
	
	@Test
	public void test2() throws Exception{
		this.logger.info("测试开始。。。");
		String host = "123.206.231.25";
		int port = 22;
		String username = "mzadmin";
		String password = "mzadmin";
		//创建SSH客户端
		SSHClient sshClient = new SSHClient();
		sshClient.addHostKeyVerifier(new HostKeyVerifier() {
			
			@Override
			public boolean verify(String paramString, int paramInt, PublicKey paramPublicKey) {
				return true;
			}
		});
		sshClient.connect(host, port);
		sshClient.authPassword(username, password);
		Session session = sshClient.startSession();
		Shell shell = session.startShell();
		shell.setAutoExpand(true);
//		OutputStream out = shell.getOutputStream();
//		InputStream in = shell.getInputStream();
//		InputStream errIn = shell.getErrorStream();
//		
//		out.write("pwd".getBytes());
		
		
		//pwd命令
		shell.close();
		session.close();
		sshClient.close();
		
	}
	
	@SuppressWarnings("unused")
	private void explainCmd(Command cmd) throws Exception{
		this.logger.info("cmd status:{}", cmd.getExitStatus());
		StringBuffer out = new StringBuffer();
		InputStream in = cmd.getInputStream();
        byte[] b = new byte[4096];
        
        for (int n;(n = in.read(b)) != -1;) {
        	out.append(new String(b, 0, n));
        }
        
        this.logger.info("服务端响应:{},失败响应:{}",out.toString(),cmd.getExitErrorMessage());
        in.close();
	}
	
//	public static void main(String[] args) throws Exception {
//		SSHClient sshClient = new SSHClient();
//		sshClient.loadKnownHosts();
//		sshClient.connect("localhost");
//		sshClient.authPublickey(System.getProperty("user.name"));
//		Session session = sshClient.startSession();
//		
//		Command cmd = session.exec("ping www.baidu.com");
//        System.out.println(IOUtils.readFully(cmd.getInputStream()).toString());
//        cmd.join(5, TimeUnit.SECONDS);
//        System.out.println("\n** exit status: " + cmd.getExitStatus());
//		
//        //关闭
//        session.close();
//        sshClient.disconnect();
//	}
	
}
