package com.github.bordertech.config;

import java.util.Arrays;
import org.apache.commons.configuration.Configuration;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for {@link InitHelper}.
 */
public class InitHelperTest {

	private final String[] expectedResources = new String[]{
		"bordertech-defaults.properties",
		"bordertech-app.properties",
		"bordertech-local.properties",
		"bordertech-extra-first.properties",
		"bordertech-extra-second.properties"};

	private final String[] expectedBordertechResources = new String[]{
		"bordertech-defaults.properties",
		"bordertech-app.properties",
		"bordertech-local.properties"};

	@Test
	public void testResourceLoadOrder() {
		String[] resources = InitHelper.getDefaultResourceLoadOrder();
		Assert.assertTrue("Incorrect resource load order", Arrays.deepEquals(expectedResources, resources));
	}

	@Test
	public void testBordertechDefaultLoadOrder() {
		String[] resources = InitHelper.getDefaultBordertechLoadOrder();
		Assert.assertTrue("Incorrect bordertech resources", Arrays.deepEquals(expectedBordertechResources, resources));
	}

	@Test
	public void loadPropertyFileExists() {
		Configuration config = InitHelper.loadPropertyFile("bordertech-app.properties");
		Assert.assertNotNull("Config for file should not be null", config);
		Assert.assertEquals("Incorrect property value returned from config", "IN-APP", config.getString("test.in.app"));
	}

	@Test
	public void loadPropertyFileNotExists() {
		Configuration config = InitHelper.loadPropertyFile("NOT-EXIST.properties");
		Assert.assertNotNull("Config for not existing file should not be null", config);
		Assert.assertFalse("Config should be empty", config.getKeys().hasNext());
	}

	@Test(expected = IllegalStateException.class)
	public void loadPropertyFileInvalid() {
		InitHelper.loadPropertyFile("InvalidPropertiesFile.properties");
	}

}
