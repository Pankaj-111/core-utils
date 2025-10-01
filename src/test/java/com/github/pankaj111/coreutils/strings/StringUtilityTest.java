package com.github.pankaj111.coreutils.strings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

public class StringUtilityTest {

	// maskEmail tests
	@Test
	void testMaskEmail_ValidEmail() {
		assertEquals("te**@example.com", StringUtility.maskEmail("test@example.com"));
		assertEquals("us**@domain.com", StringUtility.maskEmail("user@domain.com"));
	}

	@Test
	void testMaskEmail_ShortUsername() {
		assertEquals("a@b.com", StringUtility.maskEmail("a@b.com"));
		assertEquals("ab@c.com", StringUtility.maskEmail("ab@c.com"));
	}

	@Test
	void testMaskEmail_InvalidEmail() {
		assertEquals("invalid.email", StringUtility.maskEmail("invalid.email"));
		assertEquals("test@", StringUtility.maskEmail("test@"));
	}

	@Test
	void testMaskEmail_NullInput() {
		assertNull(StringUtility.maskEmail(null));
	}

	// maskPhone tests
	@Test
	void testMaskPhone_ValidPhone() {
		assertEquals("******7890", StringUtility.maskPhone("1234567890"));
		assertEquals("********8901", StringUtility.maskPhone("+12345678901"));
	}

	@Test
	void testMaskPhone_ShortPhone() {
		assertEquals("1234", StringUtility.maskPhone("1234"));
		assertEquals("12", StringUtility.maskPhone("12"));
	}

	@Test
	void testMaskPhone_InvalidPhone() {
		assertEquals("123-456-7890", StringUtility.maskPhone("123-456-7890"));
		assertEquals("abc123", StringUtility.maskPhone("abc123"));
	}

	@Test
	void testMaskPhone_NullInput() {
		assertNull(StringUtility.maskPhone(null));
	}

	// maskCreditCard tests
	@Test
	void testMaskCreditCard_ValidCard() {
		assertEquals("123456******3456", StringUtility.maskCreditCard("1234567890123456"));
		assertEquals("411111******1111", StringUtility.maskCreditCard("4111111111111111"));
	}

	@Test
	void testMaskCreditCard_ShortCard() {
		assertEquals("123456789", StringUtility.maskCreditCard("123456789"));
		assertEquals("123", StringUtility.maskCreditCard("123"));
	}

	@Test
	void testMaskCreditCard_NullInput() {
		assertNull(StringUtility.maskCreditCard(null));
	}

	// maskSensitive tests
	@Test
	void testMaskSensitive_ValidInput() {
		assertEquals("**ssword", StringUtility.maskSensitive("password", 0, 2, '*'));
		assertEquals("123XXXX890", StringUtility.maskSensitive("1234567890", 3, 7, 'X'));
	}

	@Test
	void testMaskSensitive_EdgeCases() {
		// "password" has 8 characters, so masking 0-7 leaves the last character visible
		assertEquals("*******d", StringUtility.maskSensitive("password", 0, 7, '*'));

		// Mask the entire string (0-8)
		assertEquals("********", StringUtility.maskSensitive("password", 0, 8, '*'));

		// No masking when visible range is empty
		assertEquals("password", StringUtility.maskSensitive("password", 0, 0, '*'));
		assertEquals("password", StringUtility.maskSensitive("password", 5, 5, '*'));

		// Edge of string
		assertEquals("****word", StringUtility.maskSensitive("password", 0, 4, '*'));
		assertEquals("pass****", StringUtility.maskSensitive("password", 4, 8, '*'));
	}

	@Test
	void testMaskSensitive_VariousScenarios() {
		// Normal case
		assertEquals("pa****rd", StringUtility.maskSensitive("password", 2, 6, '*'));

		// Mask from start
		assertEquals("****word", StringUtility.maskSensitive("password", 0, 4, '*'));

		// Mask to end
		assertEquals("pass****", StringUtility.maskSensitive("password", 4, 8, '*'));

		// Single character mask
		assertEquals("p*ssword", StringUtility.maskSensitive("password", 1, 2, '*'));

		// Out of bounds handling
		assertEquals("pass****", StringUtility.maskSensitive("password", 4, 20, '*'));
		assertEquals("****word", StringUtility.maskSensitive("password", -5, 4, '*'));
	}

