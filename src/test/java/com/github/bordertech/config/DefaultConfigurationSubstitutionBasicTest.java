package com.github.bordertech.config;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests to check substitution of values for {@link DefaultConfiguration}.
 */
public class DefaultConfigurationSubstitutionBasicTest {

	private static final String EXPECTED_VALUEZ = "valueZ";
	private static final String EXPECTED_VALUEZ2 = "valueZ2";

	@Test
	public void testSubstitutePropertyValuesBeforeDefined() {
		DefaultConfiguration config = new DefaultConfiguration("com/github/bordertech/config/DefaultConfigurationTestSubstitution.properties");
		Assert.assertEquals("Value for X should match Z value", EXPECTED_VALUEZ, config.get("substitute.test.X"));
		Assert.assertEquals("Value for Y should match Z value", EXPECTED_VALUEZ, config.get("substitute.test.Y"));
		Assert.assertEquals("Value for Z should be valueZ", EXPECTED_VALUEZ, config.get("substitute.test.Z"));
	}

	@Test
	public void testSubstitutePropertyValuesReverseOrder() {
		DefaultConfiguration config = new DefaultConfiguration("com/github/bordertech/config/DefaultConfigurationTestSubstitution.properties");
		Assert.assertEquals("Value for X2 should match Z2 value", EXPECTED_VALUEZ2, config.get("substitute.test.X2"));
		Assert.assertEquals("Value for Y2 should match Z2 value", EXPECTED_VALUEZ2, config.get("substitute.test.Y2"));
		Assert.assertEquals("Value for Z2 should be valueZ2", EXPECTED_VALUEZ2, config.get("substitute.test.Z2"));
	}

	@Test
	public void testSubstituteProfileSuffixKey() {
		DefaultConfiguration config = new DefaultConfiguration("com/github/bordertech/config/DefaultConfigurationTestSubstitutionProfileSuffix.properties");
		Assert.assertEquals("Profile value was not substituted", "TEST", config.get(DefaultConfiguration.PROFILE_PROPERTY));
		Assert.assertEquals("Property value with profile suffix not correct", "PROFILE_TEST", config.get("substitute.test.profile"));
	}

	@Test
	public void testSubstituteEnvSuffixKey() {
		DefaultConfiguration config = new DefaultConfiguration("com/github/bordertech/config/DefaultConfigurationTestSubstitutionEnvSuffix.properties");
		Assert.assertEquals("Environment value was not substituted", "TEST", config.get(DefaultConfiguration.ENVIRONMENT_PROPERTY));
		Assert.assertEquals("Property value with environment suffix not correct", "ENV_TEST", config.get("substitute.test.env"));
	}

}
