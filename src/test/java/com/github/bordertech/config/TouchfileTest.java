package com.github.bordertech.config;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.Assert;
import org.junit.Test;

/**
 * JUnit tests for {@link Touchfile}.
 */
public class TouchfileTest {

	private static final long DEFAULT_CHECK = 10;
	private static final long SLEEP_CHECK_INT = 20;
	private static final long FILE_UPDATE_INT = 1000;

	@Test()
	public void testConstructor() {
		long time = System.currentTimeMillis();
		Touchfile touch = new Touchfile("test", DEFAULT_CHECK);
		Assert.assertEquals("Invalid file name", "test", touch.getFilename());
		Assert.assertEquals("Invalid check interval for positive value", DEFAULT_CHECK, touch.getCheckInterval());
		Assert.assertTrue("Invalid last checked value", touch.getLastChecked() >= time);
		Assert.assertEquals("Invalid last modified for non existing file", 0, touch.getLastModified());
	}

	@Test()
	public void testConstructor2() {
		long time = System.currentTimeMillis();
		Touchfile touch = new Touchfile("test", -1);
		Assert.assertEquals("Invalid file name", "test", touch.getFilename());
		Assert.assertEquals("Invalid check interval for negative value", 0, touch.getCheckInterval());
		Assert.assertTrue("Invalid last checked value", touch.getLastChecked() >= time);
		Assert.assertEquals("Invalid last modified for non existing file", 0, touch.getLastModified());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorNullName() {
		new Touchfile(null, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testConstructorEmptyName() {
		new Touchfile("", 0);
	}

	@Test
	public void testFileNotExist() throws InterruptedException {
		Touchfile touch = new Touchfile("notexist.properties", DEFAULT_CHECK);
		Assert.assertFalse("File should not have changed", touch.hasChanged());
		Assert.assertEquals("Not existing file last modified should be zero", 0, touch.getLastModified());
		// Sleep longer than check interval
		long time = touch.getLastChecked();
		sleepThread(SLEEP_CHECK_INT);
		// Check not changed
		Assert.assertFalse("File should not have changed after interval", touch.hasChanged());
		Assert.assertTrue("Last checked value should have changed", touch.getLastChecked() > time);
	}

	@Test
	public void testFileExistsAndNoChange() throws IOException {
		File file = Files.createTempFile("testTouch", ".tmp").toFile();
		long modified = file.lastModified();
		try {
			String pathTemp = file.getAbsolutePath();
			Touchfile touch = new Touchfile(pathTemp, DEFAULT_CHECK);
			Assert.assertEquals("File last modified should match file", modified, touch.getLastModified());
			Assert.assertFalse("Existing file should not have changed", touch.hasChanged());
			// Sleep longer than check interval
			long time = touch.getLastChecked();
			sleepThread(SLEEP_CHECK_INT);
			// Check no change
			Assert.assertFalse("Existing file should not have changed after interval", touch.hasChanged());
			Assert.assertTrue("Last checked value should have changed", touch.getLastChecked() > time);
			Assert.assertEquals("File last modified should not have changed after interval", modified, touch.getLastModified());
		} finally {
			file.delete();
		}
	}

	@Test
	public void testFileChanges() throws IOException {
		File file = Files.createTempFile("testTouch2", ".tmp").toFile();
		long modified = file.lastModified();
		try {
			String pathTemp = file.getAbsolutePath();
			Touchfile touch = new Touchfile(pathTemp, DEFAULT_CHECK);
			Assert.assertFalse("Temp file should not have changed", touch.hasChanged());
			Assert.assertEquals("File last modified should match file", modified, touch.getLastModified());
			// Sleep longer than check interval
			long time = touch.getLastChecked();
			sleepThread(FILE_UPDATE_INT);
			// Update file
			try (FileWriter fw = new FileWriter(file, true)) {
				fw.write("Update content.");
			}
			if (modified == file.lastModified()) {
				Assert.fail("Touching the file did not change the last modified");
			}
			modified = file.lastModified();
			// Check has changed
			Assert.assertTrue("Updated file should have changed.", touch.hasChanged());
			Assert.assertTrue("Last checked value should have changed", touch.getLastChecked() > time);
			Assert.assertEquals("Updated file last modified should match file", modified, touch.getLastModified());
			// Make sure does not get flagged as changed again
			time = touch.getLastChecked();
			sleepThread(SLEEP_CHECK_INT);
			Assert.assertFalse("File should not be flagged as changed again.", touch.hasChanged());
			Assert.assertTrue("Last checked value should have changed", touch.getLastChecked() > time);
			Assert.assertEquals("File last modified should not change", modified, touch.getLastModified());
		} finally {
			file.delete();
		}
	}

	private void sleepThread(final long interval) {
		try {
			// Sleep thread long enough for the file modified timestamp to be different
			Thread.sleep(interval);
		} catch (InterruptedException e) {
			Thread.interrupted();
		}
	}

}
