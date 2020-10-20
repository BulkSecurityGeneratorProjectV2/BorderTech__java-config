package com.github.bordertech.config;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Class to access the static default configuration of {@link InitHelper} to allow
 * for more complete testing of the classes using {@link InitHelper}.
 */
public class AccessInitHelper {

	private AccessInitHelper(){}

	private static Field getField(String name) throws Exception {
		Field field = InitHelper.class.getField(name);

		field.setAccessible(true);

		Field modifiersFiled = Field.class.getDeclaredField("modifiers");
		modifiersFiled.setAccessible(true);
		modifiersFiled.setInt(field, field.getModifiers() & ~Modifier.FINAL);

		return field;
	}

	public static void overrideSpiEnabled(boolean value, boolean resetConfig) throws Exception {
		getField("SPI_ENABLED").set(null, value);

		if (resetConfig) {
			Config.reset();
		}
	}

	public static void overrideSpiAppend(boolean value, boolean resetConfig) throws Exception {
		getField("SPI_APPEND_DEFAULT_CONFIG").set(null, value);

		if (resetConfig) {
			Config.reset();
		}
	}

	public static void overrideDefaultConfig(String defaultConfig, boolean resetConfig) throws Exception {
		getField("DEFAULT_CONFIG_IMPL").set(null, defaultConfig);

		if (resetConfig) {
			Config.reset();
		}
	}

	public static void reset() throws Exception {
		overrideSpiEnabled(true, false);
		overrideSpiAppend(true, false);
		overrideDefaultConfig(DefaultConfiguration.class.getName(), false);

		Config.reset();
	}
}
