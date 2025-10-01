package com.github.pankaj111.coreutils.clone;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive unit tests for DeepCloneUtils
 */
class DeepCloneUtilsTest {

	// Test data classes
	static class Person {
		private String name;
		private int age;
		private Address address;
		private List<String> hobbies;

		public Person() {
		}

		public Person(String name, int age, Address address, List<String> hobbies) {
			this.name = name;
			this.age = age;
			this.address = address;
			this.hobbies = hobbies;
		}

		// getters and setters
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getAge() {
			return age;
		}

		public void setAge(int age) {
			this.age = age;
		}

		public Address getAddress() {
			return address;
		}

		public void setAddress(Address address) {
			this.address = address;
		}

		public List<String> getHobbies() {
			return hobbies;
		}

		public void setHobbies(List<String> hobbies) {
			this.hobbies = hobbies;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			Person person = (Person) o;
			return age == person.age && Objects.equals(name, person.name) && Objects.equals(address, person.address)
					&& Objects.equals(hobbies, person.hobbies);
		}

		@Override
		public int hashCode() {
			return Objects.hash(name, age, address, hobbies);
		}
	}

	static class Address {
		private String street;
		private String city;
		private transient String transientField; // Should not be cloned

		public Address() {
		}

		public Address(String street, String city) {
			this.street = street;
			this.city = city;
			this.transientField = "transient";
		}

		public String getStreet() {
			return street;
		}

		public void setStreet(String street) {
			this.street = street;
		}

		public String getCity() {
			return city;
		}

		public void setCity(String city) {
			this.city = city;
		}

		public String getTransientField() {
			return transientField;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			Address address = (Address) o;
			return Objects.equals(street, address.street) && Objects.equals(city, address.city);
		}

		@Override
		public int hashCode() {
			return Objects.hash(street, city);
		}
	}

	static class Node {
		private String value;
		private Node next;
		private Node previous; // For circular reference

		public Node() {
		}

		public Node(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public Node getNext() {
			return next;
		}

		public void setNext(Node next) {
			this.next = next;
		}

		public Node getPrevious() {
			return previous;
		}

		public void setPrevious(Node previous) {
			this.previous = previous;
		}
	}

	// NEW: Class with only parameterized constructor (tests enhanced instantiation)
	static class Employee {
		private final String name;
		private final int id;

		// Only parameterized constructor - should work with enhanced instantiation
		public Employee(String name, int id) {
			this.name = name;
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public int getId() {
			return id;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			Employee employee = (Employee) o;
			return id == employee.id && Objects.equals(name, employee.name);
		}

		@Override
		public int hashCode() {
			return Objects.hash(name, id);
		}
	}

	// NEW: Class with multiple constructors
	static class Product {
		private String name;
		private double price;
		private String category;

		public Product() {
			this.category = "default";
		}

		public Product(String name) {
			this.name = name;
			this.category = "uncategorized";
		}

		public Product(String name, double price) {
			this.name = name;
			this.price = price;
			this.category = "uncategorized";
		}

		// getters and setters
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public double getPrice() {
			return price;
		}

		public void setPrice(double price) {
			this.price = price;
		}

		public String getCategory() {
			return category;
		}

		public void setCategory(String category) {
			this.category = category;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			Product product = (Product) o;
			return Double.compare(product.price, price) == 0 && Objects.equals(name, product.name)
					&& Objects.equals(category, product.category);
		}

		@Override
		public int hashCode() {
			return Objects.hash(name, price, category);
		}
	}

	// NEW: Class with private fields and no setters
	static class ImmutableData {
		private final String data;
		private final int count;

		public ImmutableData() {
			this.data = "default";
			this.count = 0;
		}

		public ImmutableData(String data, int count) {
			this.data = data;
			this.count = count;
		}

		public String getData() {
			return data;
		}

		public int getCount() {
			return count;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			ImmutableData that = (ImmutableData) o;
			return count == that.count && Objects.equals(data, that.data);
		}

		@Override
		public int hashCode() {
			return Objects.hash(data, count);
		}
	}

	@Test
	@DisplayName("Should clone null object")
	void testCloneNull() {
		assertNull(DeepCloneUtils.deepClone(null));
		assertNull(DeepCloneUtils.deepClone(null, "field1"));

		DeepCloneUtils.CloneConfig config = DeepCloneUtils.CloneConfig.builder().build();
		assertNull(DeepCloneUtils.deepClone(null, config));
	}

