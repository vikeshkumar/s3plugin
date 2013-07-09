package com.mindtree.maven;

import java.io.File;
import java.util.List;
import java.util.Properties;
import java.util.Map.Entry;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.mindtree.amazon.LocalProxy;

@Mojo(requiresOnline = true, name = "ccfd", threadSafe = false, requiresProject = true)
public class CloudFrontMojo extends AbstractMojo {

	@Parameter(defaultValue = "${project.basedir}")
	protected File root = null;
	private final Log logger = getLog();

	@Parameter(defaultValue = "${project}")
	private org.apache.maven.project.MavenProject project;

	public static final String S3_ENDPOINTS = "ENDPOINTS";

	public void execute() throws MojoExecutionException, MojoFailureException {
		logger.info("Root directory is: " + root.getAbsolutePath());
		Properties properties = project.getProperties();
		Object proxy = properties.get("localProxy");
		if (proxy instanceof LocalProxy) {
			logger.info("Got existing proxy");
		}
		// Create a ListDistributionRequest and get hold of all the informations
		// about distributions
		for (Entry<Object, Object> entry : project.getProperties().entrySet()) {
			logger.info("Key :" + entry.getKey().toString() + " :: Entry -> "
					+ entry.getValue());
		}
		Object endpointObjects = project.getProperties().get(S3_ENDPOINTS);

		if (endpointObjects instanceof List<?>) {
			try {
				List<String> endpoints = (List<String>) endpointObjects;
				for (String e : endpoints) {
					
				}
			} catch (ClassCastException cce) {
				logger.error(cce);
			}
		}

	}

}
