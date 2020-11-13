# Change log

## Release in-progress

## 1.0.7

### API Changes
* Introduction of profile property `bordertech.config.profile` to replace environment property.

### Enhancements
* Refactor of substitution code to use apache commons text
* includes can define substitution variables e.g. `include=${previously.defined.property.key}/special-file.properties`
* Increase in unit tests and code coverage. 

## 1.0.6

### API Changes
* The runtime property `bordertech.config.parameters.useSystemOverWriteOnly` now defaults to false which allows all system properties to be
  merged when system properties enabled. Use the optional runtime property `bordertech.config.parameters.useSystemPrefixes` to limit the
  properties merged.

### Enhancements
* Latest qa-parent
* Ability to override the default config file name via a system or environment property `BT_CONFIG_FILE=my-config.properties`
* Option to append extra resources to the defined resources via config property `bordertech.config.resources.append`
* Option to load environment variables #31
  * Enable via runtime property `bordertech.config.parameters.useEnvProperties=true`
  * Option to limit variables merged via property `bordertech.config.parameters.useEnvPrefixes`. Defaults to allow all.

## 1.0.5

### Enhancements
* Latest qa-parent #27
* Improve README #24

## 1.0.4

### Enhancements
* Optional dump parameters to a file. The file name can be set via runtime property `bordertech.config.parameters.dump.file`
* Optional merge system properties into config.
  * Enable system properties via runtime property `bordertech.config.parameters.useSystemProperties=true`
  * Option to limit system properties merged via runtime property `bordertech.config.parameters.useSystemPrefixes`. Defaults to allow all.
  * Defaults to only merge properties that already exist. Disable via runtime property `bordertech.config.parameters.useSystemOverWriteOnly=false`

## 1.0.3

### Enhancements
* Latest qa-parent

## 1.0.2
* Latest qa-parent
* Switch to travis
* The reload of the configuration can be triggered via a touchfile. The touchfile can be set via the runtime property`bordertech.config.touchfile`.
  To avoid excessive IO on the touchfile an interval (in milli seconds) between checks can be set via the runtime property
 `bordertech.config.touchfile.interval` which defaults to 10000ms.

## 1.0.1
* Ability to define properties to only take effect in a certain environment. When the runtime property `bordertech.config.environment` is set,
  it is used as the suffix for each property lookup. If no property exists with the current environment suffix then the default property (ie no
  suffix) value is used.

## 1.0.0
* Initial version
