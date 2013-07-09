package com.mindtree.amazon.cloudfront;

import java.util.Collection;

import com.amazonaws.services.cloudfront.AmazonCloudFrontClient;
import com.amazonaws.services.cloudfront.model.CreateDistributionRequest;
import com.amazonaws.services.cloudfront.model.CreateDistributionResult;
import com.amazonaws.services.cloudfront.model.DistributionConfig;
import com.amazonaws.services.cloudfront.model.Origin;
import com.amazonaws.services.cloudfront.model.Origins;

public class CloudFrontHelper {
	public static CreateDistributionResult createDistribution(
			AmazonCloudFrontClient client, CreateDistributionRequest request) {
		return client.createDistribution(request);
	}

	public static CreateDistributionRequest createDistributionRequest(
			DistributionConfig config) {
		return new CreateDistributionRequest(config);
	}

	public static DistributionConfig createDistributionConfig(Origins origins) {
		DistributionConfig config = new DistributionConfig();
		config.setOrigins(origins);
		return config;
	}

	public static Origins createOrigins(Collection<Origin> items) {
		Origins origins = new Origins();
		origins.setItems(items);
		return origins;
	}
}
