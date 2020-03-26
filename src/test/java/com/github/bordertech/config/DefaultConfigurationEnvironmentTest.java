package com.github.bordertech.config;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test {@link DefaultConfiguration} for environment variables.
 */
public class DefaultConfigurationEnvironmentTest {

	@Test
	public void testEnvironmentVariablesDefault() {
		DefaultConfiguration config = new DefaultConfiguration(
				"com/github/bordertech/config/DefaultConfigurationTestLoadEnvDefault.properties");
		// Check disabled
		Assert.assertFalse("Use environment variables should defualt to false", config.getBoolean(DefaultConfiguration.USE_OSENV_PROPERTIES));
		// If no environment variables cant do anything
		if (!System.getenv().keySet().isEmpty()) {
			// Get a environment variable
			String key = System.getenv().keySet().iterator().next();
			Assert.assertFalse("Environment variables should not be in config", config.containsKey(key));
		}
	}

	@Test
	public void testEnvironmentVariablesEnabled() {
		DefaultConfiguration config = new DefaultConfiguration(
				"com/github/bordertech/config/DefaultConfigurationTestLoadEnvEnabled.properties");
		// Check enabled
		Assert.assertTrue("Use environment variables should be enabled", config.getBoolean(DefaultConfiguration.USE_OSENV_PROPERTIES));
		// If no environment variables cant do anything
		if (!System.getenv().keySet().isEmpty()) {
			// Get a environment variable
			String key = System.getenv().keySet().iterator().next();
			Assert.assertTrue("Environment variables should be in config", config.containsKey(key));
		}
	}

}