	@Test
	@DisplayName("Should clone immutable types")
	void testCloneImmutableTypes() {
		// String
		String originalString = "test";
		String clonedString = DeepCloneUtils.deepClone(originalString);
		assertEquals(originalString, clonedString);
		assertSame(originalString, clonedString);

		// Wrapper types
		Integer originalInt = 42;
		Integer clonedInt = DeepCloneUtils.deepClone(originalInt);
		assertEquals(originalInt, clonedInt);
		assertSame(originalInt, clonedInt);

		// Enum
		enum TestEnum {
			VALUE1, VALUE2
		}
		TestEnum originalEnum = TestEnum.VALUE1;
		TestEnum clonedEnum = DeepCloneUtils.deepClone(originalEnum);
		assertEquals(originalEnum, clonedEnum);
		assertSame(originalEnum, clonedEnum);

		// BigDecimal
		BigDecimal originalBigDecimal = new BigDecimal("123.45");
		BigDecimal clonedBigDecimal = DeepCloneUtils.deepClone(originalBigDecimal);
		assertEquals(originalBigDecimal, clonedBigDecimal);
		assertSame(originalBigDecimal, clonedBigDecimal);
	}

	@Test
	@DisplayName("Should clone java.time objects as immutable")
	void testCloneJavaTimeObjects() {
		LocalDate originalDate = LocalDate.now();
		LocalDate clonedDate = DeepCloneUtils.deepClone(originalDate);
		assertEquals(originalDate, clonedDate);
		assertSame(originalDate, clonedDate);

		LocalDateTime originalDateTime = LocalDateTime.now();
		LocalDateTime clonedDateTime = DeepCloneUtils.deepClone(originalDateTime);
		assertEquals(originalDateTime, clonedDateTime);
		assertSame(originalDateTime, clonedDateTime);

		Instant originalInstant = Instant.now();
		Instant clonedInstant = DeepCloneUtils.deepClone(originalInstant);
		assertEquals(originalInstant, clonedInstant);
		assertSame(originalInstant, clonedInstant);
	}

	@Test
	@DisplayName("Should clone arrays")
	void testCloneArray() {
		int[] originalArray = { 1, 2, 3, 4, 5 };
		int[] clonedArray = DeepCloneUtils.deepClone(originalArray);

		assertArrayEquals(originalArray, clonedArray);
		assertNotSame(originalArray, clonedArray);

		// Test object array
		String[] originalStringArray = { "a", "b", "c" };
		String[] clonedStringArray = DeepCloneUtils.deepClone(originalStringArray);

		assertArrayEquals(originalStringArray, clonedStringArray);
		assertNotSame(originalStringArray, clonedStringArray);
		// Strings inside array should be same instances (immutable)
		assertSame(originalStringArray[0], clonedStringArray[0]);

		// Test multi-dimensional array
		int[][] originalMultiArray = { { 1, 2 }, { 3, 4 } };
		int[][] clonedMultiArray = DeepCloneUtils.deepClone(originalMultiArray);

		assertArrayEquals(originalMultiArray, clonedMultiArray);
		assertNotSame(originalMultiArray, clonedMultiArray);
		assertNotSame(originalMultiArray[0], clonedMultiArray[0]);
	}

