package com.mindtree.amazon;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;

public class ClientConfigurer {
	public static ClientConfiguration getConfiguration(LocalProxy proxy) {
		if (proxy == null) {
			return new ClientConfiguration();
		} else {
			ClientConfiguration cc = new ClientConfiguration();
			if (proxy.getProxyDomain() != null) {
				cc.setProxyDomain(proxy.getProxyDomain());
			}
			if (proxy.getProxyHost() != null) {
				cc.setProxyHost(proxy.getProxyHost());
			}
			if (proxy.getProxyPort() != -1) {
				cc.setProxyPort(proxy.getProxyPort());
			}
			if (proxy.getProxyUsername() != null) {
				cc.setProxyUsername(proxy.getProxyUsername());
			}
			if (proxy.getProxyPassword() != null) {
				cc.setProxyPassword(proxy.getProxyPassword());
			}
			if (proxy.getProtocol() != null) {
				if (proxy.getProtocol().equalsIgnoreCase(
						Protocol.HTTPS.toString())) {
					cc.setProtocol(Protocol.HTTPS);
				}
			}
			return cc;
		}
	}
}
