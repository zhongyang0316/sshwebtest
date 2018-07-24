package com.zy.sshwebtest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class SshWebSocketConfig implements WebSocketConfigurer {

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(shellSocketHandler(), "/shell").withSockJS();
	}
	
	@Bean
	public ShellSocketHandler shellSocketHandler(){
		ShellSocketHandler shellSocketHandler = new ShellSocketHandler();
		return shellSocketHandler;
	}

}
