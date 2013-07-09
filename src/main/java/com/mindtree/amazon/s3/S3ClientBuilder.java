package com.mindtree.amazon.s3;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.mindtree.amazon.ClientConfigurer;
import com.mindtree.amazon.LocalProxy;

public class S3ClientBuilder {
	private static final Logger logger = Logger.getLogger(S3ClientBuilder.class
			.getName());

	private S3ClientBuilder() {

	}

	public static AmazonS3 getClient(String accessKey, String secretKey,
			LocalProxy proxy) {
		logger.log(Level.CONFIG, "accessKey : " + accessKey
				+ " :: secretKey : " + secretKey);
		if (proxy == null) {
			return new AmazonS3Client(new BasicAWSCredentials(accessKey,
					secretKey));
		} else {
			AmazonS3Client client = new AmazonS3Client(new BasicAWSCredentials(
					accessKey, secretKey),
					ClientConfigurer.getConfiguration(proxy));
			logger.info("Is client null " + (client == null));
			return client;
		}
	}
}
