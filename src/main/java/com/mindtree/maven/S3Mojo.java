package com.mindtree.maven;

/*
 * Copyright Mindtree Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.Region;
import com.mindtree.amazon.LocalProxy;
import com.mindtree.amazon.s3.AntStyleFileFilter;
import com.mindtree.amazon.s3.S3ClientBuilder;
import com.mindtree.amazon.s3.S3Helper;

/**
 * Goal which touches a timestamp file.
 * 
 * @deprecated Don't use!
 */
@Mojo(requiresOnline = true, name = "s3upload", requiresProject = false, threadSafe = false)
public class S3Mojo extends AbstractMojo {
	private final String UNIQUE = "unique";
	private final Log logger = getLog();
	protected FileFilter[] fileFilters;
	protected Map<String, List<File>> mapFiles = new HashMap<String, List<File>>();
	public static final String FILE_SEPARATOR = System
			.getProperty("file.separator");
	protected final static String S3_DEFAULT_REGION = Region.US_West.toString();

	@Parameter(required = true)
	protected Properties buckets;

	@Parameter(required = true)
	private String accessKey = null;

	@Parameter(required = true)
	protected String secretKey = null;

	@Parameter(required = false)
	protected String bucketRegion = null;

	protected String[] pathStyles = null;

	protected String[] bucketNames = null;

	protected String[] accessControls = null;

	@Parameter(required = false, defaultValue = "")
	protected String bucketPrefix = null;

	@Parameter(required = false, defaultValue = "")
	protected String bucketSuffix = null;

	@Parameter(required = false)
	protected String proxyHost = null;

	@Parameter(required = false)
	protected String proxyDomain = null;

	@Parameter(required = false)
	protected int proxyPort = -1;

	@Parameter(required = false)
	protected String proxyUsername = null;

	@Parameter(required = false)
	protected String proxyWorkstation = null;

	@Parameter(required = false)
	protected String proxyPassword = null;

	@Parameter(required = false, defaultValue = "https")
	protected String protocol = null;

	@Parameter(defaultValue = "${project.basedir}")
	protected File root;

	@Parameter(defaultValue = "false", alias = "retain", required = false)
	protected boolean retainFolderStructure;

	private LocalProxy localProxy = null;
	private AmazonS3 s3 = null;
	protected final String DIST_CONFIG_FILE_NAME = "Distribution.properties";

	@Parameter(defaultValue = "${project}")
	private org.apache.maven.project.MavenProject project;

	private Set<String> bucketSet = new HashSet<String>();

	public static final String S3_SUFFIX = ".s3.amazonaws.com";

	/**
	 * This will scan for all the files with a given pattern in project's
	 * directory and then simply list it for now. If successfully done, it will
	 * upload the files to amazonS3.
	 */
	public void execute() throws MojoExecutionException {
		logger.info("Executing goal " + ROLE);
		Set<Entry<Object, Object>> entries = buckets.entrySet();
		accessControls = new String[entries.size()];
		fileFilters = new AntStyleFileFilter[entries.size()];
		pathStyles = new String[entries.size()];
		bucketNames = new String[entries.size()];
		Object[] array = entries.toArray();
		for (int i = 0; i < array.length; i++) {
			Entry<Object, Object> e = (Entry<Object, Object>) array[i];
			String key = (String) e.getKey();
			String value = (String) e.getValue();
			String[] values = value.split(";");
			String pathStyle = value;
			if (values.length > 1) {
				pathStyles[i] = values[0];
				accessControls[i] = values[1];
			} else if (pathStyle.endsWith(";")) {
				logger.info("Replacing ; with nothing");
				pathStyles[i] = pathStyle.replaceAll(";", "");
			}
			bucketNames[i] = key;
			logger.info("" + key + " :: " + pathStyles[i] + " :: "
					+ accessControls[i]);
			logger.info("Root is null : " + (root == null)
					+ " :: Location is -> " + root.getAbsolutePath());
		}
		for (int i = 0; i < pathStyles.length; i++) {
			fileFilters[i] = new AntStyleFileFilter(pathStyles[i]);
		}

		// Create proxy to be used
		if (proxyHost != null) {
			localProxy = new LocalProxy(proxyHost, proxyDomain, proxyPort,
					proxyUsername, proxyPassword, proxyWorkstation, protocol);
		}

		logger.info("Created proxy.  Is proxy Null :" + (localProxy == null));
		// Create AmazonS3Client
		s3 = S3ClientBuilder.getClient(accessKey, secretKey, localProxy);

		logger.info("AmazonS3Client is null : " + (s3 == null));

		// Create list of file to be uploaded that matches path style

		// TODO: Uncomment following
		logger.debug("Filtering files ");
		filterFiles(root);
		logger.debug("Filet filter styles::");
		for (FileFilter f : fileFilters) {
			logger.debug("\t" + f.toString());
		}

		// Create and upload files
		logger.info("Starting Upload");

		// TODO: Uncomment following
		createBucketAndUploadFiles();
		logger.info("Created bucket and uploaded file");

		// Getting properties earlier give a null.
		if (localProxy != null) {
			project.getProperties().put(LocalProxy.LOCAL_PROXY, localProxy);
		}
	}

