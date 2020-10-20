package com.github.bordertech.config;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.awaitility.Awaitility;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

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
		config.setProperty("test.in.app", "expected");
		Configuration result = Config.copyConfiguration(config);
		Assert.assertNotSame("Copy should return a new instance", config, result);
		Assert.assertEquals(config.getString("test.in.app"), result.getString("test.in.app"));
	}

	@Test
	public void testCopyConfigurationWithProps() {
		Configuration config = Config.getInstance();
		final String expected = "kungfu";
		config.setProperty("kung.fu", expected);
		String actual = config.getString("kung.fu");
		Assert.assertEquals("Copy should maintain properties", expected, actual);

		PropertiesConfiguration props = new PropertiesConfiguration();
		props.setProperty("listProperty", Arrays.asList("one", "two"));

		Configuration copy = Config.copyConfiguration(props);

		Assert.assertEquals(props.getString("listProperty"), copy.getString("listProperty"));
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

	@Test
	public void testReset() {
		String key = "testResetKey";
		String value = "whoCares";

		Assert.assertNull(Config.getInstance().getString(key));

		Config.getInstance().addProperty(key, value);

		Assert.assertEquals(value, Config.getInstance().getString(key));

		Config.reset();

		Assert.assertNull(Config.getInstance().getString(key));
	}

	@Test
	public void testSetConfiguration() {
		DefaultConfiguration config = new DefaultConfiguration();
		Config.setConfiguration(config);
		Assert.assertEquals(config, Config.getInstance());
	}

	@Test
	public void testTouchFile() throws Exception {

		final String key = "test.in.defaults";
		final String value = "override";

		DefaultConfiguration config = new DefaultConfiguration();
		config.setProperty("bordertech.config.touchfile", "./parameters.touch");
		config.setProperty("bordertech.config.touchfile.interval", "2000");
		config.setProperty(key, "override");
		Config.setConfiguration(config);

		Assert.assertEquals(value, Config.getInstance().getString(key));

		FileUtils.touch(new File("./parameters.touch"));

		Awaitility.await().atMost(5, TimeUnit.SECONDS).until(() -> Config.getInstance().getString(key).equals("IN-DEFAULTS"));
	}

	@Test(expected = IllegalStateException.class)
	public void testConfigurationMissingDefaultClass() throws Exception {
		DefaultConfiguration config = new DefaultConfiguration();
		Config.setConfiguration(config);

		AccessInitHelper.overrideDefaultConfig("a.package.ClassNotExists", false);

		Config.reset();
		Assert.fail("IllegalStateException expected");
	}

	@Test
	public void testNotifyListeners() {
		String listenMsg = "propertyChangeHappened";
		final String[] listen = new String[1];
		Assert.assertNull(listen[0]);

		Config.notifyListeners();

		Assert.assertNull(listen[0]);

		Config.addPropertyChangeListener(evt -> listen[0] = listenMsg);

		Assert.assertNull(listen[0]);

		Config.notifyListeners();

		Assert.assertEquals(listenMsg, listen[0]);
	}

	@Test
	public void testGetInstanceSpiConfig() throws Exception {
		AccessInitHelper.overrideSpiEnabled(false, true);
		Assert.assertTrue(Config.getInstance() instanceof DefaultConfiguration);
	}

	@After
	public void tearDown() throws Exception {
		AccessInitHelper.reset();
	}
}
