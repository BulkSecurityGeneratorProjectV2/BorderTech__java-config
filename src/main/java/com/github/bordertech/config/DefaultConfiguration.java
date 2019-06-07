package com.github.bordertech.config;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.MapConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.SimpleLog;

/**
 * <p>
 * Implementation of the {@link Configuration} interface.
 * </p>
 *
 * @author Jonathan Austin
 * @since 1.0.0
 *
 * @see Config
 */
public class DefaultConfiguration implements Configuration {

	/**
	 * Logger for debug information.
	 */
	private static final Log LOG = new SimpleLog("DefaultConfig");

	/**
	 * If this parameter is defined, it is treated as a comma-separated list of additional resources to load. The
	 * include is processed immediately.
	 */
	public static final String INCLUDE = "include";

	/**
	 * If this parameter is defined, it is taken as a (comma-separated) resource to load. The resource is loaded after
	 * the current (set of) resources is loaded.
	 */
	public static final String INCLUDE_AFTER = "includeAfter";

	/**
	 * If this parameter is defined and resolves to true as a boolean, then the system properties will be merged at the
	 * end of the loading process.
	 */
	public static final String USE_SYSTEM_PROPERTIES = "bordertech.config.parameters.useSystemProperties";

	/**
	 * If merging System Properties this parameter controls if a system property will only overwrite an existing
	 * property. The default is true.
	 */
	public static final String USE_SYSTEM_OVERWRITEONLY = "bordertech.config.parameters.useSystemOverWriteOnly";

	/**
	 * If merging System Properties, this parameter can be used to define a list of attribute prefixes that are allowed
	 * to be merged. The default is allow all System Properties to be merged.
	 */
	public static final String USE_SYSTEM_PREFIXES = "bordertech.config.parameters.useSystemPrefixes";

	/**
	 * If this parameter is set to true, then after loading the parameters, they will be dumped to the console.
	 */
	public static final String DUMP = "bordertech.config.parameters.dump.console";

	/**
	 * If this parameter is set, then after loading the parameters, they will be dumped to the specified file.
	 */
	public static final String DUMP_FILE = "bordertech.config.parameters.dump.file";

	/**
	 * If this parameter is set, it will be used as the environment suffix for each property lookup.
	 */
	public static final String ENVIRONMENT_PROPERTY = "bordertech.config.environment";

	/**
	 * Parameters with this prefix will be dumped into the System parameters. This feature is for handling recalcitrant
	 * 3rd party software only - not for general use!!!
	 */
	public static final String SYSTEM_PARAMETERS_PREFIX = "bordertech.config.parameters.system.";

	/**
	 * If this parameter is defined and resolves to true as a boolean, then the system properties will be merged at the
	 * end of the loading process.
	 *
	 * @deprecated use {@link #USE_SYSTEM_PROPERTIES} instead
	 */
	@Deprecated
	private static final String LEGACY_USE_SYSTEM_PROPERTIES = "bordertech.wcomponents.parameters.useSystemProperties";

	/**
	 * If this parameter is set to true, then after loading the parameters, they will be dumped to the console.
	 *
	 * @deprecated use {@link #DUMP} property instead
	 */
	@Deprecated
	private static final String LEGACY_DUMP = "bordertech.wcomponents.parameters.dump.console";

	/**
	 * Parameters with this prefix will be dumped into the System parameters. This feature is for handling recalcitrant
	 * 3rd party software only - not for general use!!!
	 *
	 * @deprecated use {@link #SYSTEM_PARAMETERS_PREFIX} instead
	 */
	@Deprecated
	private static final String LEGACY_SYSTEM_PARAMETERS_PREFIX = "bordertech.wcomponents.parameters.system.";

	/**
	 * The prefix output before log messages.
	 */
	private static final String LOG_PREFIX = "PARAM_DEBUG: ";

	// -----------------------------------------------------------------------------------------------------------------
	// State used during loading of parameters
	/**
	 * The messages logged during loading of the configuration. \
	 */
	private final StringBuilder messages = new StringBuilder();

	/**
	 * The resource being loaded. This is used for the relative form of resource loading.
	 */
	private final Deque<String> resources = new ArrayDeque<>();

	/**
	 * A generic object that allows us to synchronized refreshes. Required so that gets and refreshes are threadsafe
	 */
	private final Object lockObject = new Object();

	// -----------------------------------------------------------------------------------------------------------------
	// Implementation
	/**
	 * Holds the current environment suffix (if set).
	 */
	private String currentEnvironment = null;

	/**
	 * Our backing store is a Map object.
	 */
	private Map<String, String> backing;

	/**
	 * Explicitly cache booleans for flag look-up speed.
	 */
	private Set<String> booleanBacking;

	/**
	 * Stores "explanations" of where each setting comes from. Each parameter will have a history, explaining all the
	 * locations where that parameter was defined, in reverse order (so the first entry is the defining entry).
	 */
	private Map<String, String> locations;

	/**
	 * Cache of subcontexts, by {true,false}-prefix.
	 */
	private Map<String, Properties> subcontextCache;

