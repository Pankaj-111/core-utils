package com.github.pankaj111.coreutils.clone;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A utility class for performing deep cloning of objects, including complex
 * object graphs with circular references. This class handles arrays,
 * collections, maps, and custom objects while providing flexibility to ignore
 * specific fields during cloning.
 *
 * <p>
 * <b>Features:</b>
 * <ul>
 * <li>Handles circular references using identity tracking</li>
 * <li>Supports arrays, collections, maps, and custom objects</li>
 * <li>Automatically skips immutable types (String, primitives, etc.)</li>
 * <li>Configurable field exclusion</li>
 * <li>Performance optimized with reflection caching</li>
 * <li>Thread-safe field cache</li>
 * </ul>
 *
 * <p>
 * <b>Usage Examples:</b>
 * 
 * <pre>{@code
 * // Basic deep clone
 * MyClass original = new MyClass();
 * MyClass cloned = DeepCloneUtils.deepClone(original);
 *
 * // Clone with ignored fields
 * MyClass cloned = DeepCloneUtils.deepClone(original, "id", "timestamp");
 *
 * // Clone with configuration
 * DeepCloneUtils.CloneConfig config = DeepCloneUtils.CloneConfig.builder().ignoreField("id").ignoreField("createdDate")
 * 		.build();
 * MyClass cloned = DeepCloneUtils.deepClone(original, config);
 * }</pre>
 *
 * <p>
 * <b>Limitations:</b>
 * <ul>
 * <li>Classes must have a no-args constructor</li>
 * <li>Transient fields are not cloned</li>
 * <li>Static fields are not cloned</li>
 * <li>Final fields might not be modifiable in some cases</li>
 * </ul>
 *
 * @author Pankaj Singh
 * @version 1.0
 * @see CloneConfig
 */
public final class DeepCloneUtils {

	/**
	 * Private constructor to prevent instantiation of utility class.
	 * 
	 * @throws AssertionError always when called
	 */
	private DeepCloneUtils() {
		throw new AssertionError("Cannot instantiate utility class");
	}

	/**
	 * Set of immutable types that don't require cloning. Includes wrapper types,
	 * String, temporal types, and mathematical types.
	 */
	private static final Set<Class<?>> IMMUTABLES = Set.of(String.class, Integer.class, Long.class, Double.class,
			Float.class, Boolean.class, Byte.class, Short.class, Character.class, Void.class, java.time.LocalDate.class,
			java.time.LocalDateTime.class, java.time.LocalTime.class, java.time.Instant.class, java.time.Duration.class,
			java.time.Period.class, java.math.BigInteger.class, java.math.BigDecimal.class, java.util.UUID.class,
			java.net.URL.class, java.net.URI.class);

	/**
	 * Cache for field reflection to improve performance. Uses ConcurrentHashMap for
	 * thread-safe access.
	 */
	private static final Map<Class<?>, List<Field>> FIELD_CACHE = new ConcurrentHashMap<>();

	/**
	 * Performs a deep clone of the specified object, optionally ignoring specific
	 * fields.
	 *
	 * <p>
	 * This method handles:
	 * <ul>
	 * <li>Null values - returns null</li>
	 * <li>Immutable types - returns the original instance</li>
	 * <li>Arrays - creates a new array with cloned elements</li>
	 * <li>Collections - creates appropriate collection type with cloned
	 * elements</li>
	 * <li>Maps - creates appropriate map type with cloned keys and values</li>
	 * <li>Custom objects - creates new instance and recursively clones fields</li>
	 * </ul>
	 *
	 * @param <T>          the type of the object to clone
	 * @param object       the object to deep clone, may be null
	 * @param ignoreFields the names of fields to ignore during cloning
	 * @return a deep clone of the original object, or null if the input was null
	 * @throws RuntimeException if cloning fails due to:
	 *                          <ul>
	 *                          <li>No accessible no-args constructor</li>
	 *                          <li>Security restrictions on field access</li>
	 *                          <li>Other reflection-related issues</li>
	 *                          </ul>
	 *
	 * @example
	 * 
	 *          <pre>{@code
	 * Person original = new Person("John", 30);
	 * Person cloned = DeepCloneUtils.deepClone(original, "age");
	 * // The 'age' field will not be cloned and will have default value in the clone
	 * }</pre>
	 */
	public static <T> T deepClone(T object, String... ignoreFields) {
		return deepClone(object, new IdentityHashMap<>(), new HashSet<>(Arrays.asList(ignoreFields)));
	}

