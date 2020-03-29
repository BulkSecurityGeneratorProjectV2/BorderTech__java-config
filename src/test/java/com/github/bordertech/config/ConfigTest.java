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

	@Test
	public void testResourceOrder() {
		Configuration config = Config.getInstance();
		Assert.assertEquals("Correct property value in defaults", "IN-DEFAULTS", config.getString("test.in.defaults"));
		Assert.assertEquals("Correct property value in app", "IN-APP", config.getString("test.in.app"));
		Assert.assertEquals("Correct property value in local", "IN-LOCAL", config.getString("test.in.local"));
		Assert.assertEquals("Correct property value in extra first", "IN-EXTRA-FIRST", config.getString("test.in.extra.first"));
		Assert.assertEquals("Correct property value in extra second", "IN-EXTRA-SECOND", config.getString("test.in.extra.second"));
	}

	@Test
	public void testResourceOrderOverrides() {
		Configuration config = Config.getInstance();
		Assert.assertEquals("Correct property override value in defaults", "DEFAULTS-def", config.getString("test.override.defaults"));
		Assert.assertEquals("Correct property override value in app", "APP-app", config.getString("test.override.app"));
		Assert.assertEquals("Correct property override value in local", "LOCAL-local", config.getString("test.override.local"));
		Assert.assertEquals("Correct property override value in extra first", "EXTRA-FIRST-extra-1", config.getString("test.override.extra.first"));
		Assert.assertEquals("Correct property override value in extra second", "EXTRA-SECOND-extra-2", config.getString("test.override.extra.second"));
	}

}