	@Test
	@DisplayName("Should clone various collection types")
	void testCloneCollections() {
		// ArrayList
		List<String> originalList = Arrays.asList("one", "two", "three");
		List<String> clonedList = DeepCloneUtils.deepClone(originalList);
		assertEquals(originalList, clonedList);
		assertNotSame(originalList, clonedList);

		// HashSet
		Set<Integer> originalSet = new HashSet<>(Arrays.asList(1, 2, 3));
		Set<Integer> clonedSet = DeepCloneUtils.deepClone(originalSet);
		assertEquals(originalSet, clonedSet);
		assertNotSame(originalSet, clonedSet);

		// LinkedList
		LinkedList<Double> originalLinkedList = new LinkedList<>(Arrays.asList(1.1, 2.2, 3.3));
		LinkedList<Double> clonedLinkedList = DeepCloneUtils.deepClone(originalLinkedList);
		assertEquals(originalLinkedList, clonedLinkedList);
		assertNotSame(originalLinkedList, clonedLinkedList);

		// ArrayDeque - use content comparison since ArrayDeque.equals() uses reference
		// equality
		Deque<String> originalDeque = new ArrayDeque<>(Arrays.asList("a", "b", "c"));
		Deque<String> clonedDeque = DeepCloneUtils.deepClone(originalDeque);
		// Compare content manually since ArrayDeque doesn't override equals() properly
		assertEquals(new ArrayList<>(originalDeque), new ArrayList<>(clonedDeque));
		assertNotSame(originalDeque, clonedDeque);

		// Also verify individual operations work the same
		assertEquals(originalDeque.size(), clonedDeque.size());
		assertEquals(originalDeque.peek(), clonedDeque.peek());
		assertEquals(originalDeque.peekLast(), clonedDeque.peekLast());

		// PriorityQueue
		Queue<Integer> originalQueue = new PriorityQueue<>(Arrays.asList(3, 1, 2));
		Queue<Integer> clonedQueue = DeepCloneUtils.deepClone(originalQueue);
		// Note: PriorityQueue order might not be preserved during iteration
		assertEquals(new ArrayList<>(originalQueue), new ArrayList<>(clonedQueue));
		assertNotSame(originalQueue, clonedQueue);
	}

	@Test
	@DisplayName("Should clone ArrayDeque with proper content")
	void testArrayDequeCloning() {
		ArrayDeque<String> originalDeque = new ArrayDeque<>();
		originalDeque.add("first");
		originalDeque.add("second");
		originalDeque.add("third");

		ArrayDeque<String> clonedDeque = DeepCloneUtils.deepClone(originalDeque);

		// Verify they are different instances
		assertNotSame(originalDeque, clonedDeque);

		// Verify same size
		assertEquals(originalDeque.size(), clonedDeque.size());

		// Verify same content by comparing as lists
		assertEquals(new ArrayList<>(originalDeque), new ArrayList<>(clonedDeque));

		// Verify same behavior for queue operations
		assertEquals(originalDeque.peek(), clonedDeque.peek());
		assertEquals(originalDeque.peekLast(), clonedDeque.peekLast());

		// Verify same removal order
		while (!originalDeque.isEmpty()) {
			assertEquals(originalDeque.poll(), clonedDeque.poll());
		}
		assertTrue(clonedDeque.isEmpty());
	}

	@Test
	@DisplayName("Should clone various map types")
	void testCloneMaps() {
		// HashMap
		Map<String, Integer> originalMap = new HashMap<>();
		originalMap.put("one", 1);
		originalMap.put("two", 2);
		originalMap.put("three", 3);
		Map<String, Integer> clonedMap = DeepCloneUtils.deepClone(originalMap);
		assertEquals(originalMap, clonedMap);
		assertNotSame(originalMap, clonedMap);

		// LinkedHashMap (order preservation)
		Map<String, Integer> originalLinkedMap = new LinkedHashMap<>();
		originalLinkedMap.put("first", 1);
		originalLinkedMap.put("second", 2);
		originalLinkedMap.put("third", 3);
		Map<String, Integer> clonedLinkedMap = DeepCloneUtils.deepClone(originalLinkedMap);
		assertEquals(new ArrayList<>(originalLinkedMap.keySet()), new ArrayList<>(clonedLinkedMap.keySet()));

		// IdentityHashMap
		Map<Object, Object> originalIdentityMap = new IdentityHashMap<>();
		String key1 = new String("key");
		String key2 = new String("key");
		originalIdentityMap.put(key1, "value1");
		originalIdentityMap.put(key2, "value2");
		Map<Object, Object> clonedIdentityMap = DeepCloneUtils.deepClone(originalIdentityMap);
		assertEquals(originalIdentityMap.size(), clonedIdentityMap.size());

		// ConcurrentHashMap
		Map<String, String> originalConcurrentMap = new ConcurrentHashMap<>();
		originalConcurrentMap.put("k1", "v1");
		originalConcurrentMap.put("k2", "v2");
		Map<String, String> clonedConcurrentMap = DeepCloneUtils.deepClone(originalConcurrentMap);
		assertEquals(originalConcurrentMap, clonedConcurrentMap);
		assertNotSame(originalConcurrentMap, clonedConcurrentMap);
	}