	/**
	 * Performs a deep clone of the specified object using the provided
	 * configuration.
	 *
	 * <p>
	 * This method provides more flexible configuration options through the
	 * {@link CloneConfig} class, allowing for builder-style configuration of
	 * ignored fields.
	 *
	 * @param <T>    the type of the object to clone
	 * @param object the object to deep clone, may be null
	 * @param config the cloning configuration specifying which fields to ignore
	 * @return a deep clone of the original object, or null if the input was null
	 * @throws RuntimeException if cloning fails
	 *
	 * @see CloneConfig
	 * @example
	 * 
	 *          <pre>{@code
	 * CloneConfig config = CloneConfig.builder()
	 *     .ignoreField("id")
	 *     .ignoreField("createdDate")
	 *     .build();
	 * Person cloned = DeepCloneUtils.deepClone(original, config);
	 * }</pre>
	 */
	public static <T> T deepClone(T object, CloneConfig config) {
		return deepClone(object, new IdentityHashMap<>(), config.getIgnoreFields());
	}

	/**
	 * Internal recursive method that performs the actual deep cloning.
	 *
	 * @param <T>          the type of object being cloned
	 * @param object       the object to clone
	 * @param clones       identity map to track already cloned objects and prevent
	 *                     circular references
	 * @param ignoreFields set of field names to ignore during cloning
	 * @return the cloned object
	 */
	@SuppressWarnings("unchecked")
	private static <T> T deepClone(T object, Map<Object, Object> clones, Set<String> ignoreFields) {
		if (object == null)
			return null;

		Class<?> clazz = object.getClass();

		// Return directly for immutable types
		if (isImmutable(clazz)) {
			return object;
		}

		// Return already cloned object to avoid circular references
		if (clones.containsKey(object)) {
			return (T) clones.get(object);
		}

		try {
			Object clone;

			// Handle arrays
			if (clazz.isArray()) {
				return (T) cloneArray(object, clones, ignoreFields);
			}

			// Handle collections
			if (object instanceof Collection) {
				return (T) cloneCollection((Collection<?>) object, clones, ignoreFields);
			}

			// Handle maps
			if (object instanceof Map) {
				return (T) cloneMap((Map<?, ?>) object, clones, ignoreFields);
			}

			// Handle Optional
			if (object instanceof Optional) {
				return (T) cloneOptional((Optional<?>) object, clones, ignoreFields);
			}

			// Handle regular objects
			clone = createInstance(clazz);
			clones.put(object, clone);
			cloneFields(object, clone, clazz, clones, ignoreFields);

			return (T) clone;

		} catch (Exception e) {
			throw new RuntimeException("Failed to deep clone object of type: " + clazz.getName(), e);
		}
	}

	/**
	 * Determines if a class represents an immutable type that doesn't require
	 * cloning.
	 *
	 * @param clazz the class to check
	 * @return true if the class is immutable, false otherwise
	 */
	private static boolean isImmutable(Class<?> clazz) {
		return IMMUTABLES.contains(clazz) || clazz.isEnum() || clazz.isPrimitive()
				|| (clazz.getPackage() != null && clazz.getPackage().getName().startsWith("java.time"));
	}

