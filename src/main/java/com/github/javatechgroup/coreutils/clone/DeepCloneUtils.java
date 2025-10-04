package com.github.javatechgroup.coreutils.clone;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A high-performance deep cloning utility for modern Java applications (JDK
 * 17+).
 * 
 * <p>
 * This utility provides comprehensive deep cloning capabilities for complex
 * object graphs, including handling of circular references, collections,
 * arrays, and custom objects. It uses reflection with intelligent caching for
 * optimal performance.
 * </p>
 *
 * <h2>Key Features</h2>
 * <ul>
 * <li>🔁 Handles circular references using identity tracking</li>
 * <li>📚 Supports arrays, collections, maps, and Optionals</li>
 * <li>⚡ Automatically skips immutable types for performance</li>
 * <li>🎯 Configurable field exclusion</li>
 * <li>💾 Performance optimized with reflection caching</li>
 * <li>🛡️ Thread-safe implementation</li>
 * <li>🚀 Multiple constructor strategies for flexible instantiation</li>
 * </ul>
 *
 * <h2>Usage Examples</h2>
 * 
 * <pre>{@code
 * // Basic deep clone
 * Person original = new Person("John", 30);
 * Person cloned = DeepCloneUtils.deepClone(original);
 *
 * // Clone with ignored fields
 * Person cloned = DeepCloneUtils.deepClone(original, "id", "createdDate");
 *
 * // Clone with configuration builder
 * DeepCloneUtils.CloneConfig config = DeepCloneUtils.CloneConfig.builder().ignoreField("id").ignoreField("version")
 * 		.build();
 * Person cloned = DeepCloneUtils.deepClone(original, config);
 * }</pre>
 *
 * <h2>Supported Types</h2>
 * <ul>
 * <li><b>Immutable</b>: String, primitives, wrappers, java.time, UUID,
 * etc.</li>
 * <li><b>Collections</b>: List, Set, Map, Queue, Deque and their
 * implementations</li>
 * <li><b>Arrays</b>: All array types (primitive and object)</li>
 * <li><b>Optionals</b>: Optional, OptionalInt, OptionalLong,
 * OptionalDouble</li>
 * <li><b>Custom Objects</b>: Any class with accessible constructor</li>
 * </ul>
 *
 * <h2>Limitations</h2>
 * <ul>
 * <li>Transient fields are not cloned</li>
 * <li>Static fields are not cloned</li>
 * <li>Final fields might not be modifiable in some cases</li>
 * <li>Requires some form of accessible constructor</li>
 * <li>May not work with certain security managers</li>
 * </ul>
 *
 * @author Pankaj Singh
 * @version 2.1
 * @see CloneConfig
 */
public final class DeepCloneUtils {

	/**
	 * Private constructor to prevent instantiation.
	 * 
	 * @throws AssertionError always when called
	 */
	private DeepCloneUtils() {
		throw new AssertionError("Cannot instantiate utility class");
	}

	/**
	 * Immutable types that don't require cloning. These types are returned as-is
	 * for performance and correctness.
	 */
	private static final Set<Class<?>> IMMUTABLE_TYPES = Set.of(String.class, Integer.class, Long.class, Double.class,
			Float.class, Boolean.class, Byte.class, Short.class, Character.class, Void.class, java.time.LocalDate.class,
			java.time.LocalDateTime.class, java.time.LocalTime.class, java.time.Instant.class, java.time.Duration.class,
			java.time.Period.class, java.math.BigInteger.class, java.math.BigDecimal.class, UUID.class,
			java.net.URL.class, java.net.URI.class);

	/**
	 * Common collection types that are known to have no-args constructors. Used for
	 * fast-path instantiation.
	 */
	private static final Set<Class<?>> COMMON_COLLECTIONS = Set.of(ArrayList.class, LinkedList.class, HashSet.class,
			LinkedHashSet.class, TreeSet.class, HashMap.class, LinkedHashMap.class, TreeMap.class,
			IdentityHashMap.class, WeakHashMap.class, ConcurrentHashMap.class, ArrayDeque.class, PriorityQueue.class);

