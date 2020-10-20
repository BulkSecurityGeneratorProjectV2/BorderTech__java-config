package com.github.bordertech.config;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConversionException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import static com.github.bordertech.config.DefaultConfiguration.ENVIRONMENT_PROPERTY;

/**
 * DefaultConfiguration_Test - JUnit tests for {@link DefaultConfiguration}.
 */
public class DefaultConfigurationTest {

	/**
	 * The configuration to test.
	 */
	private DefaultConfiguration config;

	/**
	 * A value for this property should not exist.
	 */
	private static final String MISSING_PROPERTY_KEY = "simple.nonExistantProperty";

	/**
	 * Used in conjunction with MISSING_PROPERTY_KEY.
	 */
	private static final int MISSING_PROPERTY_VAL = 234;

	/**
	 * The value for this property should be an empty string.
	 */
	private static final String EMPTY_PROPERTY_KEY = "simple.emptyPropertyKey";

	/**
	 * The value for this property should be "simplePropertyValue".
	 */
	private static final String STRING_PROPERTY_KEY = "simple.stringPropertyKey";

	/**
	 * The value for this property should be INT_PROPERTY_VAL.
	 */
	private static final String INT_PROPERTY_KEY = "simple.intPropertyKey";

	/**
	 * Used in conjunction with INT_PROPERTY_KEY.
	 */
	private static final int INT_PROPERTY_VAL = 123;

	/**
	 * The value for this property should be "true".
	 */
	private static final String BOOLEAN_TRUE_PROPERTY_KEY = "simple.booleanTruePropertyKey";

	/**
	 * The value for this property should be "false".
	 */
	private static final String BOOLEAN_FALSE_PROPERTY_KEY = "simple.booleanFalsePropertyKey";

	/**
	 * The value for this property should be "simplePropertyValue".
	 */
	private static final String ENV_SUFFIX_PROPERTY_KEY = "envSuffix.propertyKey";

	@Before
	public void loadProperties() {
		config = new DefaultConfiguration(
				"com/github/bordertech/config/DefaultConfigurationTest.properties");
	}

	@Test
	public void testSimpleProperties() {
		Assert.assertNull("Missing properties should be null", config.get(
			"simple.nonExistantPropertyKey"));
		Assert.assertEquals("Incorrect default value for missing property", "defaultValue", config.
			getString("simple.nonExistantPropertyKey", "defaultValue"));
		assertPropertyEquals(EMPTY_PROPERTY_KEY, "");
		assertPropertyEquals(STRING_PROPERTY_KEY, "simplePropertyValue");
		assertPropertyEquals(INT_PROPERTY_KEY, "123");
		assertPropertyEquals(BOOLEAN_TRUE_PROPERTY_KEY, "true");
		assertPropertyEquals(BOOLEAN_FALSE_PROPERTY_KEY, "false");
		assertPropertyEquals("simple.listPropertyKey", "item1,item2,item3");
		assertPropertyEquals("test.plus.equals", "first,second");
	}

	@Test
	public void testSubstitution() {
		assertPropertyEquals("substitute.missingKey", "${substitute.nonExistantKey}");
		assertPropertyEquals("substitute.part1And2Key", "part1Value+part2Value");
		assertPropertyEquals("substitute.part1And2And3Key", "part1Value+part2Value+part3Value");
		assertPropertyEquals("substitute.combinedKey", "multiPart1ValuemultiPart2Value");
		assertPropertyEquals("substitute.reurse", "${substitute.recurse}");
	}

	@Test
	public void testIncludes() {
		assertPropertyEquals("test.definedBeforeInclude", "includeValue");
		assertPropertyEquals("test.definedAfterInclude", "mainValue");
		assertPropertyEquals("test.definedBeforeIncludeAfter", "includeAfterValue");
		assertPropertyEquals("test.definedAfterIncludeAfter", "includeAfterValue");
	}