	/**
	 * Properties added at runtime.
	 */
	private IncludeProperties runtimeProperties;

	/**
	 * Variables that we are in the process of substituting. This is used to detect recursive substitutions
	 */
	private final Set<String> substituting = new HashSet<>();

	/**
	 * Resource load order.
	 */
	private final String[] resourceLoadOrder;

	/**
	 * Creates a Default Configuration.
	 */
	public DefaultConfiguration() {
		this(InitHelper.getDefaultResourceLoadOrder());
	}

	/**
	 * Creates a Default Configuration with the specified resource order.
	 *
	 * @param resourceLoadOrder the resource order
	 */
	public DefaultConfiguration(final String... resourceLoadOrder) {
		if (resourceLoadOrder == null || resourceLoadOrder.length == 0) {
			this.resourceLoadOrder = InitHelper.getDefaultResourceLoadOrder();
		} else {
			this.resourceLoadOrder = resourceLoadOrder;
		}
		initialiseInstanceVariables();
		load();
	}

	/**
	 * Splits the given comma-delimited string into an an array. Leading/trailing spaces in list items will be trimmed.
	 *
	 * @param list the String to split.
	 * @return the split version of the list.
	 */
	private String[] parseStringArray(final String list) {
		StringTokenizer tokenizer = new StringTokenizer(list, ",", false);
		int length = tokenizer.countTokens();
		String[] arr = new String[length];

		for (int i = 0; tokenizer.hasMoreElements(); i++) {
			arr[i] = cleanSpaces(tokenizer.nextToken());
		}

		return arr;
	}

	/**
	 * Removes any leading/trailing spaces from the given string. This has the same effect as calling
	 * {@link String#trim}, but is null-safe.
	 *
	 * @param aStr the String to trim.
	 * @return the trimmed String, or null if the <code>aStr</code> was null.
	 */
	private String cleanSpaces(final String aStr) {
		if (aStr == null) {
			return aStr;
		}

		return aStr.trim();
	}

	// -----------------------------------------------------------------------------------------------------------------
	/**
	 * This method initialises most of the instance variables.
	 */
	private void initialiseInstanceVariables() {
		backing = new HashMap<>();
		booleanBacking = new HashSet<>();
		locations = new HashMap<>();

		// subContextCache is updated on the fly so ensure no concurrent modification.
		subcontextCache = Collections.synchronizedMap(new HashMap());
		runtimeProperties = new IncludeProperties("Runtime: added at runtime");
		currentEnvironment = null;
	}

	/**
	 * Load the backing from the properties file visible to our classloader, plus the filesystem.
	 */
	private void load() {
		recordMessage("Loading parameters");
		File cwd = new File(".");
		String workingDir;

		try {
			workingDir = cwd.getCanonicalPath();
		} catch (IOException ex) {
			workingDir = "UNKNOWN";
		}

		recordMessage("Working directory is " + workingDir);

		for (String resourceName : resourceLoadOrder) {
			loadTop(resourceName);
		}

		if (isUseSystemProperties()) {
			recordMessage("Loading from system properties");
			loadSystemProperties();
		}

		// Now perform variable substitution.
		do {
			// Do nothing while loop
		} while (substitute());

		// Dump Header Info
		LOG.info(getDumpHeader());
		// Dump properties
		if (isDumpPropertiesConsole() || isDumpPropertiesFile()) {
			handleDumpPropertyDetails();
		}

		// We don't want the StringBuilder hanging around after 'DUMP'.
		clearMessages();

		// Now move any parameters with the system parameters prefix into the real system parameters.
		Properties systemProperties = getSubProperties(SYSTEM_PARAMETERS_PREFIX, true);
		System.getProperties().putAll(systemProperties);
		// LEGACY
		systemProperties = getSubProperties(LEGACY_SYSTEM_PARAMETERS_PREFIX, true);
		System.getProperties().putAll(systemProperties);

		// Check if environment set
		checkEnvironmentProperty();
	}

	/**
	 * @return true if load system properties into config
	 */
	private boolean isUseSystemProperties() {
		return getBoolean(USE_SYSTEM_PROPERTIES) || getBoolean(LEGACY_USE_SYSTEM_PROPERTIES);
	}

	/**
	 * @return true if dump properties to the console
	 */
	private boolean isDumpPropertiesConsole() {
		return getBoolean(DUMP) || getBoolean(LEGACY_DUMP);
	}

	/**
	 * @return true if dump properties to a file
	 */
	private boolean isDumpPropertiesFile() {
		return !StringUtils.isEmpty(getDumpFileLocation());
	}

	/**
	 * @return the dump properties to file
	 */
	private String getDumpFileLocation() {
		return get(DUMP_FILE, "");
	}

	/**
	 * Dump the property details.
	 */
	private void handleDumpPropertyDetails() {

		String loadMessages = getDumpLoadMessages();
		String propMessages = getDumpPropertyDetails();

		// Dump to console
		if (isDumpPropertiesConsole()) {
			LOG.info(loadMessages);
			LOG.info(propMessages);
		}

		// Dump to File
		if (isDumpPropertiesFile()) {
			String dest = getDumpFileLocation();
			try (FileOutputStream fos = new FileOutputStream(dest); PrintStream stream = new PrintStream(fos)) {
				stream.println(loadMessages);
				stream.println(propMessages);
			} catch (IOException e) {
				recordException(e);
			}
		}
	}

