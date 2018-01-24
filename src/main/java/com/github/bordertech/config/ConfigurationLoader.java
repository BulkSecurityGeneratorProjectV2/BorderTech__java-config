package com.github.bordertech.config;

import org.apache.commons.configuration.Configuration;

/**
 * SPI interface for classes that can load a custom configuration.
 *
 * @author Joshua Barclay
 * @since 1.0.0
 */
public interface ConfigurationLoader {

	/**
	 * <p>
	 * Provides the configuration for this loader. The result of this configuration will be added to a composite
	 * configuration along with the configuration from other ConfigurationLoaders.</p>
	 *
	 * @return The custom configuration for this loader.
	 */
	Configuration getConfiguration();

}