	/**
	 * Cache for field reflection to improve performance. Thread-safe and computed
	 * once per class.
	 */
	private static final Map<Class<?>, List<Field>> FIELD_CACHE = new ConcurrentHashMap<>();

	/**
	 * Performs a deep clone of the specified object.
	 *
	 * <p>
	 * This method handles null values, immutable types, arrays, collections, maps,
	 * Optionals, and custom objects with circular reference detection.
	 * </p>
	 *
	 * @param <T>          the type of the object to clone
	 * @param object       the object to deep clone, may be null
	 * @param ignoreFields the names of fields to ignore during cloning
	 * @return a deep clone of the original object, or null if input was null
	 * @throws RuntimeException if cloning fails due to:
	 *                          <ul>
	 *                          <li>No accessible constructor</li>
	 *                          <li>Security restrictions</li>
	 *                          <li>Other reflection-related issues</li>
	 *                          </ul>
	 * 
	 * @example
	 * 
	 *          <pre>{@code
	 * // Clone ignoring specific fields
	 * Person original = new Person("John", new Address("123 Main St"));
	 * Person cloned = DeepCloneUtils.deepClone(original, "id", "createdDate");
	 * 
	 * // The cloned object is a deep copy with 'id' and 'createdDate' fields
	 * // set to their default values (null for objects, 0 for primitives)
	 * }</pre>
	 */
	public static <T> T deepClone(T object, String... ignoreFields) {
		return deepClone(object, new IdentityHashMap<>(), new HashSet<>(Arrays.asList(ignoreFields)));
	}

	/**
	 * Performs a deep clone using the provided configuration.
	 *
	 * <p>
	 * This method provides more flexible configuration options through the
	 * {@link CloneConfig} class, allowing for builder-style configuration.
	 * </p>
	 *
	 * @param <T>    the type of the object to clone
	 * @param object the object to deep clone, may be null
	 * @param config the cloning configuration specifying which fields to ignore
	 * @return a deep clone of the original object, or null if input was null
	 * @throws RuntimeException if cloning fails
	 * 
	 * @see CloneConfig
	 * @example
	 * 
	 *          <pre>{@code
	 * CloneConfig config = CloneConfig.builder()
	 *     .ignoreField("id")
	 *     .ignoreField("version")
	 *     .ignoreField("auditInfo")
	 *     .build();
	 * Person cloned = DeepCloneUtils.deepClone(original, config);
	 * }</pre>
	 */
	public static <T> T deepClone(T object, CloneConfig config) {
		return deepClone(object, new IdentityHashMap<>(), config.getIgnoreFields());
	}

	/**
	 * Internal recursive cloning implementation.
	 *
	 * @param <T>          the type of object being cloned
	 * @param object       the object to clone
	 * @param clones       identity map for circular reference tracking
	 * @param ignoreFields set of field names to ignore
	 * @return the cloned object
	 */
	@SuppressWarnings("unchecked")
	private static <T> T deepClone(T object, Map<Object, Object> clones, Set<String> ignoreFields) {
		// Handle null input
		if (object == null) {
			return null;
		}

		Class<?> clazz = object.getClass();

		// Return immutable types directly (performance optimization)
		if (isImmutable(clazz)) {
			return object;
		}

		// Handle circular references - return already cloned instance
		if (clones.containsKey(object)) {
			return (T) clones.get(object);
		}

		try {
			// Dispatch to appropriate cloning strategy based on type
			if (clazz.isArray()) {
				return (T) cloneArray(object, clones, ignoreFields);
			}
			if (object instanceof Collection) {
				return (T) cloneCollection((Collection<?>) object, clones, ignoreFields);
			}
			if (object instanceof Map) {
				return (T) cloneMap((Map<?, ?>) object, clones, ignoreFields);
			}
			if (object instanceof Optional) {
				return (T) cloneOptional((Optional<?>) object, clones, ignoreFields);
			}

			// Handle regular objects - most complex case
			Object clone = createInstance(clazz);
			clones.put(object, clone);
			cloneFields(object, clone, clazz, clones, ignoreFields);
			return (T) clone;

		} catch (Exception e) {
			throw new RuntimeException("Failed to deep clone object of type: " + clazz.getName(), e);
		}
	}