	/**
	 * @return debugging information for logging
	 */
	private String getDumpHeader() {
		File cwd = new File(".");
		String workingDir;

		try {
			workingDir = cwd.getCanonicalPath();
		} catch (IOException ex) {
			workingDir = "UNKNOWN";
		}

		String codesourceStr = "";

		// Try to be sneaky and print the codesource location (for orientation of user)
		try {
			ProtectionDomain domain = getClass().getProtectionDomain();
			if (domain != null) {
				CodeSource codesource = domain.getCodeSource();
				codesourceStr = codesource == null ? "" : "Code location of Config implementation: " + codesource.getLocation();
			}
		} catch (Exception failed) {
			codesourceStr = "Could not determine location of Config implementation [" + failed.getMessage() + "].";
		}

		StringBuilder info = new StringBuilder();

		info.append("----Config: Info start----\n");
		info.append(codesourceStr);
		info.append("\nWorking directory is: ");
		info.append(workingDir);
		info.append("\nTo dump all params to the console set ");
		info.append(DUMP);
		info.append(" to true; current value is ");
		info.append(isDumpPropertiesConsole());
		info.append("\nTo dump all params to a file set ");
		info.append(DUMP_FILE);
		info.append(" to file location; current value is ");
		info.append(getDumpFileLocation());
		info.append("\nLOGGING can be controlled by configuring org.apache.commons.logging.impl.SimpleLog.");
		info.append("\nSimpleLog writes to System.err by default.");
		info.append("\n----Config: Info end------");

		return info.toString();
	}

	/**
	 * @return dump of all properties loaded with their location history
	 */
	private String getDumpPropertyDetails() {

		StringBuilder info = new StringBuilder();

		info.append("----Config: Properties loaded start----\n");

		for (String key : new TreeSet<>(backing.keySet())) {
			String value = backing.get(key);
			String history = locations.get(key);
			info.append(LOG_PREFIX);
			info.append(key);
			info.append(" = ");
			info.append(value);
			info.append(" (");
			info.append(history);
			info.append(")\n");
		}

		info.append("----Config: Properties loaded end----\n");

		return info.toString();
	}

	/**
	 * @return debugging load messages
	 */
	private String getDumpLoadMessages() {

		StringBuilder info = new StringBuilder();

		info.append("----Config: Load messages start----\n");
		info.append(messages.toString());
		info.append("----Config: Load messages end----\n");

		return info.toString();
	}

	/**
	 * Loading of "top level" resources is different to the general recursive case, since it is only at the top level
	 * that we check for the includeAfter parameter.
	 *
	 * @param resourceName the path of the resource to load from.
	 */
	private void loadTop(final String resourceName) {
		try {
			resources.push(resourceName);

			load(resourceName);

			// Now check for INCLUDE_AFTER resources
			String includes = get(INCLUDE_AFTER);

			if (includes != null) {
				// First, do substitution on the INCLUDE_AFTER
				do {
					// Looping
				} while (substitute(INCLUDE_AFTER));

				// Now split and process
				String[] includeAfter = getString(INCLUDE_AFTER).split(",");
				backing.remove(INCLUDE_AFTER);
				for (String after : includeAfter) {
					loadTop(after);
				}
			}
		} finally {
			resources.pop();
		}
	}

	/**
	 * Try loading the given resource name. There may be several resources corresponding to that name...
	 *
	 * @param resourceName the path of the resource to load from.
	 */
	private void load(final String resourceName) {

		boolean found = false;

		try {
			resources.push(resourceName);

			// Load the resource/s from the class loader
			List<URL> urls = findClassLoaderResources(resourceName);
			if (!urls.isEmpty()) {
				found = true;
				List<Pair<URL, byte[]>> contents = getResourceContents(urls);
				loadResourceContents(contents);
			}

			// Load the resource as a FILE (if exists)
			File file = new File(resourceName);
			if (file.exists()) {
				found = true;
				loadFileResource(file);
			}

			if (!found) {
				recordMessage("Did not find resource " + resourceName);
			}

		} catch (IOException | IllegalArgumentException ex) {
			// Most likely a "Malformed uxxxx encoding." error, which is
			// usually caused by a developer forgetting to escape backslashes
			recordException(ex);
		} finally {
			resources.pop();
		}
	}

	/**
	 * Find the resources from the class loader as there maybe more than one.
	 *
	 * @param resourceName the resource name to load
	 * @return the list of URLs from the class loader
	 * @throws IOException an IO Exception has occurred
	 */
	private List<URL> findClassLoaderResources(final String resourceName) throws IOException {

		// Try classloader - load the resources in reverse order of the enumeration.  Since later-loaded resources
		// override earlier-loaded ones, this better corresponds to the usual classpath behaviour.
		ClassLoader classloader = getParamsClassLoader();
		recordMessage("Using classloader " + classloader);

		List<URL> urls = new ArrayList<>();
		for (Enumeration<URL> res = classloader.getResources(resourceName); res.hasMoreElements();) {
			urls.add(res.nextElement());
		}
		recordMessage("Resource " + resourceName + " was found  " + urls.size() + " times");

		return urls;
	}