	/**
	 * Creates a deep clone of an array.
	 *
	 * @param array        the array to clone
	 * @param clones       identity map for circular reference tracking
	 * @param ignoreFields set of field names to ignore
	 * @return a new array with cloned elements
	 */
	private static Object cloneArray(Object array, Map<Object, Object> clones, Set<String> ignoreFields) {
		int length = Array.getLength(array);
		Object newArray = Array.newInstance(array.getClass().getComponentType(), length);
		clones.put(array, newArray);

		for (int i = 0; i < length; i++) {
			Object element = Array.get(array, i);
			Array.set(newArray, i, deepClone(element, clones, ignoreFields));
		}
		return newArray;
	}

	/**
	 * Creates a deep clone of a collection, preserving the original collection
	 * type.
	 *
	 * @param collection   the collection to clone
	 * @param clones       identity map for circular reference tracking
	 * @param ignoreFields set of field names to ignore
	 * @return a new collection of the appropriate type with cloned elements
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Collection<?> cloneCollection(Collection<?> collection, Map<Object, Object> clones,
			Set<String> ignoreFields) {
		Collection<Object> copy;

		if (collection instanceof List) {
			copy = collection instanceof LinkedList ? new LinkedList<>() : new ArrayList<>(collection.size());
		} else if (collection instanceof SortedSet) {
			copy = new TreeSet<>(((SortedSet) collection).comparator());
		} else if (collection instanceof Set) {
			copy = new HashSet<>(collection.size());
		} else {
			copy = new ArrayList<>(collection.size());
		}

		clones.put(collection, copy);
		for (Object item : collection) {
			copy.add(deepClone(item, clones, ignoreFields));
		}
		return copy;
	}

	/**
	 * Creates a deep clone of a map, preserving the original map type and
	 * comparator.
	 *
	 * @param map          the map to clone
	 * @param clones       identity map for circular reference tracking
	 * @param ignoreFields set of field names to ignore
	 * @return a new map of the appropriate type with cloned keys and values
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Map<?, ?> cloneMap(Map<?, ?> map, Map<Object, Object> clones, Set<String> ignoreFields) {
		Map<Object, Object> copy;

		if (map instanceof SortedMap) {
			copy = new TreeMap<>(((SortedMap) map).comparator());
		} else if (map instanceof LinkedHashMap) {
			copy = new LinkedHashMap<>(map.size());
		} else {
			copy = new HashMap<>(map.size());
		}

		clones.put(map, copy);
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			Object key = deepClone(entry.getKey(), clones, ignoreFields);
			Object value = deepClone(entry.getValue(), clones, ignoreFields);
			copy.put(key, value);
		}
		return copy;
	}

	/**
	 * Creates a deep clone of an Optional, cloning the contained value if present.
	 *
	 * @param optional     the Optional to clone
	 * @param clones       identity map for circular reference tracking
	 * @param ignoreFields set of field names to ignore
	 * @return a new Optional with cloned value if present, or empty Optional
	 */
	private static Optional<?> cloneOptional(Optional<?> optional, Map<Object, Object> clones,
			Set<String> ignoreFields) {
		return optional.map(value -> deepClone(value, clones, ignoreFields));
	}

	/**
	 * Creates a new instance of the specified class using the no-args constructor.
	 *
	 * @param clazz the class to instantiate
	 * @return a new instance of the class
	 * @throws RuntimeException if the class doesn't have a no-args constructor or
	 *                          if instantiation fails
	 */
	private static Object createInstance(Class<?> clazz) throws Exception {
		try {
			return clazz.getDeclaredConstructor().newInstance();
		} catch (NoSuchMethodException e) {
			throw new RuntimeException("Class " + clazz.getName() + " does not have a no-args constructor", e);
		} catch (Exception e) {
			throw new RuntimeException("Failed to create instance of " + clazz.getName(), e);
		}
	}