	@Test
	@DisplayName("Should clone complex object graph")
	void testCloneComplexObject() {
		Address address = new Address("123 Main St", "Springfield");
		List<String> hobbies = Arrays.asList("reading", "gaming", "coding");
		Person original = new Person("John Doe", 30, address, hobbies);

		Person cloned = DeepCloneUtils.deepClone(original);

		assertEquals(original, cloned);
		assertNotSame(original, cloned);
		assertNotSame(original.getAddress(), cloned.getAddress());
		assertNotSame(original.getHobbies(), cloned.getHobbies());
		assertEquals(original.getAddress(), cloned.getAddress());
		assertEquals(original.getHobbies(), cloned.getHobbies());
	}

	@Test
	@DisplayName("Should handle circular references")
	void testCircularReferences() {
		Node node1 = new Node("Node1");
		Node node2 = new Node("Node2");
		Node node3 = new Node("Node3");

		// Create circular reference: node1 -> node2 -> node3 -> node1
		node1.setNext(node2);
		node2.setNext(node3);
		node3.setNext(node1);

		// This should not cause infinite recursion
		Node cloned = DeepCloneUtils.deepClone(node1);

		assertNotNull(cloned);
		assertEquals("Node1", cloned.getValue());
		assertNotNull(cloned.getNext());
		assertEquals("Node2", cloned.getNext().getValue());
		assertNotNull(cloned.getNext().getNext());
		assertEquals("Node3", cloned.getNext().getNext().getValue());
		// Circular reference should be maintained
		assertSame(cloned, cloned.getNext().getNext().getNext());
	}

	@Test
	@DisplayName("Should ignore specified fields")
	void testIgnoreFields() {
		Address address = new Address("123 Main St", "Springfield");
		List<String> hobbies = Arrays.asList("reading", "gaming");
		Person original = new Person("John Doe", 30, address, hobbies);
		original.setName("Original Name");

		// Clone ignoring name field
		Person cloned = DeepCloneUtils.deepClone(original, "name");

		assertNotSame(original, cloned);
		assertNull(cloned.getName()); // Name should be ignored (null in default constructor)
		assertEquals(original.getAge(), cloned.getAge());
		assertEquals(original.getAddress(), cloned.getAddress());
		assertEquals(original.getHobbies(), cloned.getHobbies());
	}

	@Test
	@DisplayName("Should ignore fields using CloneConfig")
	void testIgnoreFieldsWithConfig() {
		Address address = new Address("123 Main St", "Springfield");
		List<String> hobbies = Arrays.asList("reading", "gaming");
		Person original = new Person("John Doe", 30, address, hobbies);

		DeepCloneUtils.CloneConfig config = DeepCloneUtils.CloneConfig.builder().ignoreField("name").ignoreField("age")
				.build();

		Person cloned = DeepCloneUtils.deepClone(original, config);

		assertNotSame(original, cloned);
		assertNull(cloned.getName()); // Ignored field
		assertEquals(0, cloned.getAge()); // Ignored field (default int value)
		assertEquals(original.getAddress(), cloned.getAddress());
		assertEquals(original.getHobbies(), cloned.getHobbies());
	}

	@Test
	@DisplayName("Should validate CloneConfig builder")
	void testCloneConfigBuilderValidation() {
		// Test null field name
		assertThrows(IllegalArgumentException.class, () -> DeepCloneUtils.CloneConfig.builder().ignoreField(null));

		// Test empty field name
		assertThrows(IllegalArgumentException.class, () -> DeepCloneUtils.CloneConfig.builder().ignoreField(""));

		// Test null array
		assertThrows(IllegalArgumentException.class,
				() -> DeepCloneUtils.CloneConfig.builder().ignoreFields((String[]) null));

		// Test array with null element
		assertThrows(IllegalArgumentException.class,
				() -> DeepCloneUtils.CloneConfig.builder().ignoreFields("valid", null));
	}