	/**
	 * Retrieve the resource contents.
	 *
	 * @param urls the list of URLS to load
	 * @return a list of URLs and resource contents
	 * @throws IOException an IO Exception has occurred
	 */
	private List<Pair<URL, byte[]>> getResourceContents(final List<URL> urls) throws IOException {

		// Sometimes the same URL will crop up several times (because of redundant entries in classpaths).  Also,
		// sometimes the same file appears under several URLS (because it's packaged into a jar and also a classes
		// directory, perhaps). In these circumstances we really only want to load the resource once - we load the
		// first one and then ignore later ones.
		Map<String, String> loadedFiles = new HashMap<>();

		// Build up a list of the byte arrays from the files that we then process.
		List<Pair<URL, byte[]>> contentsList = new ArrayList<>();

		// This processes from the front-of-classpath to end-of-classpath since end-of-classpath ones appear last in
		// the enumeration
		for (URL url : urls) {

			// Load the contents of the resource, for comparison with existing resources.
			byte[] urlContentBytes;
			try (InputStream urlContentStream = url.openStream(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
				copyStream(urlContentStream, baos, 2048);
				urlContentBytes = baos.toByteArray();
			}
			String urlContent = new String(urlContentBytes, StandardCharsets.UTF_8);

			// Check if we have already loaded this file.
			if (loadedFiles.containsKey(urlContent)) {
				recordMessage("Skipped url " + url + " - duplicate of " + loadedFiles.get(urlContent));
				continue;
			}

			loadedFiles.put(urlContent, url.toString());

			contentsList.add(new ImmutablePair<>(url, urlContentBytes));
		}

		return contentsList;

	}

	/**
	 * Load the resource contents.
	 *
	 * @param contentsList the list of URLs and resource content
	 * @throws IOException an IO Exception occurred
	 */
	private void loadResourceContents(final List<Pair<URL, byte[]>> contentsList) throws IOException {

		// Load in reverse order
		for (int i = contentsList.size() - 1; i >= 0; i--) {
			URL url = contentsList.get(i).getLeft();
			byte[] buff = contentsList.get(i).getRight();
			recordMessage("Loading from url " + url + "...");
			try (ByteArrayInputStream in = new ByteArrayInputStream(buff)) {
				// Use the "IncludeProperties" to load properties into us one at a time....
				IncludeProperties properties = new IncludeProperties(url.toString());
				properties.load(in);
			}
		}

	}

	/**
	 * Load the file resource.
	 *
	 * @param file the file to load
	 * @throws IOException an IO Exception occurred
	 */
	private void loadFileResource(final File file) throws IOException {

		recordMessage("Loading from file " + filename(file) + "...");

		// Use the "IncludeProperties" to load properties into us, one at a time....
		IncludeProperties properties = new IncludeProperties("file:" + filename(file));
		try (FileInputStream fin = new FileInputStream(file);
				BufferedInputStream bin = new BufferedInputStream(fin)) {
			properties.load(bin);
		}

	}

	/**
	 * Retrieves the canonical path for a given file.
	 *
	 * @param aFile the file to get the canonical path for.
	 * @return the canonical path to the given file, or <code>"UNKNOWN FILE"</code> on error.
	 */
	private String filename(final File aFile) {
		try {
			return aFile.getCanonicalPath();
		} catch (IOException ex) {
			recordException(ex);
			return "UNKNOWN FILE";
		}
	}

	/**
	 * @return the ClassLoader instance for this class.
	 */
	private ClassLoader getParamsClassLoader() {
		// Ideally we could just use the defining classloader for this class.  But unfortunately we have to deal with
		// "legacy" deployment styles where this class is visible to the container's system class loader (ie in the
		// system class path), instead of being deployed within the application.
		//
		// One idea is to use the context class loader; but iPlanet does not set this usefully, so we have to fool
		// about...

		// First, try the context class loader.
		ClassLoader loader = Thread.currentThread().getContextClassLoader();

		if (loader == null) {
			recordMessage("No context classloader had been set");
			loader = getClass().getClassLoader();
			return loader;
		}

		// Are we visible to this class loader?
		try {
			Class test = loader.loadClass(getClass().getName());

			if (test == getClass()) {
				recordMessage("Visible to ContextClassLoader");
				// Beauty - context class loader looks good
				return loader;
			} else {
				// Rats - this should not happen with a sane application server
				recordMessage("Whoa - is visible to context class loader, but it gives a different class");
				// If this happens we need to investigate further, but for the time being we'll use the context class
				// loader
				return loader;
			}
		} catch (ClassNotFoundException ex) {
			recordMessage("Not visible to context class loader (" + loader + "):" + ex.getMessage());
			loader = getClass().getClassLoader();
			return loader;
		}
	}

	/**
	 * Load the System Properties into Config.
	 */
	private void loadSystemProperties() {

		boolean overWriteOnly = getBoolean(USE_SYSTEM_OVERWRITEONLY, true);
		List<String> allowedPrefixes = getList(USE_SYSTEM_PREFIXES);

		for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {

			String key = (String) entry.getKey();
			String value = (String) entry.getValue();

			// Check for "include" keys (should not come from System Properties)
			if (INCLUDE.equals(key) || INCLUDE_AFTER.equals(key)) {
				continue;
			}

			// Check allowed prefixes
			if (!isAllowedKeyPrefix(allowedPrefixes, key)) {
				continue;
			}

			// Check overwrite only
			if (overWriteOnly && get(key) == null) {
				continue;
			}

			// Load property
			load(key, value, "System Properties");
		}
	}

	/**
	 * Check allowed prefixes.
	 *
	 * @param allowedPrefixes the list of allowed prefixes
	 * @param key the key to check
	 * @return true if the key is an allowed prefix
	 */
	private boolean isAllowedKeyPrefix(final List<String> allowedPrefixes, final String key) {

		// If no prefixes defined, then ALL keys are allowed
		if (allowedPrefixes == null || allowedPrefixes.isEmpty()) {
			return true;
		}

		// Check allowed prefixes
		for (String prefix : allowedPrefixes) {
			if (key.startsWith(prefix)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Loads a single parameter into the configuration. This handles the special directives such as "include".
	 *
	 * @param key the parameter key.
	 * @param value the parameter value.
	 * @param location the location where the parameter was defined.
	 */
	private void load(final String key, final String value, final String location) {
		// Recursive bit
		if (INCLUDE.equals(key)) {
			load(parseStringArray(value));
		} else {
			backing.put(key, value);

			if ("yes".equals(value) || "true".equals(value)) {
				booleanBacking.add(key);
			} else {
				booleanBacking.remove(key);
			}

			String history = locations.get(key);

			if (history == null) {
				history = location;
			} else {
				history = location + "; " + history;
			}

			locations.put(key, history);
		}
	}

	/**
	 * Loads the configuration from a set of files.
	 *
	 * @param subFiles the files to load from.
	 */
	private void load(final String[] subFiles) {
		for (String subFile : subFiles) {
			load(subFile);
		}
	}

	/**
	 * Logs an exception.
	 *
	 * @param throwable the exception to log.
	 */
	private void recordException(final Throwable throwable) {
		LOG.error("Error loading config. " + throwable.getMessage(), throwable);
	}

	/**
	 * Records a message in the internal log buffer.
	 *
	 * @param msg the message log.
	 */
	private void recordMessage(final String msg) {
		messages.append(msg).append('\n');
	}

	/**
	 * Clears the logged message buffer.
	 */
	private void clearMessages() {
		messages.setLength(0);
	}

	/**
	 * Iterates through the values, looking for values containing ${...} strings. For those that do, we substitute if
	 * the stuff in the {...} is a defined key.
	 *
	 * @return true if any substitutions were made, false otherwise.
	 */
	private boolean substitute() {
		boolean madeChange = false;

		for (String key : backing.keySet()) {
			madeChange = madeChange || substitute(key);
		}

		return madeChange;
	}

	/**
	 * Performs value substitution for the given key. For values containing ${...} strings, we substitute if the stuff
	 * in the {...} is a defined key.
	 *
	 * @param aKey the key to run the substitution for.
	 * @return true if a substitutions was made, false otherwise.
	 */
	private boolean substitute(final String aKey) {
		boolean madeChange = false;

		if (substituting.contains(aKey)) {
			backing.put(aKey, "");
			booleanBacking.remove(aKey);
			recordMessage("WARNING: Recursive substitution detected on parameter " + aKey);
			String history = locations.get(aKey);
			locations.put(aKey, history + "recursion detected, using null value; " + history);
			return true;
		}

		try {
			substituting.add(aKey);

			String value = backing.get(aKey);
			if (value == null) {
				return madeChange;
			}

			int start = findStartVariable(value);

			if (start == -1) {
				return madeChange;
			}

			int end = value.indexOf('}', start);

			if (end == -1) {
				return madeChange;
			}

			String variableName = value.substring(start + 2, end);

			madeChange = madeChange || substitute(variableName);

			String variableValue = get(variableName);

			if (variableValue == null) {
				return madeChange;
			}

			String newValue = value.substring(0, start) + variableValue + value.substring(end + 1);

			madeChange = true;

			backing.put(aKey, newValue);

			if ("yes".equals(newValue) || "true".equals(newValue)) {
				booleanBacking.add(aKey);
			} else {
				booleanBacking.remove(aKey);
			}

			// Record this substitution in the history
			String history = locations.get(aKey);
			history = "substitution of ${" + variableName + "}; " + history;
			locations.put(aKey, history);

			return madeChange;
		} finally {
			substituting.remove(aKey);
		}
	}

	/**
	 * Finds the start of a variable name in the given string. Variables use the "${<i>variableName</i>}" notation.
	 *
	 * @param aKey the key to search.
	 * @return the index of the start of a variable name in the given string, or -1 if not found.
	 */
	private int findStartVariable(final String aKey) {
		if (aKey == null) {
			return -1;
		}

		// Look for the first occurence of ${ in the parameter.
		int index = aKey.indexOf('$');

		for (; index >= 0; index = aKey.indexOf('$', index + 1)) {
			if (index == aKey.length() - 1) {
				continue;
			}

			if (aKey.charAt(index + 1) != '{') {
				continue;
			}

			// Ahh - got it
			break; // NOPMD
		}

		return index;
	}

	/**
	 * Copies information from the input stream to the output stream using a specified buffer size.
	 *
	 * @param in the source stream.
	 * @param out the destination stream.
	 * @param bufferSize the buffer size.
	 * @throws IOException if there is an error reading or writing to the streams.
	 */
	private static void copyStream(final InputStream in, final OutputStream out, final int bufferSize)
			throws IOException {
		final byte[] buf = new byte[bufferSize];
		int bytesRead = in.read(buf);

		while (bytesRead != -1) {
			out.write(buf, 0, bytesRead);
			bytesRead = in.read(buf);
		}
		out.flush();
	}

	/**
	 * Reload the properties to their initial state.
	 */
	public void refresh() {
		synchronized (lockObject) {
			// Now reset this object back to its initial state.
			initialiseInstanceVariables();

			// Load all the parameters from scratch.
			load();

			// Finally, notify all the listeners that have registered with this object that a change in properties has
			// occurred.
			Config.notifyListeners();
		}
	}

	/**
	 * @return a copy of the current properties
	 */
	public Properties getProperties() {
		// Don't return the backing directly; make a copy so that the caller can't change us...
		Properties copy = new Properties();
		copy.putAll(backing);
		return copy;
	}

	// -----------------------------------------------------------------------------------------------------------------
	// The rest of this class is the implementation of Configuration interface
	@Override
	public int getInt(final String key, final int defolt) {
		try {
			String value = get(key);

			if (value == null) {
				return defolt;
			}

			return Integer.parseInt(value);
		} catch (NumberFormatException ex) {
			throw new ConversionException(ex);
		}
	}

	@Override
	public int getInt(final String key) {
		return getInt(key, 0);
	}

	@Override
	public short getShort(final String key) {
		return getShort(key, (short) 0);
	}

	@Override
	public short getShort(final String key, final short defaultValue) {
		try {
			String value = get(key);

			if (value == null) {
				return defaultValue;
			}

			return Short.parseShort(value);
		} catch (NumberFormatException ex) {
			throw new ConversionException(ex);
		}
	}

	@Override
	public Short getShort(final String key, final Short defaultValue) {
		try {
			String value = get(key);

			if (value == null) {
				return defaultValue;
			}

			return Short.valueOf(value);
		} catch (NumberFormatException ex) {
			throw new ConversionException(ex);
		}
	}

	@Override
	public void addProperty(final String key, final Object value) {
		if (containsKey(key)) {
			String newValue = get(key) + ',' + (value == null ? "" : value);
			addOrModifyProperty(key, newValue);
		} else {
			addOrModifyProperty(key, value == null ? null : value.toString());
		}
	}

	@Override
	public void clear() {
		backing.clear();
		handlePropertiesChanged();
	}

	@Override
	public void clearProperty(final String key) {
		backing.remove(key);
		handlePropertiesChanged();
	}

	@Override
	public boolean containsKey(final String key) {
		if (useEnvironmentKey(key) && backing.containsKey(getEnvironmentKey(key))) {
			return true;
		}
		return backing.containsKey(key);
	}

	@Override
	public BigDecimal getBigDecimal(final String key) {
		return getBigDecimal(key, new BigDecimal("0.0"));
	}

	@Override
	public BigDecimal getBigDecimal(final String key, final BigDecimal defaultValue) {
		try {
			String value = get(key);

			if (value == null) {
				return defaultValue;
			}

			return new BigDecimal(value);
		} catch (NumberFormatException ex) {
			throw new ConversionException(ex);
		}
	}

	@Override
	public BigInteger getBigInteger(final String key) {
		return getBigInteger(key, BigInteger.ZERO);
	}

	@Override
	public BigInteger getBigInteger(final String key, final BigInteger defaultValue) {
		try {
			String value = get(key);

			if (value == null) {
				return defaultValue;
			}

			return new BigInteger(value);
		} catch (NumberFormatException ex) {
			throw new ConversionException(ex);
		}
	}

	@Override
	public boolean getBoolean(final String key) {
		if (useEnvironmentKey(key) && booleanBacking.contains(getEnvironmentKey(key))) {
			return true;
		}
		return booleanBacking.contains(key);
	}

	@Override
	public boolean getBoolean(final String key, final boolean defaultValue) {
		return containsKey(key) ? getBoolean(key) : defaultValue;
	}

	@Override
	public Boolean getBoolean(final String key, final Boolean defaultValue) {
		if (containsKey(key)) {
			return getBoolean(key);
		}
		return defaultValue;
	}

	@Override
	public byte getByte(final String key) {
		return getByte(key, (byte) 0);
	}

	@Override
	public byte getByte(final String key, final byte defaultValue) {
		try {
			String value = get(key);

			if (value == null) {
				return defaultValue;
			}

			return Byte.parseByte(value);
		} catch (NumberFormatException ex) {
			throw new ConversionException(ex);
		}
	}

	@Override
	public Byte getByte(final String key, final Byte defaultValue) {
		try {
			String value = get(key);

			if (value == null) {
				return defaultValue;
			}

			return Byte.valueOf(value);
		} catch (NumberFormatException ex) {
			throw new ConversionException(ex);
		}
	}

	@Override
	public double getDouble(final String key) {
		return getDouble(key, 0.0);
	}

	@Override
	public double getDouble(final String key, final double defaultValue) {
		try {
			String value = get(key);

			if (value == null) {
				return defaultValue;
			}

			return Double.parseDouble(value);
		} catch (NumberFormatException ex) {
			throw new ConversionException(ex);
		}
	}

	@Override
	public Double getDouble(final String key, final Double defaultValue) {
		try {
			String value = get(key);

			if (value == null) {
				return defaultValue;
			}

			return Double.valueOf(value);
		} catch (NumberFormatException ex) {
			throw new ConversionException(ex);
		}
	}

	@Override
	public float getFloat(final String key) {
		return getFloat(key, 0.0f);
	}

	@Override
	public float getFloat(final String key, final float defaultValue) {
		try {
			String value = get(key);

			if (value == null) {
				return defaultValue;
			}

			return Float.parseFloat(value);
		} catch (NumberFormatException ex) {
			throw new ConversionException(ex);
		}
	}

	@Override
	public Float getFloat(final String key, final Float defaultValue) {
		try {
			String value = get(key);

			if (value == null) {
				return defaultValue;
			}

			return Float.valueOf(value);
		} catch (NumberFormatException ex) {
			throw new ConversionException(ex);
		}
	}

	@Override
	public Integer getInteger(final String key, final Integer defaultValue) {
		try {
			String value = get(key);

			if (value == null) {
				return defaultValue;
			}

			return Integer.valueOf(value);
		} catch (NumberFormatException ex) {
			throw new ConversionException(ex);
		}
	}

	@Override
	public Iterator<String> getKeys() {
		return backing.keySet().iterator();
	}

	@Override
	public Iterator<String> getKeys(final String prefix) {
		Set<String> keys = new HashSet<>();

		for (String key : backing.keySet()) {
			if (key.startsWith(prefix)) {
				keys.add(key);
			}
		}

		return keys.iterator();
	}

	@Override
	public List getList(final String key) {
		return getList(key, new ArrayList(1));
	}

	@Override
	public List getList(final String key, final List defaultValue) {
		if (containsKey(key)) {
			return Arrays.asList(getStringArray(key));
		} else {
			return defaultValue;
		}
	}

	@Override
	public long getLong(final String key) {
		return getLong(key, 0L);
	}

	@Override
	public long getLong(final String key, final long defaultValue) {
		try {
			String value = get(key);

			if (value == null) {
				return defaultValue;
			}

			return Long.parseLong(value);
		} catch (NumberFormatException ex) {
			throw new ConversionException(ex);
		}
	}

	@Override
	public Long getLong(final String key, final Long defaultValue) {
		try {
			String value = get(key);

			if (value == null) {
				return defaultValue;
			}

			return Long.valueOf(value);
		} catch (NumberFormatException ex) {
			throw new ConversionException(ex);
		}
	}

	@Override
	public Properties getProperties(final String key) {
		String[] keyValuePairs = getStringArray(key);

		Properties props = new Properties();

		for (String pair : keyValuePairs) {
			int index = pair.indexOf('=');

			if (index < 1) {
				throw new IllegalArgumentException("Malformed property: " + pair);
			}

			props.put(pair.substring(0, index), pair.substring(index + 1, pair.length()));
		}

		return props;
	}

	@Override
	public Object getProperty(final String key) {
		return get(key);
	}

	@Override
	public String getString(final String key) {
		return get(key);
	}

	@Override
	public String getString(final String key, final String defaultValue) {
		return get(key, defaultValue);
	}

	@Override
	public String[] getStringArray(final String key) {
		String list = get(key);

		if (list == null) {
			return new String[0];
		}

		return parseStringArray(list);
	}

	@Override
	public boolean isEmpty() {
		return backing.isEmpty();
	}

	@Override
	public void setProperty(final String key, final Object value) {
		addOrModifyProperty(key, value == null ? null : value.toString());
	}

	@Override
	public Configuration subset(final String prefix) {
		return new MapConfiguration(getSubProperties(prefix, false));
	}

	/**
	 * Returns a sub-set of the parameters contained in this configuration.
	 *
	 * @param prefix the prefix of the parameter keys which should be included.
	 * @param truncate if true, the prefix is truncated in the returned properties.
	 * @return the properties sub-set, may be empty.
	 */
	protected Properties getSubProperties(final String prefix, final boolean truncate) {
		String cacheKey = truncate + prefix;
		Properties sub = subcontextCache.get(cacheKey);

		if (sub != null) {
			// make a copy so users can't change.
			Properties copy = new Properties();
			copy.putAll(sub);
			return copy;
		}

		sub = new Properties();

		int length = prefix.length();

		for (Map.Entry<String, String> entry : backing.entrySet()) {

			String key = entry.getKey();

			if (key.startsWith(prefix)) {
				// If we are truncating, remove the prefix
				String newKey = key;

				if (truncate) {
					newKey = key.substring(length);
				}

				sub.setProperty(newKey, entry.getValue());
			}
		}

		subcontextCache.put(cacheKey, sub);

		// Make a copy so users can't change.
		Properties copy = new Properties();
		copy.putAll(sub);

		return copy;
	}

	/**
	 *
	 * @param key the property key
	 * @param defolt the default value if key not available
	 * @return the property value or null
	 */
	protected String get(final String key, final String defolt) {
		String result = get(key);
		if (result == null) {
			return defolt;
		} else {
			return result;
		}
	}

	/**
	 *
	 * @param key the property key
	 * @return the property value or null
	 */
	protected String get(final String key) {
		// Check environment property
		if (useEnvironmentKey(key)) {
			String result = backing.get(getEnvironmentKey(key));
			if (result != null) {
				return result;
			}
		}
		return backing.get(key);
	}

	/**
	 * Add or Modify a property at runtime.
	 *
	 * @param name the property name
	 * @param value the property value
	 *
	 */
	protected void addOrModifyProperty(final String name, final String value) {
		if (name == null) {
			throw new IllegalArgumentException("name parameter can not be null.");
		}

		if (name.length() < 1) {
			throw new IllegalArgumentException("name parameter can not be the empty String.");
		}

		if (value == null) {
			throw new IllegalArgumentException("value parameter can not be null.");
		}

		recordMessage("modifyProperties() - Adding property '" + name + "' with the value '" + value + "'.");

		runtimeProperties.setProperty(name, value);

		handlePropertiesChanged();
	}

	/**
	 * Handle a property change.
	 */
	protected void handlePropertiesChanged() {
		// clear the subContext cache, it's now invalid
		subcontextCache.clear();
		// Check if environment changed
		checkEnvironmentProperty();
	}

	/**
	 * Check if the environment property has been set.
	 */
	protected void checkEnvironmentProperty() {
		String env = backing.get(ENVIRONMENT_PROPERTY);
		currentEnvironment = (env == null || env.isEmpty()) ? null : env;
	}

	/**
	 * @param key the property key
	 * @return true if check environment suffix
	 */
	protected boolean useEnvironmentKey(final String key) {
		// Has environment and is not the environment property
		return currentEnvironment != null && !ENVIRONMENT_PROPERTY.equals(key);
	}

	/**
	 * @param key the property key
	 * @return the property key with the environment suffix
	 */
	protected String getEnvironmentKey(final String key) {
		return key + "." + currentEnvironment;
	}

	/**
	 * A helper class for properties which are being loaded.
	 */
	class IncludeProperties extends Properties {

		/**
		 * The properties file location (if applicable).
		 */
		private final String location;

		/**
		 * Creates an IncludeProperties, which has not being sourced externally.
		 */
		IncludeProperties() {
			this("Modified at Runtime");
		}

		/**
		 * Creates an IncludeProperties, which will be sourced from the given location.
		 *
		 * @param aLocation the location of the external properties.
		 */
		IncludeProperties(final String aLocation) {
			location = aLocation;
		}

		/**
		 * Adds a value to the properties set. This has been overridden to support the Configuration extensions (e.g.
		 * the "include" directive).
		 *
		 * @param aKey the key to add
		 * @param aValue the value to add
		 * @return the old value for the key, or null if there was no previously associated value.
		 */
		@Override
		public synchronized Object put(final Object aKey, final Object aValue) {
			String key = (String) aKey;
			String value = (String) aValue;

			// Act on "include" directives immediately
			if (INCLUDE.equals(key)) {
				DefaultConfiguration.this.load(parseStringArray(value));
				return value;
			} else {
				// Check for a trailing "+" sign on the key (or a leading "+= on the value")
				boolean append = false;

				if (key.endsWith("+")) {
					key = key.substring(0, key.length() - 1);
					append = true;
				} else if (value != null && value.startsWith("+=")) {
					// If the line contained "key += value" then the Properties will have parsed this as 'key'
					// and '+= value'
					value = value.substring(2).trim();
					append = true;
				}

				if (append) {
					String already = DefaultConfiguration.this.get(key);

					// If there is no value already, strip off the leading comma, otherwise append.
					value = (already != null ? already + "," + value : value);
				}

				DefaultConfiguration.this.load(key, value, location);

				return super.put(key, value);
			}
		}

		@Override
		public synchronized int hashCode() {
			int hash = 5;
			hash = 97 * hash + Objects.hashCode(this.location);
			return hash;
		}

		@Override
		public synchronized boolean equals(final Object obj) {
			return obj instanceof IncludeProperties && Objects.equals(this.location, ((IncludeProperties) obj).location);
		}

	}

}
