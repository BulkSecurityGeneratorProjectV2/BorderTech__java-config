# Config
Project configuration mechanism.

## Status
[![Build Status](https://travis-ci.com/BorderTech/java-config.svg?branch=master)](https://travis-ci.com/BorderTech/java-config)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/738a3851c483470da86ffe1d047f344c)](https://www.codacy.com/app/BorderTech/java-config?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=BorderTech/java-config&amp;utm_campaign=Badge_Grade)
[![Codacy Badge](https://api.codacy.com/project/badge/Coverage/738a3851c483470da86ffe1d047f344c)](https://www.codacy.com/app/BorderTech/java-config?utm_source=github.com&utm_medium=referral&utm_content=BorderTech/java-config&utm_campaign=Badge_Coverage)
[![Javadocs](https://www.javadoc.io/badge/com.github.bordertech.config/config.svg)](https://www.javadoc.io/doc/com.github.bordertech.config/config)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.bordertech.config/config.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.github.bordertech.config%22%20AND%20a:%22config%22)

# Why Use Config?
TODO

# How Config Works
The `Config` class is the central access point to the configuration mechanism, and is used to read or modify the
current configuration.

The library is configured using the `Apache Configuration API`. This allows developers to programmatically integrate
the configuration with whatever mechanism is used to configure their applications.

The default resources `Config` looks for are:-
 * `bordertech-defaults.properties` - framework defaults
 * `bordertech-app.properties` - application properties
 * `bordertech-local.properties` - local developer properties

The default configuration can be overridden by setting properties in a file `bordertech-config.properties`.

The following properties can be set:-
* `bordertech.config.default.impl` - Default implementation class name
* `bordertech.config.spi.enabled` - enable SPI lookup (default `true`)
* `bordertech.config.spi.append.default` - append the default configuration (default `true`)
* `bordertech.config.resource.order` - order of resources to load into the configuration

## SPI
'ConfigurationLoader' is the SPI interface for classes that can load a custom configuration.

