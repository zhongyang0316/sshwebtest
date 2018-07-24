package com.zy.sshwebtest.config;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * https://blog.csdn.net/jiangzeyin_/article/details/77992813
 * @author zhongyang
 *
 */
public class ShellSocketHandler extends TextWebSocketHandler {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	/**
	 * shellSsh会话
	 */
	private Map<String, ShellSshSession> shellSshSnMap = new ConcurrentHashMap<String, ShellSshSession>();
	
	/**
	 * shellSsh轮询线程
	 */
	private Map<String, ShellSshThread> shellSshTdMap = new ConcurrentHashMap<String, ShellSshThread>();
	
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		//建立连接之后
		this.logger.info("建立WebSocketSession,ID:{}", session.getId());
		super.afterConnectionEstablished(session);
	}
	
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		//关闭连接之后
		this.logger.info("关闭WebSocketSession,ID:{}", session.getId());
		removeShellInfo(session.getId());
		super.afterConnectionClosed(session, status);
	}
	
	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		//处理消息
		String messageStr = message.getPayload();
		this.logger.info("handle message,sessionId:{},message:{}", session.getId(), messageStr);
		//退出处理
		if (messageStr.equals("quit")){ //退出
			removeShellInfo(session.getId());
			session.sendMessage(new TextMessage("SSH已退出..."));
			return;
		}
		if (this.shellSshSnMap.containsKey(session.getId())) { //有相关ssh连接信息
			//处理客户端消息
			if (this.shellSshSnMap.get(session.getId()).getSshClient().isConnected()) { //连上sshServer
				//处理客户端消息
				this.shellSshSnMap.get(session.getId()).send(messageStr);
			} else { //未连上sshServer
				//删除之前的shellInfo
				removeShellInfo(session.getId());
				//返回断开连接
				session.sendMessage(new TextMessage("connected::false,请重新登录"));
			}
			
		} else { //未创建相关ssh连接信息
			//获取登陆信息
			if (messageStr.equals("DefaultLogin")) { //默认登录本机
				//创建sshSession
				ShellSshSession shellSshSession = new ShellSshSession();
				this.shellSshSnMap.put(session.getId(), shellSshSession);
				//创建轮询线程
				ShellSshThread shellSshThread = new ShellSshThread(session, shellSshSession.getShell().getInputStream());
				shellSshThread.start();
				this.shellSshTdMap.put(session.getId(), shellSshThread);
			} else if (messageStr.startsWith("connect::")) { //指定登录
				Map<String, String> loginMap = checkSshLoginMessage(messageStr);
				if (!loginMap.isEmpty() 
						&& !StringUtils.isEmpty(loginMap.get("host"))
						&& !StringUtils.isEmpty(loginMap.get("port"))
						&& !StringUtils.isEmpty(loginMap.get("user"))
						&& !StringUtils.isEmpty(loginMap.get("password"))) {
					//创建sshSession
					ShellSshSession shellSshSession = new ShellSshSession(loginMap.get("host"), 
							Integer.valueOf(loginMap.get("port")), 
							loginMap.get("user"), 
							loginMap.get("password"));
					this.shellSshSnMap.put(session.getId(), shellSshSession);
					//创建轮询线程
					ShellSshThread shellSshThread = new ShellSshThread(session, shellSshSession.getShell().getInputStream());
					shellSshThread.start();
					this.shellSshTdMap.put(session.getId(), shellSshThread);
				} else {
					session.sendMessage(new TextMessage("connected::false,请输入登录相关信息..."));
				}
			} else { //登录错误
				session.sendMessage(new TextMessage("请指定登录方式...."));
			}
			
		}
	}
	
	
	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		//有异常时的处理
		removeShellInfo(session.getId());
		this.logger.error("websocket handle error,sessionId:{},error:{}", session.getId(), exception);
		super.handleTransportError(session, exception);
	}
	
	private Map<String, String> checkSshLoginMessage(String trim){
		Map<String, String> loginMap = new HashMap<String, String>();
		if (trim.startsWith("connect::")) {
			String replace = trim.replace("connect::", "");
            String[] split = replace.split("@");
            String host = split[0];
            String port = split[1];
            String user = split[2];
            String password = split[3];
            loginMap.put("user", user);
            loginMap.put("host", host);
            loginMap.put("port", port);
            loginMap.put("password", password);
		}
		return loginMap;
	}
	
	private void removeShellInfo(String sessionId) throws Exception{
		//因需中断线程，需先删除sshSession
		//sshSession
		if (this.shellSshSnMap.containsKey(sessionId)) {
			this.shellSshSnMap.get(sessionId).close();
			this.shellSshSnMap.remove(sessionId);
		}
		//轮询线程
		if (this.shellSshTdMap.containsKey(sessionId)) {
			this.shellSshTdMap.get(sessionId).close();
			this.shellSshTdMap.remove(sessionId);
		}
	}
	
}
