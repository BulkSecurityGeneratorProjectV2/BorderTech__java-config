# Config
Project configuration mechanism.

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

