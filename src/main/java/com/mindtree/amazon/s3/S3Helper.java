package com.mindtree.amazon.s3;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.Owner;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.Region;

public class S3Helper {
	private static final Logger logger = Logger.getLogger(S3Helper.class
			.toString());

	/**
	 * @param s3
	 *            {@link AmazonS3}
	 * @param bucketName
	 *            Name of Bucket, should be unique across all amazon s3
	 * @param region
	 *            {@link Region}
	 * @return Bucket created. Tries creating a bucket.
	 */
	public static Bucket createBucket(AmazonS3 s3, String bucketName,
			Region region) {
		try {
			return s3.createBucket(bucketName, region);
		} catch (AmazonServiceException e) {
			logger.log(Level.SEVERE, e.toString());
			throw e;
		} catch (AmazonClientException e) {
			logger.log(Level.SEVERE, e.toString());
			throw e;
		}
	}

	/**
	 * @param s3
	 * @param bucketName
	 * @return Bucket created. Tries creating a bucket in Default S3 location as
	 *         mentioned in {@link: com.amazonaws.services.s3.model.Region}
	 */
	public static Bucket createBucket(AmazonS3 s3, String bucketName) {
		return createBucket(s3, bucketName, Region.US_Standard);
	}

	/**
	 * 
	 * @param s3
	 * @param prefix
	 * @param postfix
	 * @param region
	 * @return Bucket after creating a uniqueBucket, adding prefix and postfix
	 *         supplied. If prefix or postfix is null, then "" is prepended and
	 *         appended. Region is default {@link Region}
	 */

	public static Bucket createUniqueBucket(AmazonS3 s3, String prefix,
			String postfix, Region region) {
		String randomUUID = UUID.randomUUID().toString();
		System.out.println("UUID generated : " + randomUUID + " of length: "
				+ randomUUID.length());
		String bucketName = prefix == null ? ""
				: prefix + randomUUID + postfix == null ? "" : postfix;
		return createBucket(s3, bucketName, region);
	}

	/**
	 * 
	 * @param s3
	 * @param prefix
	 * @param postfix
	 * @return Bucket after creating a uniqueBucket, adding prefix and postfix
	 *         supplied. If prefix or postfix is null, then "" is prepended and
	 *         appended
	 */
	public static Bucket createUniqueBucket(AmazonS3 s3, String prefix,
			String postfix) {
		return createUniqueBucket(s3, prefix, postfix, Region.US_Standard);
	}

	public static List<Bucket> getBuckets(AmazonS3 s3) {
		return s3.listBuckets();
	}

	/**
	 * @param file
	 * @param bucketPath
	 *            The path includes bucket name the subfolder name. Assumption
	 *            is that if it is required to upload file to
	 *            bucketA/somePath/xyzPath, then somePath/xyzPath will be the
	 *            key to file
	 * @param s3
	 * @return
	 */
	public static PutObjectResult uploadFile(File file, String bucketPath,
			AmazonS3 s3, CannedAccessControlList cacl) {
		String key = bucketPath;
		String bucketName = bucketPath;
		try {
			if (bucketPath.indexOf("/") != -1) {
				int indexOfSep = bucketPath.indexOf("/");
				key = bucketPath.substring(indexOfSep + 1) + "/"
						+ file.getName();
				bucketName = bucketPath.substring(0, indexOfSep);
			} else {
				key = file.getName();
			}
			PutObjectRequest putRequest = new PutObjectRequest(bucketName, key,
					file).withCannedAcl(CannedAccessControlList.PublicRead);
			System.out.println("Uploading file " + file.getAbsolutePath()
					+ " to " + bucketName + " :: key is :: " + key);
			PutObjectResult result = s3.putObject(putRequest);
			System.out.println("Result of upload : " + result.getETag());
			return result;
		} catch (AmazonServiceException e) {
			logger.log(Level.SEVERE, e.toString());
			throw e;
		} catch (AmazonClientException e) {
			logger.log(Level.SEVERE, e.toString());
			throw e;
		}
	}

	public static List<PutObjectResult> uploadFiles(List<File> files,
			String path, AmazonS3 s3, CannedAccessControlList cacl) {
		List<PutObjectResult> por = new ArrayList<PutObjectResult>();
		if (files != null) {
			for (File f : files) {
				por.add(uploadFile(f, path, s3, cacl));
			}
		} else {
			System.out.println("There was no file to upload");
		}
		return por;
	}

	public static List<PutObjectResult> uploadFiles(List<File> fileList,
			String bucketPath, AmazonS3 s3, CannedAccessControlList cacl,
			File root) {
		List<PutObjectResult> por = new ArrayList<PutObjectResult>();
		int prefixIndex = bucketPath.indexOf("/");
		String bucketName = bucketPath.substring(0, prefixIndex);
		for (File f : fileList) {
			String pathOnDisk = f.getAbsolutePath();
			pathOnDisk = pathOnDisk.replace('\\', '/');
			String rootDir = root.getAbsolutePath().replace('\\', '/');
			String key = pathOnDisk.substring(rootDir.length() + 1);
			if (bucketPath.indexOf('/') != -1) {
				key = bucketPath.substring(bucketPath.indexOf('/') + 1) + "/"
						+ key;
			}
			PutObjectResult result = s3.putObject(bucketName, key, f);
			logger.info("Uploading file: -" + f.getAbsolutePath() + "- to "
					+ bucketName + " with key :: " + key);
			por.add(result);
		}
		return por;
	}
}
