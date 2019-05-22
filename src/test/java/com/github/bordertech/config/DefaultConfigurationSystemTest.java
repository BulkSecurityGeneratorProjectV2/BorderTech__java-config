package com.github.bordertech.config;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * DefaultConfiguration_Test - JUnit tests for {@link DefaultConfiguration}.
 */
public class DefaultConfigurationSystemTest {

	private static final String PROPERTY_EXISTS_KEY = "simple.exists";
	private static final String PROPERTY_EXISTS_OVERWRITE = "overwrite1";
	private static final String PROPERTY_NOTEXISTS_KEY = "simple.notexists";
	private static final String PROPERTY_NOTEXISTS_OVERWRITE = "overwrite2";
	private static final String PROPERTY_NOTEXISTS_DEFAULT = null;

	private static final String PREFIX_1_KEY = "test1.my.exists";
	private static final String PREFIX_1_OVERWRITE = "testoverwrite1";
	private static final String PREFIX_2_KEY = "test2.my.exists";
	private static final String PREFIX_2_OVERWRITE = "testoverwrite2";
	private static final String PREFIX_3_KEY = "test3.my.exists";
	private static final String PREFIX_3_DEFAULT = "testvalue3";
	private static final String PREFIX_3_OVERWRITE = "testoverwrite3";

	@BeforeClass
	public static void setupSystemProperties() {
		System.getProperties().setProperty(PROPERTY_EXISTS_KEY, PROPERTY_EXISTS_OVERWRITE);
		System.getProperties().setProperty(PROPERTY_NOTEXISTS_KEY, PROPERTY_NOTEXISTS_OVERWRITE);
		System.getProperties().setProperty(PREFIX_1_KEY, PREFIX_1_OVERWRITE);
		System.getProperties().setProperty(PREFIX_2_KEY, PREFIX_2_OVERWRITE);
		System.getProperties().setProperty(PREFIX_3_KEY, PREFIX_3_OVERWRITE);
	}

	@AfterClass
	public static void clearSystemProperties() {
		System.getProperties().remove(PROPERTY_EXISTS_KEY);
		System.getProperties().remove(PROPERTY_NOTEXISTS_KEY);
		System.getProperties().remove(PREFIX_1_KEY);
		System.getProperties().remove(PREFIX_2_KEY);
		System.getProperties().remove(PREFIX_3_KEY);
	}

	@Test
	public void loadSystemOverWriteOnly() {
		DefaultConfiguration config = new DefaultConfiguration(
				"com/github/bordertech/config/DefaultConfigurationTestLoadSystem.properties");
		// Check system settings in the test properties files
		Assert.assertTrue(config.getBoolean(DefaultConfiguration.USE_SYSTEM_PROPERTIES));
		// OverWriteOnly Enabled - ie using the default so no property set
		Assert.assertFalse(config.containsKey(DefaultConfiguration.USE_SYSTEM_OVERWRITEONLY));
		// No prefixes set
		Assert.assertFalse(config.containsKey(DefaultConfiguration.USE_SYSTEM_PREFIXES));
		// Check properties
		Assert.assertEquals("Existing property should have been overwritten", PROPERTY_EXISTS_OVERWRITE, config.get(PROPERTY_EXISTS_KEY));
		Assert.assertEquals("Not existing property should have not changed", PROPERTY_NOTEXISTS_DEFAULT, config.get(PROPERTY_NOTEXISTS_KEY));
	}

	@Test
	public void loadSystemOverWriteOnlyDisabled() {
		DefaultConfiguration config = new DefaultConfiguration(
				"com/github/bordertech/config/DefaultConfigurationTestLoadSystemOverWrite.properties");
		// Check system settings in the test properties files
		Assert.assertTrue(config.getBoolean(DefaultConfiguration.USE_SYSTEM_PROPERTIES));
		// OverWriteOnly disabled
		Assert.assertFalse(config.getBoolean(DefaultConfiguration.USE_SYSTEM_OVERWRITEONLY));
		// No prefixes set
		Assert.assertFalse(config.containsKey(DefaultConfiguration.USE_SYSTEM_PREFIXES));
		// Check properties
		Assert.assertEquals("Existing property should have been overwritten", PROPERTY_EXISTS_OVERWRITE, config.get(PROPERTY_EXISTS_KEY));
		Assert.assertEquals("Not existing property should have been overwritten", PROPERTY_NOTEXISTS_OVERWRITE, config.get(PROPERTY_NOTEXISTS_KEY));
	}

	@Test
	public void loadSystemPrefixes() {
		DefaultConfiguration config = new DefaultConfiguration(
				"com/github/bordertech/config/DefaultConfigurationTestLoadSystemPrefixes.properties");
		// Check system settings in the test properties files
		Assert.assertTrue(config.getBoolean(DefaultConfiguration.USE_SYSTEM_PROPERTIES));
		// OverWriteOnly Enabled - ie using the default so no property set
		Assert.assertFalse(config.containsKey(DefaultConfiguration.USE_SYSTEM_OVERWRITEONLY));
		// Prefixes set
		Assert.assertFalse(config.getList(DefaultConfiguration.USE_SYSTEM_PREFIXES).isEmpty());
		// Check properties
		Assert.assertEquals("Test1 prefix should have been overwritten", PREFIX_1_OVERWRITE, config.get(PREFIX_1_KEY));
		Assert.assertEquals("Test2 prefix should have been overwritten", PREFIX_2_OVERWRITE, config.get(PREFIX_2_KEY));
		Assert.assertEquals("Test3 prefix should not have been overwritten", PREFIX_3_DEFAULT, config.get(PREFIX_3_KEY));
	}

}
