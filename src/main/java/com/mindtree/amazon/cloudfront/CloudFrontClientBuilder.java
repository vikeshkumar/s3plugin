package com.mindtree.amazon.cloudfront;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cloudfront.AmazonCloudFrontClient;
import com.mindtree.amazon.ClientConfigurer;
import com.mindtree.amazon.LocalProxy;

public class CloudFrontClientBuilder {

	public static AmazonCloudFrontClient getClient(String accessKey,
			String secretKey, LocalProxy proxy, boolean async) {
		if (proxy == null) {
			return new AmazonCloudFrontClient(new BasicAWSCredentials(
					accessKey, secretKey));
		} else {
			return new AmazonCloudFrontClient(new BasicAWSCredentials(
					accessKey, secretKey),
					ClientConfigurer.getConfiguration(proxy));
		}
	}
}
