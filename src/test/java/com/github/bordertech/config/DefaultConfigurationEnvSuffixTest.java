package com.github.bordertech.config;

import org.junit.Assert;
import org.junit.Test;

import static com.github.bordertech.config.DefaultConfiguration.ENVIRONMENT_PROPERTY;
import static com.github.bordertech.config.DefaultConfiguration.PROFILE_PROPERTY;

/**
 * DefaultConfiguration_EnvironmentSuffix_Test - JUnit tests for {@link DefaultConfiguration}.
 */
public class DefaultConfigurationEnvSuffixTest {

	private static final String TEST_PROPERTY_KEY = "default.propertyKey";
	private static final String DEFAULT_PROPERTY_VALUE = "defaultValue";
	private static final String SUFFIX1_PROPERTY_VALUE = "suffix1Value";
	private static final String SUFFIX2_PROPERTY_VALUE = "suffix2Value";

	@Test
	public void testEnvSuffixProperties() {
		DefaultConfiguration config = new DefaultConfiguration(
			"com/github/bordertech/config/DefaultConfigurationEnvSuffixTest.properties");

		System.clearProperty(ENVIRONMENT_PROPERTY);
		config.refresh();

		Assert.assertNull(System.getProperty(ENVIRONMENT_PROPERTY));

		//Test the default value
		assertPropertyEquals(config, DEFAULT_PROPERTY_VALUE);

		//Set the environment suffix 1 and reload
		System.setProperty(ENVIRONMENT_PROPERTY, "suffix1");
		config.refresh();

		//Test for the suffix 1 is set
		assertPropertyEquals(config, SUFFIX1_PROPERTY_VALUE);

		//Set the environment suffix 2 and reload
		System.setProperty(ENVIRONMENT_PROPERTY, "suffix2");
		config.refresh();

		//Test for the suffix 2 is set
		assertPropertyEquals(config, SUFFIX2_PROPERTY_VALUE);

		System.clearProperty(ENVIRONMENT_PROPERTY);
		config.refresh();
	}

	@Test
	public void testProfileSuffixProperties() {
		DefaultConfiguration config = new DefaultConfiguration(
			"com/github/bordertech/config/DefaultConfigurationEnvSuffixTest.properties");

		System.clearProperty(PROFILE_PROPERTY);
		config.refresh();

		Assert.assertNull(System.getProperty(PROFILE_PROPERTY));

		//Test the default value
		assertPropertyEquals(config, DEFAULT_PROPERTY_VALUE);

		//Set the environment suffix 1 and reload
		System.setProperty(PROFILE_PROPERTY, "suffix1");
		config.refresh();

		//Test for the suffix 1 is set
		assertPropertyEquals(config, SUFFIX1_PROPERTY_VALUE);

		//Set the environment suffix 2 and reload
		System.setProperty(PROFILE_PROPERTY, "suffix2");
		config.refresh();

		//Test for the suffix 2 is set
		assertPropertyEquals(config, SUFFIX2_PROPERTY_VALUE);

		System.clearProperty(PROFILE_PROPERTY);
		config.refresh();
	}

	/**
	 * Asserts that the configuration contains the given key/value.
	 *
	 * @param expected the expected property value.
	 */
	private void assertPropertyEquals(final DefaultConfiguration config, final Object expected) {
		Assert.assertEquals("Incorrect value for " + TEST_PROPERTY_KEY, expected, config.get(TEST_PROPERTY_KEY));
	}
}
