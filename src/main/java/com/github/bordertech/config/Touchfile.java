package com.github.bordertech.config;

import java.io.File;

/**
 * Monitor a touchfile to check if it has changed.
 */
public class Touchfile {

	/**
	 * Touchfile name.
	 */
	private final String filename;

	/**
	 * The touch file.
	 */
	private final File file;

	/**
	 * Check interval in milli seconds.
	 */
	private final long checkInterval;

	/**
	 * The last time the file was checked.
	 */
	private long lastChecked;

	/**
	 * The files last modified time.
	 */
	private long lastModified;

	/**
	 * @param filename the touch file name
	 * @param checkInterval the interval to check the file in milli seconds
	 */
	public Touchfile(final String filename, final long checkInterval) {
		if (filename == null || filename.isEmpty()) {
			throw new IllegalArgumentException("A touch filename must be provided.");
		}
		this.filename = filename;
		this.file = new File(filename);
		this.checkInterval = checkInterval < 0 ? 0 : checkInterval;
		this.lastChecked = System.currentTimeMillis();
		this.lastModified = file.lastModified();
	}

	/**
	 *
	 * @return true if touch file has changed
	 */
	public boolean hasChanged() {

		// Has the check interval passed?
		long now = System.currentTimeMillis();
		if (now - lastChecked < checkInterval) {
			return false;
		}
		lastChecked = now;

		// Has the file changed?
		long modified = file.lastModified();
		if (lastModified == modified) {
			// No Change
			return false;
		}

		// Has changed so save new file details
		lastModified = modified;
		return true;
	}

	/**
	 * @return the file name
	 */
	public final String getFilename() {
		return filename;
	}

	/**
	 * @return the check interval
	 */
	public final long getCheckInterval() {
		return checkInterval;
	}

	/**
	 * @return the last checked time
	 */
	public final long getLastChecked() {
		return lastChecked;
	}

	/**
	 * @return the file last modified
	 */
	public final long getLastModified() {
		return lastModified;
	}

}
