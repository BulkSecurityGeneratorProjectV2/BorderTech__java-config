package com.github.bordertech.config;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * Helper class for {@link Config} initialisation.
 *
 * @author Jonathan Austin
 * @since 1.0.0
 */
public final class InitHelper {

	private static final String DEFAULTS_FILE_NAME = "bordertech-config.properties";

	private static final String PARAM_KEY_DEFAULT_CONFIG_IMPL = "bordertech.config.default.impl";
	private static final String PARAM_KEY_SLI_APPEND_DEFAULT = "bordertech.config.sli.append.default";
	private static final String PARAM_KEY_SLI_ENABLED = "bordertech.config.sli.enabled";
	private static final String PARAM_KEY_RESOURCE_ORDER = "bordertech.config.resource.order";

	public static final String DEFAULT_CONFIG_IMPL;
	public static final boolean SLI_ENABLED;
	public static final boolean SLI_APPEND_DEFAULT_CONFIG;
	public static final String[] DEFAULT_RESOURCE_LOAD_ORDER;
	public static final String[] DEFAULT_BORDERTECH_LOAD_ORDER
			= {
				// The name of the first resource we look for is for internal default properties
				"bordertech-default.properties",
				// The name of the next resource we look at is for application properties
				"bordertech-app.properties",
				// The last properties which are loaded are local/developer properties
				"bordertech-local.properties",};

	static {
		// Load the config defaults (if exists)
		Configuration configDefaults = loadPropertyFile(DEFAULTS_FILE_NAME);
		DEFAULT_CONFIG_IMPL = configDefaults.getString(PARAM_KEY_DEFAULT_CONFIG_IMPL, DefaultConfiguration.class.getName());
		SLI_APPEND_DEFAULT_CONFIG = configDefaults.getBoolean(PARAM_KEY_SLI_APPEND_DEFAULT, true);
		SLI_ENABLED = configDefaults.getBoolean(PARAM_KEY_SLI_ENABLED, true);
		String[] override = configDefaults.getStringArray(PARAM_KEY_RESOURCE_ORDER);
		if (override == null || override.length == 0) {
			DEFAULT_RESOURCE_LOAD_ORDER = DEFAULT_BORDERTECH_LOAD_ORDER;
		} else {
			DEFAULT_RESOURCE_LOAD_ORDER = override;
		}
	}

	/**
	 * Helper method to retrieve a single property file.
	 *
	 * @param fileName the property file name
	 * @return the file properties or empty properties if file does not exist
	 */
	public static Configuration loadPropertyFile(final String fileName) {
		Configuration configDefaults = null;
		if (ConfigurationUtils.locate(fileName) != null) {
			try {
				configDefaults = new PropertiesConfiguration(fileName);
			} catch (ConfigurationException e) {
				throw new IllegalStateException("Could not load config file [" + fileName + "]." + e.getMessage(), e);
			}
		}
		if (configDefaults == null) {
			// Empty Config
			configDefaults = new PropertiesConfiguration();
		}
		return configDefaults;
	}

}
