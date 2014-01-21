package com.mindtree.amazon.s3;

import java.io.File;
import java.io.FileFilter;
import java.util.logging.Logger;

import org.apache.shiro.util.AntPathMatcher;

public class AntStyleFileFilter extends AntPathMatcher implements FileFilter {
	private String pattern;
	private static final String FILE_SEPARATOR = System
			.getProperty("file.separator");
	private final Logger logger = Logger.getLogger(AntStyleFileFilter.class
			.getCanonicalName());

	/**
	 * @param pathStyle
	 */
	public AntStyleFileFilter(String pathStyle) {
		this.pattern = pathStyle;
		/*
		 * If file separator on current system is not the default one then set
		 * the path separator as current system's path separator. Also change
		 * the pattern so that "\" is replaced by current path's path separator.
		 */
		if (!FILE_SEPARATOR.equals(DEFAULT_PATH_SEPARATOR)) {
			System.out.println("File separator set to \\");
			setPathSeparator(FILE_SEPARATOR);
			pattern = pattern.replace("/", FILE_SEPARATOR);
		} else {
			setPathSeparator(DEFAULT_PATH_SEPARATOR);
		}
	}

	/**
	 * Will return true if the pathname matches the given pattern.
	 */
	public boolean accept(File pathname) {
		String path = pathname.getAbsolutePath();
		if (path.startsWith("/") && !pattern.startsWith("/")) {
			path = path.substring(1);
		}
		boolean match = match(pattern, path);
		System.out.println("Path style :" + pattern + " :: File -> "
				+ pathname.getAbsolutePath() + " :: matches -> " + match);
		return match;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((pattern == null) ? 0 : pattern.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AntStyleFileFilter other = (AntStyleFileFilter) obj;
		if (pattern == null) {
			if (other.pattern != null)
				return false;
		} else if (!pattern.equals(other.pattern))
			return false;
		return true;
	}

	public String toString() {
		return pattern;
	}

}