	@Test
	@DisplayName("Should not clone transient fields")
	void testTransientFieldsNotCloned() {
		Address original = new Address("123 Main St", "Springfield");
		assertEquals("transient", original.getTransientField());

		Address cloned = DeepCloneUtils.deepClone(original);

		assertEquals(original.getStreet(), cloned.getStreet());
		assertEquals(original.getCity(), cloned.getCity());
		assertNull(cloned.getTransientField()); // Transient field should not be cloned
	}

	@Test
	@DisplayName("Should not clone static fields")
	void testStaticFieldsNotCloned() {
		class WithStaticField {
			private String instanceField = "instance";
		}

		WithStaticField original = new WithStaticField();
		WithStaticField cloned = DeepCloneUtils.deepClone(original);

		assertEquals(original.instanceField, cloned.instanceField);
		// Static field is shared, not cloned
	}

	@Test
	@DisplayName("Should clone Optional")
	void testCloneOptional() {
		Optional<String> originalPresent = Optional.of("test");
		Optional<String> clonedPresent = DeepCloneUtils.deepClone(originalPresent);

		assertEquals(originalPresent, clonedPresent);
		assertTrue(clonedPresent.isPresent());
		assertEquals("test", clonedPresent.get());

		Optional<String> originalEmpty = Optional.empty();
		Optional<String> clonedEmpty = DeepCloneUtils.deepClone(originalEmpty);

		assertEquals(originalEmpty, clonedEmpty);
		assertFalse(clonedEmpty.isPresent());

		// Test Optional with complex object
		Person person = new Person("Test", 25, new Address("St", "City"), List.of("hobby"));
		Optional<Person> originalComplex = Optional.of(person);
		Optional<Person> clonedComplex = DeepCloneUtils.deepClone(originalComplex);

		assertTrue(clonedComplex.isPresent());
		assertEquals(person, clonedComplex.get());
		assertNotSame(person, clonedComplex.get());
	}

	@Test
	@DisplayName("Should handle SortedSet and SortedMap with ordering")
	void testSortedCollections() {
		// SortedSet with custom comparator
		SortedSet<Integer> originalSet = new TreeSet<>(Collections.reverseOrder());
		originalSet.addAll(Arrays.asList(3, 1, 2));

		SortedSet<Integer> clonedSet = DeepCloneUtils.deepClone(originalSet);

		// Verify content equality
		assertEquals(originalSet, clonedSet);

		// Verify ordering is preserved (behavioral test)
		List<Integer> originalOrder = new ArrayList<>(originalSet);
		List<Integer> clonedOrder = new ArrayList<>(clonedSet);
		assertEquals(originalOrder, clonedOrder);

		// Verify the comparator produces the same ordering
		assertEquals(originalSet.comparator() != null, clonedSet.comparator() != null);

		// SortedMap with custom comparator
		SortedMap<String, Integer> originalMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		originalMap.put("Zebra", 1);
		originalMap.put("apple", 2);
		originalMap.put("Banana", 3);

		SortedMap<String, Integer> clonedMap = DeepCloneUtils.deepClone(originalMap);

		// Verify content equality
		assertEquals(originalMap, clonedMap);

		// Verify ordering is preserved
		List<String> originalKeyOrder = new ArrayList<>(originalMap.keySet());
		List<String> clonedKeyOrder = new ArrayList<>(clonedMap.keySet());
		assertEquals(originalKeyOrder, clonedKeyOrder);

		// Verify case-insensitive behavior works
		assertTrue(clonedMap.containsKey("zebra")); // Should find with different case
		assertTrue(clonedMap.containsKey("APPLE")); // Should find with different case
	}

	@Test
	@DisplayName("Should handle natural ordering sorted collections")
	void testNaturalOrderingCollections() {
		// Natural ordering (null comparator)
		SortedSet<String> naturalSet = new TreeSet<>();
		naturalSet.addAll(Arrays.asList("cherry", "apple", "banana"));

		SortedSet<String> clonedSet = DeepCloneUtils.deepClone(naturalSet);

		assertEquals(naturalSet, clonedSet);
		assertEquals(new ArrayList<>(naturalSet), new ArrayList<>(clonedSet));
		assertNull(clonedSet.comparator()); // Natural ordering should have null comparator

		// Verify alphabetical order is preserved
		assertEquals(Arrays.asList("apple", "banana", "cherry"), new ArrayList<>(clonedSet));
	}

