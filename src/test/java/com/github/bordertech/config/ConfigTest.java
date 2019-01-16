package com.github.bordertech.config;

import org.apache.commons.configuration.Configuration;
import org.junit.Assert;
import org.junit.Test;

/**
 * Ensure the Config class does what it says.
 *
 * @author Rick Brown
 */
public class ConfigTest {

	@Test
	public void testGetInstance() {
		Configuration expResult = Config.getInstance();
		Configuration result = Config.getInstance();
		Assert.assertSame("The singleton should return the same instance", expResult, result);
	}

	@Test
	public void testCopyConfiguration() {
		Configuration config = Config.getInstance();
		Configuration result = Config.copyConfiguration(config);
		Assert.assertNotSame("Copy should return a new instance", config, result);
	}

	@Test
	public void testCopyConfigurationWithProps() {
		Configuration config = Config.getInstance();
		final String expected = "kungfu";
		config.setProperty("kung.fu", expected);
		String actual = config.getString("kung.fu");
		Assert.assertEquals("Copy should maintain properties", expected, actual);
	}
}