	@Test
	public void testGetSubProperties() {
		final int propertyCount = 7;
		// Test without the prefix truncated
		Properties props = config.getSubProperties("simple.", false);
		Assert.assertEquals("Incorrect number of properties", propertyCount, props.size());
		assertPropertyEquals(EMPTY_PROPERTY_KEY, "", props);
		assertPropertyEquals(STRING_PROPERTY_KEY, "simplePropertyValue", props);
		assertPropertyEquals(INT_PROPERTY_KEY, "123", props);
		assertPropertyEquals(BOOLEAN_TRUE_PROPERTY_KEY, "true", props);
		assertPropertyEquals(BOOLEAN_FALSE_PROPERTY_KEY, "false", props);
		assertPropertyEquals("simple.listPropertyKey", "item1,item2,item3", props);
		assertPropertyEquals("simple.propertiesPropertyKey", "key1=value1,key2=value2,key3=value3",
				props);

		// Now test with the prefix truncated
		props = config.getSubProperties("simple.", true);
		Assert.assertEquals("Incorrect number of properties", propertyCount, props.size());
		assertPropertyEquals("emptyPropertyKey", "", props);
		assertPropertyEquals("stringPropertyKey", "simplePropertyValue", props);
		assertPropertyEquals("intPropertyKey", "123", props);
		assertPropertyEquals("booleanTruePropertyKey", "true", props);
		assertPropertyEquals("booleanFalsePropertyKey", "false", props);
		assertPropertyEquals("listPropertyKey", "item1,item2,item3", props);
		assertPropertyEquals("propertiesPropertyKey", "key1=value1,key2=value2,key3=value3", props);
	}

	@Test
	public void testSetProperty() {
		assertPropertyEquals(STRING_PROPERTY_KEY, "simplePropertyValue");
		config.setProperty(STRING_PROPERTY_KEY, "changedValue");
		assertPropertyEquals(STRING_PROPERTY_KEY, "changedValue");
	}