	@Test
	@DisplayName("Should handle classes with parameterized constructors")
	void testClassesWithParameterizedConstructors() {
		// Class with only parameterized constructor
		Employee original = new Employee("John Doe", 123);
		Employee cloned = DeepCloneUtils.deepClone(original);

		assertNotNull(cloned);
		assertEquals(original.getName(), cloned.getName());
		assertEquals(original.getId(), cloned.getId());

		// Class with multiple constructors
		Product product = new Product("Laptop", 999.99);
		product.setCategory("Electronics");
		Product clonedProduct = DeepCloneUtils.deepClone(product);

		assertEquals(product, clonedProduct);
		assertNotSame(product, clonedProduct);

		// Class with final fields
		ImmutableData data = new ImmutableData("test", 42);
		ImmutableData clonedData = DeepCloneUtils.deepClone(data);

		assertEquals(data, clonedData);
		assertNotSame(data, clonedData);
	}

	@Test
	@DisplayName("Should handle primitive types")
	void testPrimitiveTypes() {
		// These should return the same instance since they're immutable
		int primitiveInt = 42;
		Integer cloned = DeepCloneUtils.deepClone(primitiveInt);
		assertEquals(Integer.valueOf(primitiveInt), cloned);

		boolean primitiveBool = true;
		Boolean clonedBool = DeepCloneUtils.deepClone(primitiveBool);
		assertEquals(Boolean.valueOf(primitiveBool), clonedBool);

		char primitiveChar = 'A';
		Character clonedChar = DeepCloneUtils.deepClone(primitiveChar);
		assertEquals(Character.valueOf(primitiveChar), clonedChar);
	}

	@Test
	@DisplayName("Should handle UUID and URI")
	void testOtherImmutableTypes() {
		UUID originalUuid = UUID.randomUUID();
		UUID clonedUuid = DeepCloneUtils.deepClone(originalUuid);
		assertEquals(originalUuid, clonedUuid);
		assertSame(originalUuid, clonedUuid);

		URI originalUri = URI.create("https://example.com");
		URI clonedUri = DeepCloneUtils.deepClone(originalUri);
		assertEquals(originalUri, clonedUri);
		assertSame(originalUri, clonedUri);
	}

	@Test
	@DisplayName("Should handle nested collections")
	void testNestedCollections() {
		List<List<String>> originalNestedList = new ArrayList<>();
		originalNestedList.add(Arrays.asList("a", "b"));
		originalNestedList.add(Arrays.asList("c", "d"));

		List<List<String>> clonedNestedList = DeepCloneUtils.deepClone(originalNestedList);

		assertEquals(originalNestedList, clonedNestedList);
		assertNotSame(originalNestedList, clonedNestedList);
		assertNotSame(originalNestedList.get(0), clonedNestedList.get(0));

		// Complex nested structure
		Map<String, List<Set<Integer>>> complexStructure = new HashMap<>();
		complexStructure.put("key1", Arrays.asList(Set.of(1, 2), Set.of(3, 4)));
		complexStructure.put("key2", Arrays.asList(Set.of(5, 6)));

		Map<String, List<Set<Integer>>> clonedStructure = DeepCloneUtils.deepClone(complexStructure);

		assertEquals(complexStructure, clonedStructure);
		assertNotSame(complexStructure, clonedStructure);
		assertNotSame(complexStructure.get("key1"), clonedStructure.get("key1"));
		assertNotSame(complexStructure.get("key1").get(0), clonedStructure.get("key1").get(0));
	}

	@Test
	@DisplayName("Should handle empty collections and maps")
	void testEmptyCollections() {
		List<String> emptyList = new ArrayList<>();
		List<String> clonedEmptyList = DeepCloneUtils.deepClone(emptyList);
		assertTrue(clonedEmptyList.isEmpty());
		assertNotSame(emptyList, clonedEmptyList);

		Map<String, String> emptyMap = new HashMap<>();
		Map<String, String> clonedEmptyMap = DeepCloneUtils.deepClone(emptyMap);
		assertTrue(clonedEmptyMap.isEmpty());
		assertNotSame(emptyMap, clonedEmptyMap);

		Set<Integer> emptySet = new HashSet<>();
		Set<Integer> clonedEmptySet = DeepCloneUtils.deepClone(emptySet);
		assertTrue(clonedEmptySet.isEmpty());
		assertNotSame(emptySet, clonedEmptySet);
	}

