package com.github.bordertech.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * DefaultConfigurationDumpTest - JUnit tests for {@link DefaultConfiguration}.
 */
public class DefaultConfigurationDumpFileTest {

	private static final String LOG_START = "Properties loaded start";
	private static final String LOG_END = "Properties loaded end";
	private static final String LOG_PARAM = "simple.param1";
	private static final String LOG_FILE = "target/testdump.log";
	private static final String EXISTING_LOG = "ALREADY EXISTS";

	@Before
	@After
	public void deleteTempLog() {
		File temp = new File(LOG_FILE);
		if (temp.exists()) {
			temp.delete();
		}
	}

	@Test
	public void dumpParametersFileEnabled() {
		DefaultConfiguration config = new DefaultConfiguration(
				"com/github/bordertech/config/DefaultConfigurationTestDumpFileEnabled.properties");
		// Check dump file enabled
		Assert.assertEquals(LOG_FILE, config.get(DefaultConfiguration.DUMP_FILE));
		// Check log file exists
		Assert.assertTrue("Log file should exist", new File(LOG_FILE).exists());
		// Check content
		checkLogContent(getLogContent());
	}

	@Test
	public void dumpParametersFileEnabledAlreadyExists() {

		// Create an existing file
		File file = new File(LOG_FILE);
		try (FileOutputStream fos = new FileOutputStream(file); PrintStream stream = new PrintStream(fos)) {
			stream.println(EXISTING_LOG);
		} catch (IOException e) {
			throw new IllegalStateException("Could not create a temp file.", e);
		}

		DefaultConfiguration config = new DefaultConfiguration(
				"com/github/bordertech/config/DefaultConfigurationTestDumpFileEnabled.properties");
		// Check dump file enabled
		Assert.assertEquals(LOG_FILE, config.get(DefaultConfiguration.DUMP_FILE));
		// Check log file exists
		Assert.assertTrue("Log file should exist", file.exists());
		// Check content
		String log = getLogContent();
		checkLogContent(log);
		// Should have been overwritten
		Assert.assertFalse("Existing file should have been overwritten", log.contains(EXISTING_LOG));
	}

	@Test
	public void dumpParametersFileDisabled() {
		DefaultConfiguration config = new DefaultConfiguration(
				"com/github/bordertech/config/DefaultConfigurationTestDumpFileDisabled.properties");
		// Check dump file disabled
		Assert.assertEquals("", config.get(DefaultConfiguration.DUMP_FILE));
		// Log file should not exist
		Assert.assertFalse("Log file should not exist", new File(LOG_FILE).exists());
	}

	/**
	 * @return the log content
	 */
	private String getLogContent() {
		try {
			return new String(Files.readAllBytes(Paths.get(LOG_FILE)), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new IllegalStateException("Could not load temporary log content", e);
		}
	}

	/**
	 * @param log the log content to check
	 */
	private void checkLogContent(final String log) {
		Assert.assertTrue("Log should contain dump start load", log.contains(LOG_START));
		Assert.assertTrue("Log should contain dump start end", log.contains(LOG_END));
		Assert.assertTrue("Log should contain property", log.contains(LOG_PARAM));
	}

}