	@Test
	void testMaskSensitive_NullInput() {
		assertNull(StringUtility.maskSensitive(null, 0, 2, '*'));
	}

	// isValidEmail tests
	@ParameterizedTest
	@ValueSource(strings = { "test@example.com", "user.name@domain.co.uk", "first.last@subdomain.example.org" })
	void testIsValidEmail_ValidEmails(String email) {
		assertTrue(StringUtility.isValidEmail(email));
	}

	@ParameterizedTest
	@ValueSource(strings = { "invalid.email", "missing@domain", "@domain.com", "test@.com", "test@domain.", "" })
	@NullAndEmptySource
	void testIsValidEmail_InvalidEmails(String email) {
		assertFalse(StringUtility.isValidEmail(email));
	}

	// isValidPhone tests
	@ParameterizedTest
	@ValueSource(strings = { "1234567890", "+12345678901", "+441234567890" })
	void testIsValidPhone_ValidPhones(String phone) {
		assertTrue(StringUtility.isValidPhone(phone));
	}

	@ParameterizedTest
	@ValueSource(strings = { "123456789", "1234567890123456", "123-456-7890", "+1-800-123-4567", "abc1234567" })
	@NullAndEmptySource
	void testIsValidPhone_InvalidPhones(String phone) {
		assertFalse(StringUtility.isValidPhone(phone));
	}

	// isValidUrl tests
	@ParameterizedTest
	@ValueSource(strings = { "https://example.com", "http://www.example.com/path", "ftp://files.example.com",
			"https://example.com?query=param" })
	void testIsValidUrl_ValidUrls(String url) {
		assertTrue(StringUtility.isValidUrl(url));
	}

	@ParameterizedTest
	@ValueSource(strings = { "invalid.url", "www.example.com", "example.com", "https://", "" })
	@NullAndEmptySource
	void testIsValidUrl_InvalidUrls(String url) {
		assertFalse(StringUtility.isValidUrl(url));
	}

	// generateRandom tests
	@Test
	void testGenerateRandom_ValidLength() {
		assertEquals(10, StringUtility.generateRandom(10).length());
		assertEquals(5, StringUtility.generateRandom(5).length());
		assertEquals("", StringUtility.generateRandom(0));
	}

	@Test
	void testGenerateRandom_Alphanumeric() {
		String random = StringUtility.generateRandom(100);
		assertTrue(random.matches("[a-zA-Z0-9]+"));
	}

	@Test
	void testGenerateRandom_NegativeLength() {
		assertThrows(IllegalArgumentException.class, () -> StringUtility.generateRandom(-1));
	}

	// toSlug tests
	@Test
	void testToSlug_ValidInput() {
		assertEquals("hello-world", StringUtility.toSlug("Hello World!"));
		assertEquals("test-slug-123", StringUtility.toSlug("Test @#$% Slug 123"));
		assertEquals("multiple-hyphens", StringUtility.toSlug("multiple---hyphens"));
	}

	@Test
	void testToSlug_NullInput() {
		assertEquals("", StringUtility.toSlug(null));
	}

	@Test
	void testToSlug_EmptyInput() {
		assertEquals("", StringUtility.toSlug(""));
		assertNotEquals("", StringUtility.toSlug("   "));
	}

	@Test
	void testTruncateAtWord_ValidTruncation() {
		// "Hello world" is 11 characters, so maxLength=10 should truncate to "Hello..."
		assertEquals("Hello", StringUtility.truncateAtWord("Hello world this is test", 10));

		// "Short text" is 10 characters, so maxLength=12 should include it
		assertEquals("Short text", StringUtility.truncateAtWord("Short text with more words", 12));

		// Additional test cases
		assertEquals("Hello world", StringUtility.truncateAtWord("Hello world this is test", 11));
		assertEquals("Short", StringUtility.truncateAtWord("Short text with more words", 8));
	}

	@Test
	void testTruncateAtWord_VariousScenarios() {
		// Exact word boundary
		assertEquals("Hello world", StringUtility.truncateAtWord("Hello world test", 11));

		// No space found
		assertEquals("abcdefghij", StringUtility.truncateAtWord("abcdefghijklmnop", 10));

		// Space right at maxLength
		assertEquals("Test string", StringUtility.truncateAtWord("Test string here", 12));
	}