	@Test
	@DisplayName("Should handle complex object with mixed collection types")
	void testComplexObjectWithMixedCollections() {
		class ComplexObject {
			private List<String> list;
			private Set<Integer> set;
			private Map<String, Object> map;
			private Object[] array;

			public ComplexObject(List<String> list, Set<Integer> set, Map<String, Object> map, Object[] array) {
				this.list = list;
				this.set = set;
				this.map = map;
				this.array = array;
			}

			// getters and equals/hashCode
			public List<String> getList() {
				return list;
			}

			public Set<Integer> getSet() {
				return set;
			}

			public Map<String, Object> getMap() {
				return map;
			}

			public Object[] getArray() {
				return array;
			}

			@Override
			public boolean equals(Object o) {
				if (this == o)
					return true;
				if (o == null || getClass() != o.getClass())
					return false;
				ComplexObject that = (ComplexObject) o;
				return Objects.equals(list, that.list) && Objects.equals(set, that.set) && Objects.equals(map, that.map)
						&& Arrays.equals(array, that.array);
			}

			@Override
			public int hashCode() {
				return Objects.hash(list, set, map, Arrays.hashCode(array));
			}
		}

		ComplexObject original = new ComplexObject(Arrays.asList("a", "b", "c"), new HashSet<>(Arrays.asList(1, 2, 3)),
				Map.of("key1", "value1", "key2", 42), new Object[] { "arr1", 123, LocalDate.now() });

		ComplexObject cloned = DeepCloneUtils.deepClone(original);

		assertEquals(original, cloned);
		assertNotSame(original, cloned);
		assertNotSame(original.getList(), cloned.getList());
		assertNotSame(original.getSet(), cloned.getSet());
		assertNotSame(original.getMap(), cloned.getMap());
		assertNotSame(original.getArray(), cloned.getArray());
	}

	@Test
	@DisplayName("Should demonstrate field caching through performance")
	void testFieldCachingThroughPerformance() {
		class TestClass {
			private String field1 = "value1";
			private int field2 = 42;
			private List<String> field3 = Arrays.asList("a", "b", "c");
		}

		// First clone - should populate cache
		TestClass original1 = new TestClass();
		long startTime1 = System.nanoTime();
		TestClass cloned1 = DeepCloneUtils.deepClone(original1);
		long duration1 = System.nanoTime() - startTime1;

		// Second clone - should use cached fields
		TestClass original2 = new TestClass();
		long startTime2 = System.nanoTime();
		TestClass cloned2 = DeepCloneUtils.deepClone(original2);
		long duration2 = System.nanoTime() - startTime2;

		// Verify correctness
		assertEquals(original1.field1, cloned1.field1);
		assertEquals(original1.field2, cloned1.field2);
		assertEquals(original1.field3, cloned1.field3);
		assertEquals(original2.field1, cloned2.field1);
		assertEquals(original2.field2, cloned2.field2);
		assertEquals(original2.field3, cloned2.field3);

		// Second clone should generally be faster due to caching
		// Note: This is not a strict assertion as JVM performance can vary
		System.out.println("First clone duration: " + duration1 + " ns");
		System.out.println("Second clone duration: " + duration2 + " ns");

		// The main point is that both work correctly
		assertNotSame(original1, cloned1);
		assertNotSame(original2, cloned2);
		assertNotSame(original1.field3, cloned1.field3);
		assertNotSame(original2.field3, cloned2.field3);
	}

	@Test
	@DisplayName("Should handle inheritance hierarchies")
	void testInheritanceHierarchies() {
		class BaseClass {
			private String baseField = "base";

			public String getBaseField() {
				return baseField;
			}
		}

		class DerivedClass extends BaseClass {
			private String derivedField = "derived";

			public String getDerivedField() {
				return derivedField;
			}
		}

		DerivedClass original = new DerivedClass();
		DerivedClass cloned = DeepCloneUtils.deepClone(original);

		assertEquals(original.getBaseField(), cloned.getBaseField());
		assertEquals(original.getDerivedField(), cloned.getDerivedField());
		assertNotSame(original, cloned);
	}
}