	@Test
	public void testAddProperty() {
		assertPropertyEquals(STRING_PROPERTY_KEY, "simplePropertyValue");
		config.addProperty(STRING_PROPERTY_KEY, "addedValue");
		assertPropertyEquals(STRING_PROPERTY_KEY, "simplePropertyValue,addedValue");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddPropertyNullKey() {
		config.addProperty(null, "addedValue");
		Assert.fail("IllegalArgumentException expected for null key");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddPropertyEmptyKey() {
		config.addProperty("", "addedValue");
		Assert.fail("IllegalArgumentException expected for empty key");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddPropertyNullValue() {
		config.addProperty("A_DIFFERENT_KEY", null);
		Assert.fail("IllegalArgumentException expected for null value");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetPropertyNullKey() {
		config.setProperty(null, "x");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetPropertyEmptyKey() {
		config.setProperty("", "x");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetPropertyNullValue() {
		config.setProperty("x", null);
	}

	@Test
	public void testGetLong() {
		Assert.assertEquals("Incorrect long value for " + INT_PROPERTY_KEY, INT_PROPERTY_VAL, config.getLong(INT_PROPERTY_KEY));

		Assert.assertEquals("Incorrect long value for missing key", 0, config.getLong(MISSING_PROPERTY_KEY));

		Assert.assertEquals("Incorrect default long value for missing key", MISSING_PROPERTY_VAL,
				config.getLong(MISSING_PROPERTY_KEY, MISSING_PROPERTY_VAL));

		Assert.assertEquals("Incorrect default long value for missing key",
			Long.valueOf(MISSING_PROPERTY_VAL), config.getLong(MISSING_PROPERTY_KEY, Long.valueOf(MISSING_PROPERTY_VAL)));
	}

	@Test(expected = ConversionException.class)
	public void testGetInvalidLong() {
		config.getLong(STRING_PROPERTY_KEY);
	}

	@Test(expected = ConversionException.class)
	public void testGetInvalidLongObject() {
		config.getLong(STRING_PROPERTY_KEY, Long.valueOf("1"));
	}

	@Test
	public void testGetInt() {
		Assert.assertEquals("Incorrect int value for " + INT_PROPERTY_KEY, INT_PROPERTY_VAL, config.getInt(INT_PROPERTY_KEY));

		Assert.assertEquals("Incorrect int value for missing key", 0, config.getInt(MISSING_PROPERTY_KEY));

		Assert.assertEquals("Incorrect default int value for missing key", MISSING_PROPERTY_VAL,
			config.getInt(MISSING_PROPERTY_KEY, MISSING_PROPERTY_VAL));

		Assert.assertEquals("Incorrect default integer value for missing key",
			Integer.valueOf(MISSING_PROPERTY_VAL), config.getInteger(MISSING_PROPERTY_KEY, MISSING_PROPERTY_VAL));
	}

	@Test(expected = ConversionException.class)
	public void testGetInvalidInt() {
		config.getInt(STRING_PROPERTY_KEY);
	}

	@Test(expected = ConversionException.class)
	public void testGetInvalidIntObject() {
		config.getInt(STRING_PROPERTY_KEY, new Integer("1"));
	}

	@Test
	public void testGetShort() {
		Assert.assertEquals("Incorrect short value for " + INT_PROPERTY_KEY, INT_PROPERTY_VAL, config.getShort(INT_PROPERTY_KEY));

		Assert.assertEquals("Incorrect short value for missing key", 0, config.getShort(MISSING_PROPERTY_KEY));

		Assert.assertEquals("Incorrect default short value for missing key", MISSING_PROPERTY_VAL, config.getShort(MISSING_PROPERTY_KEY,
			(short) MISSING_PROPERTY_VAL));

		Assert.assertEquals("Incorrect default short value for missing key",
			Short.valueOf((short) MISSING_PROPERTY_VAL), config.getShort(MISSING_PROPERTY_KEY, Short.valueOf((short) MISSING_PROPERTY_VAL)));
	}

	@Test(expected = ConversionException.class)
	public void testGetInvalidShort() {
		config.getShort(STRING_PROPERTY_KEY);
	}

	@Test(expected = ConversionException.class)
	public void testGetInvalidShortObject() {
		config.getShort(STRING_PROPERTY_KEY, Short.valueOf("1"));
	}

	@Test
	public void testGetByte() {
		final int expectedVal = 111;
		Assert.assertEquals("Incorrect byte value for " + INT_PROPERTY_KEY, INT_PROPERTY_VAL, config.getByte(INT_PROPERTY_KEY));

		Assert.assertEquals("Incorrect byte value for missing key", 0, config.getByte(MISSING_PROPERTY_KEY));

		Assert.assertEquals("Incorrect default byte value for missing key", expectedVal, config.getByte(MISSING_PROPERTY_KEY, (byte) expectedVal));

		Assert.assertEquals("Incorrect default byte value for missing key",
			Byte.valueOf((byte) expectedVal), config.getByte(MISSING_PROPERTY_KEY, Byte.valueOf(
				(byte) expectedVal)));
	}

	@Test(expected = ConversionException.class)
	public void testGetInvalidByte() {
		config.getByte(STRING_PROPERTY_KEY);
	}

	@Test(expected = ConversionException.class)
	public void testGetInvalidByteObject() {
		config.getByte(STRING_PROPERTY_KEY, Byte.valueOf("1"));
	}

	@Test
	public void testGetBigDecimal() {
		Assert.assertEquals("Incorrect BigDecimal value for " + INT_PROPERTY_KEY,
			BigDecimal.valueOf(INT_PROPERTY_VAL), config.getBigDecimal(INT_PROPERTY_KEY));

		Assert.assertEquals("Incorrect BigDecimal value for missing key",
			BigDecimal.valueOf(0.0), config.getBigDecimal(MISSING_PROPERTY_KEY));

		Assert.assertEquals("Incorrect default BigDecimal value for missing key",
				BigDecimal.valueOf(MISSING_PROPERTY_VAL), config.getBigDecimal(MISSING_PROPERTY_KEY, BigDecimal.
				valueOf(MISSING_PROPERTY_VAL)));
	}

	@Test(expected = ConversionException.class)
	public void testGetInvalidBigDecimal() {
		config.getBigDecimal(STRING_PROPERTY_KEY);
	}

	@Test
	public void testGetBigInteger() {
		Assert.assertEquals("Incorrect BigInteger value for " + INT_PROPERTY_KEY,
				BigInteger.valueOf(INT_PROPERTY_VAL), config.getBigInteger(INT_PROPERTY_KEY));

		Assert.assertEquals("Incorrect BigInteger value for missing key",
				BigInteger.valueOf(0), config.getBigInteger(MISSING_PROPERTY_KEY));

		Assert.assertEquals("Incorrect default BigInteger value for missing key",
				BigInteger.valueOf(MISSING_PROPERTY_VAL), config.getBigInteger(MISSING_PROPERTY_KEY, BigInteger.
				valueOf(MISSING_PROPERTY_VAL)));
	}

	@Test(expected = ConversionException.class)
	public void testGetInvalidBigInteger() {
		config.getBigInteger(STRING_PROPERTY_KEY);
	}

	@Test
	public void testGetBoolean() {
		Assert.assertTrue("Incorrect boolean value for " + BOOLEAN_TRUE_PROPERTY_KEY, config.getBoolean(BOOLEAN_TRUE_PROPERTY_KEY));

		Assert.assertFalse("Incorrect boolean value for " + BOOLEAN_FALSE_PROPERTY_KEY, config.getBoolean(BOOLEAN_FALSE_PROPERTY_KEY));

		Assert.assertFalse("Incorrect boolean value for missing key", config.getBoolean(MISSING_PROPERTY_KEY));

		Assert.assertTrue("Incorrect default boolean value for missing key", config.getBoolean(MISSING_PROPERTY_KEY, true));

		Assert.assertTrue("Incorrect default boolean value for missing key", config.getBoolean(MISSING_PROPERTY_KEY, Boolean.TRUE));
	}

	@Test
	public void testGetFloat() {
		final float expectedVal = 234.0f;
		Assert.assertEquals("Incorrect float value for " + INT_PROPERTY_KEY,
				Float.parseFloat("123"), config.getFloat(INT_PROPERTY_KEY), 0.0);

		Assert.assertEquals("Incorrect float value for missing key",
				0.0f, config.getFloat(MISSING_PROPERTY_KEY), 0.0);

		Assert.assertEquals("Incorrect default float value for missing key",
				expectedVal, config.getFloat(MISSING_PROPERTY_KEY, expectedVal), 0.0);

		Assert.assertEquals("Incorrect default float value for missing key",
			Float.valueOf(MISSING_PROPERTY_VAL), config.getFloat(MISSING_PROPERTY_KEY, Float.valueOf(MISSING_PROPERTY_VAL)));
	}

	@Test(expected = ConversionException.class)
	public void testGetInvalidFloat() {
		config.getFloat(STRING_PROPERTY_KEY);
	}

	@Test(expected = ConversionException.class)
	public void testGetInvalidFloatObject() {
		config.getFloat(STRING_PROPERTY_KEY, Float.valueOf("0.0"));
	}

	@Test
	public void testGetDouble() {
		final double expectedVal = 234.0;
		Assert.assertEquals("Incorrect double value for " + INT_PROPERTY_KEY,
			Double.parseDouble("123"), config.getDouble(INT_PROPERTY_KEY), 0.0);

		Assert.assertEquals("Incorrect double value for missing key",
			0.0, config.getDouble(MISSING_PROPERTY_KEY), 0.0);

		Assert.assertEquals("Incorrect default double value for missing key",
				expectedVal, config.getDouble(MISSING_PROPERTY_KEY, MISSING_PROPERTY_VAL), 0.0);

		Assert.assertEquals("Incorrect default double value for missing key",
			Double.valueOf(MISSING_PROPERTY_VAL), config.getDouble(MISSING_PROPERTY_KEY, Double.valueOf(MISSING_PROPERTY_VAL)));
	}

	@Test(expected = ConversionException.class)
	public void testGetInvalidDouble() {
		config.getDouble(STRING_PROPERTY_KEY);
	}

	@Test(expected = ConversionException.class)
	public void testGetInvalidDoubleObject() {
		config.getDouble(STRING_PROPERTY_KEY, Double.valueOf("1.0"));
	}

	@Test
	public void testGetList() {
		Assert.assertEquals("Incorrect list value for " + STRING_PROPERTY_KEY,
			Collections.singletonList("simplePropertyValue"), config.getList(
				STRING_PROPERTY_KEY));

		Assert.assertEquals("Incorrect list value for simple.listPropertyKey",
			Arrays.asList("item1", "item2", "item3"), config.getList(
				"simple.listPropertyKey"));

		List<String> defaultList = Arrays.asList("default1", "default2");
		Assert.assertEquals("Incorrect default list value for missing key",
			defaultList, config.getList(MISSING_PROPERTY_KEY, defaultList));
	}

	@Test
	public void testGetProperties() {
		final int expectedProps = 3;
		Properties props = config.getProperties("simple.propertiesPropertyKey");
		Assert.assertEquals("Incorrect number of properties", expectedProps, props.size());
		assertPropertyEquals("key1", "value1", props);
		assertPropertyEquals("key2", "value2", props);
		assertPropertyEquals("key3", "value3", props);

		Properties props2 = config.getProperties();
		Assert.assertNotEquals(props, config.getProperties());
		Assert.assertEquals(props2, config.getProperties());
	}

	@Test
	public void testRefresh() {
		String listenMsg = "propertyChangeHappened";
		final String[] listen = new String[1];
		Assert.assertNull(listen[0]);

		Config.addPropertyChangeListener(evt -> listen[0] = listenMsg);

		String orig = "simplePropertyValue";
		String newValue = "newvalue";
		assertPropertyEquals(STRING_PROPERTY_KEY, orig);
		config.setProperty(STRING_PROPERTY_KEY, newValue);
		assertPropertyEquals(STRING_PROPERTY_KEY, newValue);
		config.refresh();
		assertPropertyEquals(STRING_PROPERTY_KEY, orig);

		Assert.assertEquals(listenMsg, listen[0]);
	}


	@Test
	public void testClear() {
		Assert.assertFalse(config.isEmpty());
		config.clear();
		Assert.assertTrue(config.isEmpty());
	}

	@Test
	public void testClearProperty() {

		String anyKey = "ANY_KEY";
		String anyValue = "ANY_VALUE";
		config.addProperty(anyKey, anyValue);
		Assert.assertEquals(anyValue, config.getString(anyKey));
		config.clearProperty(anyKey);
		Assert.assertNull(config.getString(anyKey));
	}

	@Test
	public void testContainsKey() {
		Assert.assertTrue("Key does not exist but", config.containsKey(ENV_SUFFIX_PROPERTY_KEY));
		assertPropertyEquals(ENV_SUFFIX_PROPERTY_KEY, "envSuffixPropertyValue");

		Assert.assertFalse(config.containsKey("notExpectedToFindThisKey"));

		//Need to setup with env suffix
		System.setProperty(ENVIRONMENT_PROPERTY, "suffix1");
		config.refresh();

		Assert.assertTrue("Key does not exist", config.containsKey(ENV_SUFFIX_PROPERTY_KEY));
		assertPropertyEquals(ENV_SUFFIX_PROPERTY_KEY, "envSuffixPropertyValueSuffix1");

		//Need to setup with env suffix that has no property set
		System.setProperty(ENVIRONMENT_PROPERTY, "suffix2");
		config.refresh();
		Assert.assertTrue("Key does not exist", config.containsKey(ENV_SUFFIX_PROPERTY_KEY));
		assertPropertyEquals(ENV_SUFFIX_PROPERTY_KEY, "envSuffixPropertyValue");

	}

	@Test
	public void testGetKeys() {
		config.clear();
		config.addProperty("aKeyFirst", "NotMatter");
		config.addProperty("aKeySecond", "NotMatter2");

		Iterator<String> iter = config.getKeys();

		Assert.assertTrue(iter.hasNext());
		Assert.assertEquals("aKeyFirst", iter.next());
		Assert.assertTrue(iter.hasNext());
		Assert.assertEquals("aKeySecond", iter.next());
		Assert.assertFalse(iter.hasNext());


		iter = config.getKeys("aKeySecond");

		Assert.assertTrue(iter.hasNext());
		Assert.assertEquals("aKeySecond", iter.next());
		Assert.assertFalse(iter.hasNext());
	}

	@Test
	public void testConstructorMissingResourceLoader() {
		assertMissingResourceLoader(new DefaultConfiguration(null));
		assertMissingResourceLoader(new DefaultConfiguration(""));
		assertMissingResourceLoader(new DefaultConfiguration(" "));
	}

	private void assertMissingResourceLoader(DefaultConfiguration config) {
		Assert.assertNotNull(config);
		Assert.assertEquals("DEFAULTS-def", config.getString("test.override.defaults"));
	}

	@Test
	public void testGetStringArray() {

		Assert.assertEquals(0, config.getStringArray("NON_EXISTENT_KEY").length);

		Assert.assertEquals(0, config.getStringArray("simple.emptyPropertyKey").length);

		Assert.assertEquals(1, config.getStringArray("simple.stringPropertyKey").length);
		Assert.assertEquals("simplePropertyValue", config.getStringArray("simple.stringPropertyKey")[0]);

		String key = "stringArrayTest";

		config.addProperty(key, "test1, test2,test3");

		String[] result = config.getStringArray(key);

		Assert.assertEquals(3, result.length);
		Assert.assertEquals("test1", result[0]);
		Assert.assertEquals("test2", result[1]);
		Assert.assertEquals("test3", result[2]);
	}

	@Test
	public void testGetEnvironmentKey() {

		System.setProperty(ENVIRONMENT_PROPERTY, "env");

		config.refresh();

		final String key = "key";

		Assert.assertEquals(key + ".env", config.getEnvironmentKey(key));

		System.clearProperty(ENVIRONMENT_PROPERTY);
	}

	@Test
	public void testUseEnvironmentKey() {

		System.clearProperty(ENVIRONMENT_PROPERTY);
		config.refresh();

		Assert.assertFalse(config.useEnvironmentKey("anything"));

		System.setProperty(ENVIRONMENT_PROPERTY, "env");

		config.refresh();
		Assert.assertFalse(config.useEnvironmentKey(ENVIRONMENT_PROPERTY));

		Assert.assertTrue(config.useEnvironmentKey("key"));

		System.clearProperty(ENVIRONMENT_PROPERTY);
	}

	@Test
	public void testSubset() {
		Configuration subConfig = config.subset("simple.propertiesPropertyKey");
		Properties props = subConfig.getProperties("simple.propertiesPropertyKey");

		Assert.assertEquals("Incorrect number of properties", 3, props.size());
		assertPropertyEquals("key1", "value1", props);
		assertPropertyEquals("key2", "value2", props);
		assertPropertyEquals("key3", "value3", props);
	}

	@Test
	public void testLoadWithPhysicalFile() throws Exception {

		Properties props = new Properties();
		props.setProperty("physical.file.include", "physicalFileIncludeValue");
		props.store(new FileWriter(new File("./DefaultConfigTestInclude.properties")), null);

		DefaultConfiguration config = new DefaultConfiguration(
			"com/github/bordertech/config/DefaultConfigurationTest_include.properties");

		Assert.assertEquals("physicalFileIncludeValue", config.getString("physical.file.include"));
	}

	/**
	 * Asserts that the configuration contains the given key/value.
	 *
	 * @param key      the property key
	 * @param expected the expected property value.
	 */
	private void assertPropertyEquals(final String key, final Object expected) {
		Assert.assertEquals("Incorrect value for " + key, expected, config.get(key));
	}

	/**
	 * Asserts that the given properties contains the given key/value.
	 *
	 * @param key the property key
	 * @param expected the expected property value.
	 * @param props the properties to search in.
	 */
	private void assertPropertyEquals(final String key, final Object expected,
			final Properties props) {
		Assert.assertEquals("Incorrect value for " + key, expected, props.get(key));
	}
}