	/**
	 * Determines if a class represents an immutable type.
	 *
	 * @param clazz the class to check
	 * @return true if the class is immutable and doesn't require cloning
	 */
	private static boolean isImmutable(Class<?> clazz) {
		return clazz.isEnum() || clazz.isPrimitive() || IMMUTABLE_TYPES.contains(clazz)
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
	 * Creates a deep clone of a collection.
	 *
	 * @param original     the collection to clone
	 * @param clones       identity map for circular reference tracking
	 * @param ignoreFields set of field names to ignore
	 * @return a new collection of the appropriate type with cloned elements
	 */
	private static Collection<?> cloneCollection(Collection<?> original, Map<Object, Object> clones,
			Set<String> ignoreFields) {
		Collection<Object> copy = createCollectionInstance(original);
		clones.put(original, copy);

		for (Object item : original) {
			copy.add(deepClone(item, clones, ignoreFields));
		}
		return copy;
	}

	/**
	 * Creates a deep clone of a map.
	 *
	 * @param original     the map to clone
	 * @param clones       identity map for circular reference tracking
	 * @param ignoreFields set of field names to ignore
	 * @return a new map of the appropriate type with cloned keys and values
	 */
	private static Map<?, ?> cloneMap(Map<?, ?> original, Map<Object, Object> clones, Set<String> ignoreFields) {
		Map<Object, Object> copy = createMapInstance(original);
		clones.put(original, copy);

		for (Map.Entry<?, ?> entry : original.entrySet()) {
			Object key = deepClone(entry.getKey(), clones, ignoreFields);
			Object value = deepClone(entry.getValue(), clones, ignoreFields);
			copy.put(key, value);
		}
		return copy;
	}

	/**
	 * Creates a deep clone of an Optional.
	 *
	 * @param optional     the Optional to clone
	 * @param clones       identity map for circular reference tracking
	 * @param ignoreFields set of field names to ignore
	 * @return a new Optional with cloned value if present, or empty Optional
	 */
	private static Optional<?> cloneOptional(Optional<?> optional, Map<Object, Object> clones,
			Set<String> ignoreFields) {
		if (optional.isEmpty()) {
			return Optional.empty();
		}
		return optional.map(value -> deepClone(value, clones, ignoreFields));
	}

	/**
	 * Clones all non-static, non-transient fields from source to target object.
	 * Handles synthetic fields and final fields gracefully.
	 */
	private static void cloneFields(Object source, Object target, Class<?> clazz, Map<Object, Object> clones,
			Set<String> ignoreFields) throws IllegalAccessException {
		for (Field field : getCachedFields(clazz)) {
			// Skip fields that should not be cloned
			if (ignoreFields.contains(field.getName()) || Modifier.isStatic(field.getModifiers())
					|| Modifier.isTransient(field.getModifiers()) || field.isSynthetic()) { // ADD THIS LINE - skip
																							// synthetic fields
				continue;
			}

			// Ensure field is accessible
			if (!field.canAccess(source)) {
				field.setAccessible(true);
			}

			// Skip final fields that cannot be modified (except primitives and wrappers)
			if (Modifier.isFinal(field.getModifiers())) {
				// Only attempt to set final fields for primitive/wrapper types
				// as they might be initialized to default values
				Class<?> fieldType = field.getType();
				if (!fieldType.isPrimitive() && !IMMUTABLE_TYPES.contains(fieldType) && field.get(source) != null) {
					continue; // Skip non-primitive final fields with non-null values
				}
			}

			try {
				// Recursively clone field value
				Object fieldValue = field.get(source);
				field.set(target, deepClone(fieldValue, clones, ignoreFields));
			} catch (IllegalAccessException e) {
				// If we can't set the field (e.g., final field in some cases), skip it
				System.err.println("Warning: Could not clone field '" + field.getName() + "' of type " + clazz.getName()
						+ ": " + e.getMessage());
			}
		}
	}

	/**
	 * Retrieves cached fields for a class, computing them if not cached.
	 *
	 * @param clazz the class to get fields for
	 * @return list of all non-static, non-transient fields for the class hierarchy
	 */
	private static List<Field> getCachedFields(Class<?> clazz) {
		return FIELD_CACHE.computeIfAbsent(clazz, DeepCloneUtils::getAllFields);
	}

	/**
	 * Retrieves all non-static fields from the class hierarchy.
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
	 * Creates an instance of the specified class using multiple strategies.
	 *
	 * <p>
	 * Attempts the following strategies in order:
	 * <ol>
	 * <li>No-args constructor</li>
	 * <li>Any constructor with default values for parameters</li>
	 * </ol>
	 *
	 * @param clazz the class to instantiate
	 * @return a new instance of the class
	 * @throws RuntimeException if no usable constructor is found
	 */
	private static Object createInstance(Class<?> clazz) throws Exception {
		// Strategy 1: Try no-args constructor (preferred)
		try {
			Constructor<?> constructor = clazz.getDeclaredConstructor();
			if (!constructor.canAccess(null)) {
				constructor.setAccessible(true);
			}
			return constructor.newInstance();
		} catch (NoSuchMethodException e) {
			// Strategy 2: Try any constructor with default parameter values
			for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
				try {
					if (!constructor.canAccess(null)) {
						constructor.setAccessible(true);
					}

					Class<?>[] paramTypes = constructor.getParameterTypes();
					Object[] args = new Object[paramTypes.length];

					// Provide sensible default values for parameters
					for (int i = 0; i < paramTypes.length; i++) {
						args[i] = getDefaultValue(paramTypes[i]);
					}

					return constructor.newInstance(args);
				} catch (Exception ignored) {
					// Try next constructor
				}
			}

			throw new RuntimeException("No usable constructor found for: " + clazz.getName()
					+ ". Available constructors: " + Arrays.toString(clazz.getDeclaredConstructors()));
		}
	}