	/**
	 * Clones all fields from source object to target object.
	 *
	 * @param source       the source object to copy fields from
	 * @param target       the target object to copy fields to
	 * @param clazz        the class being processed
	 * @param clones       identity map for circular reference tracking
	 * @param ignoreFields set of field names to ignore
	 * @throws IllegalAccessException if field access is denied
	 */
	private static void cloneFields(Object source, Object target, Class<?> clazz, Map<Object, Object> clones,
			Set<String> ignoreFields) throws IllegalAccessException {

		for (Field field : getCachedFields(clazz)) {
			if (ignoreFields.contains(field.getName()) || Modifier.isStatic(field.getModifiers())
					|| Modifier.isTransient(field.getModifiers())) {
				continue;
			}

			field.setAccessible(true);
			Object fieldValue = field.get(source);
			field.set(target, deepClone(fieldValue, clones, ignoreFields));
		}
	}

	/**
	 * Retrieves cached fields for a class, computing them if not already cached.
	 *
	 * @param clazz the class to get fields for
	 * @return list of all non-static, non-transient fields for the class hierarchy
	 */
	private static List<Field> getCachedFields(Class<?> clazz) {
		return FIELD_CACHE.computeIfAbsent(clazz, DeepCloneUtils::getAllFields);
	}

	/**
	 * Retrieves all non-static fields from the class hierarchy (excluding
	 * Object.class).
	 *
	 * @param clazz the class to get fields for
	 * @return list of all non-static fields from the class and its superclasses
	 */
	private static List<Field> getAllFields(Class<?> clazz) {
		List<Field> fields = new ArrayList<>();
		Class<?> currentClass = clazz;

		while (currentClass != null && currentClass != Object.class) {
			for (Field field : currentClass.getDeclaredFields()) {
				if (!Modifier.isStatic(field.getModifiers())) {
					fields.add(field);
				}
			}
			currentClass = currentClass.getSuperclass();
		}
		return fields;
	}

	/**
	 * Configuration class for deep cloning operations. Provides a builder pattern
	 * for configuring which fields to ignore during cloning.
	 *
	 * @example
	 * 
	 *          <pre>{@code
	 * CloneConfig config = CloneConfig.builder()
	 *     .ignoreField("id")
	 *     .ignoreField("timestamp")
	 *     .build();
	 * }</pre>
	 */
	public static class CloneConfig {
		private final Set<String> ignoreFields;

		/**
		 * Creates a new CloneConfig with the specified ignored fields.
		 *
		 * @param ignoreFields the set of field names to ignore during cloning
		 */
		private CloneConfig(Set<String> ignoreFields) {
			this.ignoreFields = Set.copyOf(ignoreFields);
		}

		/**
		 * Gets the set of field names that should be ignored during cloning.
		 *
		 * @return an unmodifiable set of field names to ignore
		 */
		public Set<String> getIgnoreFields() {
			return ignoreFields;
		}

		/**
		 * Creates a new builder for constructing CloneConfig instances.
		 *
		 * @return a new CloneConfig builder
		 */
		public static Builder builder() {
			return new Builder();
		}

		/**
		 * Builder class for creating CloneConfig instances with a fluent API.
		 */
		public static class Builder {
			private final Set<String> ignoreFields = new HashSet<>();

			/**
			 * Adds a field name to the ignore list.
			 *
			 * @param fieldName the name of the field to ignore during cloning
			 * @return this builder for method chaining
			 */
			public Builder ignoreField(String fieldName) {
				this.ignoreFields.add(fieldName);
				return this;
			}

			/**
			 * Adds multiple field names to the ignore list.
			 *
			 * @param fieldNames the names of fields to ignore during cloning
			 * @return this builder for method chaining
			 */
			public Builder ignoreFields(String... fieldNames) {
				this.ignoreFields.addAll(Arrays.asList(fieldNames));
				return this;
			}

			/**
			 * Builds the CloneConfig instance with the configured settings.
			 *
			 * @return a new immutable CloneConfig instance
			 */
			public CloneConfig build() {
				return new CloneConfig(ignoreFields);
			}
		}
	}
}