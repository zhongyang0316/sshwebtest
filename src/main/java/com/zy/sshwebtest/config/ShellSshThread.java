package com.zy.sshwebtest.config;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

public class ShellSshThread extends Thread implements Closeable {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private WebSocketSession session;
	
	private BufferedReader reader;
	
	private boolean run = true;
	
	public ShellSshThread(WebSocketSession session, InputStream in){
		this.reader = new BufferedReader(new InputStreamReader(in));
        this.session = session;
	}
	
	@Override
	public void run() {
		String line = null;
		try {
			while (this.run 
					&& this.reader != null
					&& this.session != null
					&& (line = reader.readLine()) != null) {
				session.sendMessage(new TextMessage(line));
			}
		} catch (Exception e) {
			this.logger.error("ShellSshThread Run Error:{}", e);
			this.run = false;
			if (this.session != null) {
				this.session = null;
			}
			if (this.reader != null) {
				try {
					this.reader.close();
				} catch (IOException e1) {
					this.logger.error("ShellSshThread Run Error:{}", e1);
				}
				this.reader = null;
			}
		}
	}

	@Override
	public void close() throws IOException {
		this.run = false;
		if (this.session != null) {
			this.session = null;
		}
		if (reader != null) {
			this.logger.debug("ShellSshThread2 reader closed");
			this.reader.close();
			this.reader = null;
		}
	}
	
	
	
}