	@Test
	void testTruncateAtWord_NoTruncationNeeded() {
		assertEquals("Short", StringUtility.truncateAtWord("Short", 10));
		assertEquals("Hello world", StringUtility.truncateAtWord("Hello world", 11));
	}

	@Test
	void testTruncateAtWord_NoSpaceFound() {
		assertEquals("abcdefghij", StringUtility.truncateAtWord("abcdefghijklmnop", 10));
	}

	@Test
	void testTruncateAtWord_NullInput() {
		assertNull(StringUtility.truncateAtWord(null, 10));
	}

	// countWords tests
	@ParameterizedTest
	@CsvSource({ "'Hello world', 2", "'  Multiple   spaces  ', 2", "'Single', 1", "'', 0", "'   ', 0" })
	void testCountWords(String input, int expected) {
		assertEquals(expected, StringUtility.countWords(input));
	}

	@Test
	void testCountWords_NullInput() {
		assertEquals(0, StringUtility.countWords(null));
	}

	// getInitials tests
	@Test
	void testGetInitials_ValidNames() {
		assertEquals("J.D.", StringUtility.getInitials("John Doe"));
		assertEquals("M.S.D.", StringUtility.getInitials("Mahendra Singh Dhoni"));
		assertEquals("S.", StringUtility.getInitials("  single  "));
	}

	@Test
	void testGetInitials_EdgeCases() {
		assertEquals("", StringUtility.getInitials(null));
		assertEquals("", StringUtility.getInitials(""));
		assertEquals("", StringUtility.getInitials("   "));
	}

	// toTitleCase tests
	@Test
	void testToTitleCase_ValidInput() {
		assertEquals("Hello World", StringUtility.toTitleCase("hello world"));
		assertEquals("Hello World", StringUtility.toTitleCase("hELLO wORLD"));
		assertEquals("Multiple Spaces", StringUtility.toTitleCase("multiple   spaces"));
	}

	@Test
	void testToTitleCase_EdgeCases() {
		assertNull(StringUtility.toTitleCase(null));
		assertEquals("", StringUtility.toTitleCase(""));
		assertEquals("A", StringUtility.toTitleCase("a"));
	}

	// removeAccents tests
	@Test
	void testRemoveAccents_ValidInput() {
		assertEquals("cafe", StringUtility.removeAccents("café"));
		assertEquals("naive", StringUtility.removeAccents("naïve"));
		assertEquals("Metal", StringUtility.removeAccents("Mëtàl"));
	}

	@Test
	void testRemoveAccents_NoAccents() {
		assertEquals("hello", StringUtility.removeAccents("hello"));
		assertEquals("TEST", StringUtility.removeAccents("TEST"));
	}

	@Test
	void testRemoveAccents_NullInput() {
		assertNull(StringUtility.removeAccents(null));
	}

	// isPalindrome tests
	@Test
	void testIsPalindrome_ValidPalindromes() {
		assertTrue(StringUtility.isPalindrome("racecar"));
		assertTrue(StringUtility.isPalindrome("A man, a plan, a canal, Panama!"));
		assertTrue(StringUtility.isPalindrome("12321"));
	}

	@Test
	void testIsPalindrome_NotPalindromes() {
		assertFalse(StringUtility.isPalindrome("hello"));
		assertFalse(StringUtility.isPalindrome("test123"));
	}

	@Test
	void testIsPalindrome_EdgeCases() {
		assertFalse(StringUtility.isPalindrome(null));
		assertTrue(StringUtility.isPalindrome(""));
		assertTrue(StringUtility.isPalindrome("a"));
	}

	// levenshteinDistance tests
	@Test
	void testLevenshteinDistance_ValidCases() {
		assertEquals(3, StringUtility.levenshteinDistance("kitten", "sitting"));
		assertEquals(1, StringUtility.levenshteinDistance("cat", "bat"));
		assertEquals(3, StringUtility.levenshteinDistance("", "abc"));
	}

	@Test
	void testLevenshteinDistance_NullInput() {
		assertEquals(4, StringUtility.levenshteinDistance(null, "test"));
		assertEquals(5, StringUtility.levenshteinDistance("hello", null));
		assertEquals(0, StringUtility.levenshteinDistance(null, null));
	}

	@Test
	void testLevenshteinDistance_EqualStrings() {
		assertEquals(0, StringUtility.levenshteinDistance("test", "test"));
		assertEquals(0, StringUtility.levenshteinDistance("", ""));
	}
}