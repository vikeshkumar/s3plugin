package com.mindtree.amazon;

public final class LocalProxy {
	private String proxyDomain;
	private String proxyHost;
	private String proxyPassword;
	private int proxyPort;
	private String proxyUsername;
	private String proxyWorkstation;
	private String protocol;
	public static final String LOCAL_PROXY = "LocalProxy";

	public LocalProxy(String proxyHost, String proxyDomain, int proxyPort,
			String proxyUsername, String proxyPassword,
			String proxyWorkstation, String protocol) {
		super();
		this.proxyDomain = proxyDomain;
		this.proxyHost = proxyHost;
		this.proxyPassword = proxyPassword;
		this.proxyPort = proxyPort;
		this.proxyUsername = proxyUsername;
		this.proxyWorkstation = proxyWorkstation;
	}

	public String getProxyHost() {
		return proxyHost;
	}

	public String getProxyDomain() {
		return proxyDomain;
	}

	public int getProxyPort() {
		return proxyPort;
	}

	public String getProxyUsername() {
		return proxyUsername;
	}

	public String getProxyWorkstation() {
		return proxyWorkstation;
	}

	public String getProxyPassword() {
		return proxyPassword;
	}

	public String getProtocol() {
		return protocol;
	}
}