	/**
	 * Provides default values for common types used in constructor parameters.
	 *
	 * @param type the parameter type
	 * @return a sensible default value for the type
	 */
	private static Object getDefaultValue(Class<?> type) {
		if (type.isPrimitive()) {
			if (type == boolean.class)
				return false;
			if (type == byte.class)
				return (byte) 0;
			if (type == short.class)
				return (short) 0;
			if (type == int.class)
				return 0;
			if (type == long.class)
				return 0L;
			if (type == float.class)
				return 0.0f;
			if (type == double.class)
				return 0.0;
			if (type == char.class)
				return '\0';
		} else {
			// Common wrapper types and other frequently used types
			if (type == Boolean.class)
				return Boolean.FALSE;
			if (type == Byte.class)
				return (byte) 0;
			if (type == Short.class)
				return (short) 0;
			if (type == Integer.class)
				return 0;
			if (type == Long.class)
				return 0L;
			if (type == Float.class)
				return 0.0f;
			if (type == Double.class)
				return 0.0;
			if (type == Character.class)
				return '\0';
			if (type == String.class)
				return "";
		}
		return null; // For all other object types
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Collection<Object> createCollectionInstance(Collection<?> original) {
		Class<?> clazz = original.getClass();

		// Fast path for common collection types
		if (COMMON_COLLECTIONS.contains(clazz)) {
			try {
				Constructor<?> constructor = clazz.getDeclaredConstructor();
				constructor.setAccessible(true);
				Collection<Object> instance = (Collection<Object>) constructor.newInstance();

				// Special handling for TreeSet to preserve comparator
				if (original instanceof SortedSet && instance instanceof TreeSet) {
					SortedSet sortedOriginal = (SortedSet) original;
					// We can't set comparator after creation, so we need to create a new one
					return new TreeSet<>(sortedOriginal.comparator());
				}

				return instance;
			} catch (Exception e) {
				// Fall through to general case
			}
		}

		// General case with type-specific fallbacks
		try {
			Constructor<?> constructor = clazz.getDeclaredConstructor();
			constructor.setAccessible(true);
			Collection<Object> instance = (Collection<Object>) constructor.newInstance();

			// Special handling for TreeSet to preserve comparator
			if (original instanceof SortedSet && instance instanceof TreeSet) {
				SortedSet sortedOriginal = (SortedSet) original;
				return new TreeSet<>(sortedOriginal.comparator());
			}

			return instance;
		} catch (Exception e) {
			// Fallback to appropriate collection type based on interface
			if (original instanceof SortedSet) {
				SortedSet sortedSet = (SortedSet) original;
				return new TreeSet<>(sortedSet.comparator()); // PRESERVE COMPARATOR
			}
			if (original instanceof List) {
				return new ArrayList<>(original.size());
			}
			if (original instanceof Set) {
				return new HashSet<>(original.size());
			}
			if (original instanceof Deque) {
				return new ArrayDeque<>(original.size());
			}
			if (original instanceof Queue) {
				return new LinkedList<>();
			}
			return new ArrayList<>(original.size());
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Map<Object, Object> createMapInstance(Map<?, ?> original) {
		Class<?> clazz = original.getClass();

		// Fast path for common map types
		if (COMMON_COLLECTIONS.contains(clazz)) {
			try {
				Constructor<?> constructor = clazz.getDeclaredConstructor();
				constructor.setAccessible(true);
				Map<Object, Object> instance = (Map<Object, Object>) constructor.newInstance();

				// Special handling for TreeMap to preserve comparator
				if (original instanceof SortedMap && instance instanceof TreeMap) {
					SortedMap sortedOriginal = (SortedMap) original;
					return new TreeMap(sortedOriginal.comparator());
				}

				return instance;
			} catch (Exception e) {
				// Fall through to general case
			}
		}

		// General case with type-specific fallbacks
		try {
			Constructor<?> constructor = clazz.getDeclaredConstructor();
			constructor.setAccessible(true);
			Map<Object, Object> instance = (Map<Object, Object>) constructor.newInstance();

			// Special handling for TreeMap to preserve comparator
			if (original instanceof SortedMap && instance instanceof TreeMap) {
				SortedMap sortedOriginal = (SortedMap) original;
				return new TreeMap(sortedOriginal.comparator());
			}

			return instance;
		} catch (Exception e) {
			// Fallback to appropriate map type based on interface
			if (original instanceof SortedMap) {
				SortedMap sortedMap = (SortedMap) original;
				return new TreeMap(sortedMap.comparator()); // PRESERVE COMPARATOR
			}
			if (original instanceof LinkedHashMap) {
				return new LinkedHashMap<>(original.size());
			}
			if (original instanceof IdentityHashMap) {
				return new IdentityHashMap<>(original.size());
			}
			if (original instanceof WeakHashMap) {
				return new WeakHashMap<>(original.size());
			}
			return new HashMap<>(original.size());
		}
	}

	/**
	 * Configuration class for deep cloning operations.
	 * 
	 * <p>
	 * Provides a fluent builder pattern for configuring which fields to ignore
	 * during the cloning process.
	 * </p>
	 *
	 * @example
	 * 
	 *          <pre>{@code
	 * DeepCloneUtils.CloneConfig config = DeepCloneUtils.CloneConfig.builder()
	 *     .ignoreField("id")
	 *     .ignoreField("version")
	 *     .ignoreField("auditInfo")
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
			 * @throws IllegalArgumentException if fieldName is null or empty
			 */
			public Builder ignoreField(String fieldName) {
				if (fieldName == null || fieldName.trim().isEmpty()) {
					throw new IllegalArgumentException("Field name cannot be null or empty");
				}
				ignoreFields.add(fieldName);
				return this;
			}

			/**
			 * Adds multiple field names to the ignore list.
			 *
			 * @param fieldNames the names of fields to ignore during cloning
			 * @return this builder for method chaining
			 * @throws IllegalArgumentException if fieldNames is null or contains null/empty
			 *                                  values
			 */
			public Builder ignoreFields(String... fieldNames) {
				if (fieldNames == null) {
					throw new IllegalArgumentException("Field names cannot be null");
				}
				for (String name : fieldNames) {
					if (name == null || name.trim().isEmpty()) {
						throw new IllegalArgumentException("Field name cannot be null or empty");
					}
				}
				ignoreFields.addAll(Arrays.asList(fieldNames));
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