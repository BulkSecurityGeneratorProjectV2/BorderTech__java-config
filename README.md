# Config

## Status
[![Build Status](https://travis-ci.com/BorderTech/java-config.svg?branch=master)](https://travis-ci.com/BorderTech/java-config)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=bordertech-java-config&metric=alert_status)](https://sonarcloud.io/dashboard?id=bordertech-java-config)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=bordertech-java-config&metric=reliability_rating)](https://sonarcloud.io/dashboard?id=bordertech-java-config)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=bordertech-java-config&metric=coverage)](https://sonarcloud.io/dashboard?id=bordertech-java-config)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/ff9d14e9be2c4071b5e94bed4c7545cb)](https://www.codacy.com/gh/BorderTech/java-config?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=BorderTech/java-config&amp;utm_campaign=Badge_Grade)
[![Javadocs](https://www.javadoc.io/badge/com.github.bordertech.config/config.svg)](https://www.javadoc.io/doc/com.github.bordertech.config/config)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.bordertech.config/config.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.github.bordertech.config%22%20AND%20a:%22config%22)

## Content
- [What is Config](#what-is-config)
- [Why use Config](#why-use-config)
- [Getting started](#getting-started)
- [Features](#features)
- [Configuration](#configuration)
- [Contributing](#contributing)

## What is Config
Project configuration mechanism.

## Why use Config
The [Config](https://github.com/BorderTech/java-config/blob/master/src/main/java/com/github/bordertech/config/Config.java) class provides a standard mechanism java applications can use to access configuration data via the [Apache Commons Configuration](https://commons.apache.org/proper/commons-configuration/index.html) interface.

The [features](#features) of the [Default Configuration](https://github.com/BorderTech/java-config/blob/master/src/main/java/com/github/bordertech/config/DefaultConfiguration.java) combine and enhance the functionality of the classic [PropertiesConfiguration](https://commons.apache.org/proper/commons-configuration/apidocs/org/apache/commons/configuration2/PropertiesConfiguration.html) and [SystemConfiguration](https://commons.apache.org/proper/commons-configuration/apidocs/org/apache/commons/configuration2/SystemConfiguration.html) with predefined property file resources.

Projects can easily override this default implementation via the [configuration](#configuration) settings.

## Getting started
Add dependency:

``` xml
<project>
  ....
  <dependency>
    <groupId>com.github.bordertech.config</groupId>
    <artifactId>config</artifactId>
    <version>1.0.6</version>
  </dependency>
  ....
</project>
```

`Config.getInstance()` is the central access point to the configuration mechanism. Configuration properties can be read from the current configuration as follows:

``` java
  ....
  // Retrieve a property `my.example` with a default value
  String value = Config.getInstance().getString("my.example", "a-default-value");
  ....
```

To override the `my.example` property, create a `bordertech-app.properties` file in the resources directory and add the following:

``` java properties
my.example=a-override-value
```

## Features

### Predefined property resources
The default implementation looks for the following resources either as a classpath resource or a URL:

 - `bordertech-defaults.properties` - framework defaults
 - `bordertech-app.properties` - application properties
 - `bordertech-local.properties` - local developer properties

Projects will usually use `bordertech-app.properties` resource files.

The priority of the properties is in reverse order to the list of resources (i.e. `local` overrides `app` which overrides `defaults`).

The resources loaded into the Configuration can be overridden via [configuration](#configuration) settings.

### Include resources
Other property files can be included from other predefined property files.
If the "include" property is defined, it is treated as a (comma-separated) list of additional resources (classpath resource or a URL) 
to load that are processed immediately within the current resource being loaded

``` java properties
include=include_resource_1.properties[,include_resource_2.properties]
```

### IncludeAfter resources
Other property files can be included from the predefined property file after the current set has loaded.
If this property is defined, it is treated as a (comma-separated) list of additional resources (classpath resource or a URL) 
to load that are processed after the current (set of) resources have loaded.

``` java properties
includeAfter=include_resource_1.properties[,include_resource_2.properties]
``` 

### += Append values to predefined properties
Config also allows for the ability to append values to properties already defined. 
This is done using '+=' instead of '=' on a key-value pair. Suggested use case is you have a global default set and then
you want to append application specific values to the default values for access within an application.
 
 ``` java properties
# Defined in default.properties
already.defined.key+=value1

#Defined in app.properties
already.defined.key+=value2,value3
``` 

`Config.getInstance().get("already.defined.key")` returns `value1,value2,value3`

### Profiles
Profiles allow you to map properties to different profiles - for example, dev, test, prod or mock.
We can activate these profiles in different environments to set(override) the properties we need. 
The profile property is generally to be defined as either an OS environment variable or a JVM system property. 
However, it can be set in a properties file which is useful in unit testing or testing on a local environment.

When a property with the key `bordertech.config.profile` is set, it is used as the suffix for each property lookup:

``` java properties
## MOCK Environment set as an Environment or JVM System property only
bordertech.config.profile=MOCK

my.example.property.MOCK=mocking
my.example.property.PROD=proding
my.example.property=defaulting
```

The Environment Suffix `bordertech.config.environment` feature has been deprecated and will be removed in the next
major release but is still honoured within the profile feature.

The order of precedence:
- `bordertech.config.profile` defined as a property anywhere
- `bordertech.config.environment` defined as a property anywhere(Deprecated - to be removed next major release)

### Touchfile

The reload of the configuration can be triggered via a `touchfile`. The `touchfile` can be set via the property:

``` java properties
bordertech.config.touchfile=my-touchfile.properties
```

To avoid excessive IO an interval (in milli seconds) between checks can be set and defaults to 10000:

``` java properties
bordertech.config.touchfile.interval=3000
```

### Merge System Properties into Configuration

Sometimes you may need to include System Properties in the Configuration:

|Property key|Description|Default value|
|-------------|-----------|-------------|
|bordertech.config.parameters.useSystemProperties|This flag allows system properties to be merged into the Configuration at the end of the loading process.|false|
|bordertech.config.parameters.useSystemOverWriteOnly|This flag controls if a system property will only overwrite an existing property|false|
|bordertech.config.parameters.useSystemPrefixes|Define a list of system property prefixes that are allowed to be merged. Default is allow all.|n/a|

System properties will override properties in resource files.

### Merge Environment Properties into Configuration

Sometimes you may need to include Environment Properties in the Configuration:

|Property key|Description|Default value|
|-------------|-----------|-------------|
|bordertech.config.parameters.useEnvProperties|This flag allows environment properties to be merged into the Configuration at the end of the loading process.|false|
|bordertech.config.parameters.useEnvPrefixes|Define a list of environment property prefixes that are allowed to be merged. Default is allow all.|n/a|

Environemnt properties will override system properties and properties in resource files.

### Merge Configuration into System Properties

Sometimes you may need to merge Configuration properties into the System Properties:

|Property key|Description|Default value|
|-------------|-----------|-------------|
|bordertech.config.parameters.system.\*|Parameters with this prefix will be dumped into the System parameters. Not for general use|n/a|


### Logging

The default implementation uses [SimpleLog](https://commons.apache.org/proper/commons-logging/apidocs/org/apache/commons/logging/impl/SimpleLog.html). This Simple implementation of Log sends all enabled log messages, for all defined loggers, to System.err.

Other logging options:

|Property key|Description|Default value|
|-------------|-----------|-------------|
|bordertech.config.parameters.dump.console|This flag allows properties to be dumped to the console after being loaded.|false|
|bordertech.config.parameters.dump.file|The file name to dump the properties to after being loaded.|n/a|

### Property listeners

Property listeners can be set on the `Config` to be notified whenever the `Config` is set or reloaded.

``` java
  Config.addPropertyChangeListener(new MyListener());
```

### Testing

The following methods in the `Config` class are useful for unit testing:

- `reset()` - will restore the configuration.
- `copyConfiguration(Configuration)` - will perform a deep-copy of the given configuration. This is useful when you need to create a backup copy of the current configuration before modifying it for a particular test.

## Configuration

The initial configuration of `Config` can be overridden by setting properties in a file `bordertech-config.properties`. 
The default `bordertech-config.properties` file name can also be overriden via a System or Environment property `BT_CONFIG_FILE`.

The following options can be set:-

|Property key|Description|Default value|
|-------------|-----------|-------------|
|bordertech.config.default.impl|Default Configuration implementation class name|com.github.bordertech.config.DefaultConfiguration|
|bordertech.config.spi.enabled|The flag to enable SPI lookup|true|
|bordertech.config.spi.append.default|The flag to append the default configuration|true|
|bordertech.config.resource.order|The list of property resources to load into the configuration. Priority of properties is in reverse order of the list.|bordertech-defaults.properties, bordertech-app.properties, bordertech-local.properties|
|bordertech.config.resource.append|An optional list of extra property resources to append to the resources. Useful to add extra resources to the default resources.|n/a|

### Default Implementation

Example of overriding the default implementation:

``` java properties
bordertech.config.default.impl=my.example.SpecialConfiguration
```

### Custom Resources to Load

Example of loading the default resources and a project specific resource:

``` java properties
bordertech.config.resource.append=my-project.properties
```

### SPI

[ConfigurationLoader](https://github.com/BorderTech/java-config/blob/master/src/main/java/com/github/bordertech/config/ConfigurationLoader.java) is the [SPI](https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html) interface for classes that can load a custom configuration.

By default, the SPI lookup is enabled and if found, it will create the custom configuration
 
If the `bordertech.config.spi.append.default` is true the Default Configuration will also be appended to the configuration.

### Best Practice

When using java-config in a container and setting specific properties for that container instance, 
this can be achieved by property file(s) placed in the container that can be referred to and included within the application at runtime. 

## Contributing

Refer to these guidelines for [Workflow](https://github.com/BorderTech/java-common/wiki/Workflow) and [Releasing](https://github.com/BorderTech/java-common/wiki/Releasing).
