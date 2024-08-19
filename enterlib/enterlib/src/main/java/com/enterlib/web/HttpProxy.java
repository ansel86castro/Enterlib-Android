package com.enterlib.web;

public class HttpProxy {
	String username;
	String password;
	String host;
	int port;
	String authScheme;

	public HttpProxy(String username, String password, String host, int port) {
		this.username = username;
		this.password = password;
		this.host = host;
		this.port = port;
	}

	public HttpProxy(String username, String password, String host, int port,
			String authScheme) {
		this.username = username;
		this.password = password;
		this.host = host;
		this.port = port;
		this.authScheme = authScheme;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getAuthScheme() {
		return authScheme;
	}

	public void setAuthScheme(String authScheme) {
		this.authScheme = authScheme;
	}
}
