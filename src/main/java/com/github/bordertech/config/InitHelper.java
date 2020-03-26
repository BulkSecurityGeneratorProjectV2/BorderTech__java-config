package com.github.bordertech.config;

import java.util.Arrays;
import java.util.List;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang3.StringUtils;

/**
 * Helper class for {@link Config} initialisation.
 * <p>
 * The helper checks for configuration overrides by searching for a property file named
 * <code>bordertech-config.properties</code> in the user home directory, the current classpath and the system classpath.
 * The file name can be overridden by setting an environment or system property with the key
 * <code>bordertech.config.file</code>.
 * </p>
 * <p>
 * The following properties can be set:-
 * <ul>
 * <li>bordertech.config.default.impl - Default implementation class name</li>
 * <li>bordertech.config.spi.enabled - enable SPI lookup (default: true)</li>
 * <li>bordertech.config.spi.append.default - append the default configuration (default: true)</li>
 * <li>bordertech.config.resource.order - order of resources to load into the configuration</li>
 * </ul>
 * <p>
 * The default resources Config looks for are:-
 * </p>
 * <ul>
 * <li><code>bordertech-defaults.properties</code> - framework defaults</li>
 * <li><code>bordertech-app.properties</code> - application properties</li>
 * <li><code>bordertech-local.properties</code> - local developer properties</li>
 * </ul>
 *
 *
 * @author Jonathan Austin
 * @since 1.0.0
 *
 * @see Config
 */
public final class InitHelper {

	private static final String DEFAULTS_FILE_PARAM_KEY = "bordertech.config.file";
	private static final String DEFAULTS_FILE_NAME = "bordertech-config.properties";
	private static final String PARAM_KEY_DEFAULT_CONFIG_IMPL = "bordertech.config.default.impl";
	private static final String PARAM_KEY_SPI_ENABLED = "bordertech.config.spi.enabled";
	private static final String PARAM_KEY_SPI_APPEND_DEFAULT = "bordertech.config.spi.append.default";
	private static final String PARAM_KEY_RESOURCE_ORDER = "bordertech.config.resource.order";
	private static final List<String> DEFAULT_BORDERTECH_LOAD_ORDER = Arrays.asList(
			// The name of the first resource we look for is for internal default properties
			"bordertech-defaults.properties",
			// The name of the next resource we look at is for application properties
			"bordertech-app.properties",
			// The last properties which are loaded are local/developer properties
			"bordertech-local.properties");
	private static final List<String> DEFAULT_RESOURCE_LOAD_ORDER;

	/**
	 * Default configuration class name. *
	 */
	public static final String DEFAULT_CONFIG_IMPL;
	/**
	 * SPI enabled flag.
	 */
	public static final boolean SPI_ENABLED;
	/**
	 * SPI append default config flag.
	 */
	public static final boolean SPI_APPEND_DEFAULT_CONFIG;

	static {
		// Load the config defaults (if exists)
		String configFile = getDefaultConfigFileName();
		Configuration configDefaults = loadPropertyFile(configFile);
		DEFAULT_CONFIG_IMPL = configDefaults.getString(PARAM_KEY_DEFAULT_CONFIG_IMPL, DefaultConfiguration.class.getName());
		SPI_APPEND_DEFAULT_CONFIG = configDefaults.getBoolean(PARAM_KEY_SPI_APPEND_DEFAULT, true);
		SPI_ENABLED = configDefaults.getBoolean(PARAM_KEY_SPI_ENABLED, true);
		String[] override = configDefaults.getStringArray(PARAM_KEY_RESOURCE_ORDER);
		if (override == null || override.length == 0) {
			DEFAULT_RESOURCE_LOAD_ORDER = DEFAULT_BORDERTECH_LOAD_ORDER;
		} else {
			DEFAULT_RESOURCE_LOAD_ORDER = Arrays.asList(override);
		}
	}

	/**
	 * Private constructor.
	 */
	private InitHelper() {
	}

	/**
	 * @return the default resource load order.
	 */
	public static String[] getDefaultResourceLoadOrder() {
		return DEFAULT_RESOURCE_LOAD_ORDER.toArray(new String[]{});
	}

	/**
	 * @return default predefined bordertech resource load order.
	 */
	public static String[] getDefaultBordertechLoadOrder() {
		return DEFAULT_BORDERTECH_LOAD_ORDER.toArray(new String[]{});
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

	/**
	 * Check if the default config file name has been overridden via environment or system properties.
	 *
	 * @return the default config file name
	 */
	private static String getDefaultConfigFileName() {
		// Check environment variable
		String name = System.getenv(DEFAULTS_FILE_PARAM_KEY);
		if (!StringUtils.isBlank(name)) {
			return name;
		}
		// Check system property
		name = System.getProperty(DEFAULTS_FILE_PARAM_KEY);
		// If no system property, return the default file name
		return StringUtils.isBlank(name) ? DEFAULTS_FILE_NAME : name;
	}

}