	/**
	 * @param projectDirectory
	 *            Recursively scans files in project directory, and adds files
	 *            matching paxml thStyle to upload map
	 */
	private void filterFiles(File projectDirectory) {
		for (File file : projectDirectory.listFiles()) {
			if (!file.isDirectory()) {
				addToUploadMap(file);
			} else {
				filterFiles(file);
			}
		}
	}

	/**
	 * @param file
	 *            Iterates through all the FileFilters and add the file to
	 *            fileUpload list if name of file matches the path style of
	 *            fileFilter
	 */
	private void addToUploadMap(File file) {
		int i = 0;
		String tempPathName = bucketNames[i];
		for (FileFilter f : fileFilters) {
			if (f.accept(file)) {
				logger.debug("\tAccepted");
				if (mapFiles.containsKey(tempPathName)) {
					logger.debug("Added to already existing map"
							+ file.getAbsolutePath() + " to map : "
							+ tempPathName);
					mapFiles.get(tempPathName).add(file);
				} else {
					List<File> files = new ArrayList<File>();
					files.add(file);
					mapFiles.put(tempPathName, files);
					logger.debug("Create and Added " + file.getAbsolutePath()
							+ " to map : " + tempPathName);
				}
			}
			++i;
		}
	}

	private void createBucketAndUploadFiles() throws MojoExecutionException {
		for (int i = 0; i < bucketNames.length; i++) {
			String path = bucketNames[i];
			int index = path.indexOf("/");
			logger.debug("Got index of / : " + index);
			logger.debug("Trying upload for files with path : " + path);
			// The path to upload can have subdirectories hence only the first
			// directory (root) is required
			String rootBucket = path;
			if (index == 0) {
				logger.debug("Unique name bucket creation is required");
				rootBucket = UNIQUE;
			} else if (index != -1) {
				logger.debug("Given name bucket creation is required");
				rootBucket = rootBucket.substring(0, index);
			}
			Bucket bucket = createS3Bucket(rootBucket);
			if (bucket != null) {
				List<File> fileList = mapFiles.get(path);
				logger.debug("Got fileList as null :: " + (fileList == null));
				if (fileList != null) {
					logger.debug("Size of fileList :" + fileList.size());
				}
				CannedAccessControlList cacl = CannedAccessControlList.Private;
				if (accessControls[i]
						.equalsIgnoreCase(CannedAccessControlList.AuthenticatedRead
								.toString())) {
					cacl = CannedAccessControlList.AuthenticatedRead;
				} else if (accessControls[i]
						.equalsIgnoreCase(CannedAccessControlList.BucketOwnerFullControl
								.toString())) {
					cacl = CannedAccessControlList.BucketOwnerFullControl;
				} else if (accessControls[i]
						.equalsIgnoreCase(CannedAccessControlList.BucketOwnerRead
								.toString())) {
					cacl = CannedAccessControlList.BucketOwnerRead;
				} else if (accessControls[i]
						.equalsIgnoreCase(CannedAccessControlList.LogDeliveryWrite
								.toString())) {
					cacl = CannedAccessControlList.LogDeliveryWrite;
				} else if (accessControls[i]
						.equalsIgnoreCase(CannedAccessControlList.Private
								.toString())) {
					cacl = CannedAccessControlList.Private;
				} else if (accessControls[i]
						.equalsIgnoreCase(CannedAccessControlList.PublicRead
								.toString())) {
					cacl = CannedAccessControlList.PublicRead;
				} else if (accessControls[i]
						.equalsIgnoreCase(CannedAccessControlList.PublicReadWrite
								.toString())) {
					cacl = CannedAccessControlList.PublicReadWrite;
				}
				String bucketName = bucket.getName()
						+ bucketNames[i].substring(bucketNames[i].indexOf("/"));
				logger.debug("File to upload to :" + bucketName);
				if (fileList != null && fileList.size() > 0) {
					if (!retainFolderStructure) {
						logger.debug("Not retaining folder structure and uploadinf files");
						System.out.println(cacl.toString());
						List<PutObjectResult> fileUploadResults = S3Helper
								.uploadFiles(fileList, bucketName, s3, cacl);
					} else {
						logger.debug("Uploading with retained dir structure");
						List<PutObjectResult> fileUploadResults = S3Helper
								.uploadFiles(fileList, bucketName, s3, cacl,
										root);
					}
				}
			}
		}
	}

	/**
	 * @param bucketName
	 * 
	 *            Tries to create bucket in amazon S3. Since bucket name has to
	 *            be unique, if the given bucket name is not available it aborts
	 */
	private Bucket createS3Bucket(String bucketName) {
		List<Bucket> buckets = S3Helper.getBuckets(s3);
		for (Bucket b : buckets) {
			if (b.getName().equals(bucketName)) {
				return b;
			}
		}
		if (bucketName.equalsIgnoreCase(UNIQUE)) {
			if (bucketRegion == null) {
				return S3Helper.createUniqueBucket(s3, bucketPrefix,
						bucketSuffix);
			} else {
				Region region = Region.fromValue(bucketRegion);
				return S3Helper.createUniqueBucket(s3, bucketPrefix,
						bucketSuffix, region);
			}
		} else {
			if (bucketRegion == null) {
				return S3Helper.createBucket(s3, bucketName);
			} else {
				Region region = Region.fromValue(bucketRegion);
				return S3Helper.createBucket(s3, bucketName, region);
			}
		}
	}
}
