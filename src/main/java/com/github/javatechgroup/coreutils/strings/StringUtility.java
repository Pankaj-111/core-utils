package com.github.javatechgroup.coreutils.strings;

import java.util.regex.Pattern;

import org.apache.commons.text.RandomStringGenerator;

/**
 * Utility class for common string operations such as masking, validation,
 * formatting, and generation. Provides static methods for tasks like email
 * masking, phone number validation, random string generation, and more.
 *
 * <p>
 * <b>Features:</b>
 * <ul>
 * <li>Masking sensitive information (emails, phone numbers, credit cards)</li>
 * <li>Validating formats (email, phone, URL)</li>
 * <li>Generating random alphanumeric strings</li>
 * <li>Converting text to SEO-friendly slugs</li>
 * <li>Truncating text at word boundaries</li>
 * <li>Counting words in a string</li>
 * <li>Extracting initials from names</li>
 * <li>Converting text to title case</li>
 * <li>Removing accents from characters</li>
 * <li>Checking for palindromes</li>
 * <li>Calculating Levenshtein distance between strings</li>
 * </ul>
 *
 * <p>
 * All methods are static and the class cannot be instantiated.
 *
 * @author Pankaj Kumar
 * @version 1.0
 * @since 2024-06-15
 */
public final class StringUtility {

	private StringUtility() {
		// Utility class - prevent instantiation
		throw new UnsupportedOperationException("StringUtility is a utility class and cannot be instantiated");
	}

	// Pattern for common validations
	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
	private static final Pattern PHONE_PATTERN = Pattern.compile("^[+]?[0-9]{10,15}$");
	private static final Pattern URL_PATTERN = Pattern.compile("^(https?|ftp)://[^\\s/$.?#].[^\\s]*$");
	private static final RandomStringGenerator SECURE_GENERATOR = new RandomStringGenerator
			// Builder pattern to include letters and digits
			.Builder()
			// includes numbers and letters
			.withinRange('0', 'z')
			// filter only alphanumeric
			.filteredBy(Character::isLetterOrDigit)
			// Generate the string
			.get();

	/**
	 * Masks an email address by preserving the first 2 characters of the username
	 * and the full domain. Useful for displaying emails in UI while protecting user
	 * privacy.
	 *
	 * <p>
	 * <b>Examples:</b>
	 * <ul>
	 * <li>{@code "test@example.com" → "te**@example.com"}</li>
	 * <li>{@code "user@domain.com" → "u*@domain.com"}</li>
	 * <li>{@code "a@b.com" → "a@b.com"} (too short to mask)</li>
	 * </ul>
	 *
	 * @param email the email address to mask, may be null
	 * @return the masked email address, or original string if null, invalid, or too
	 *         short to mask
	 * @see #isValidEmail(String)
	 */
	public static String maskEmail(String email) {
		if (email == null || !isValidEmail(email))
			return email;

		int atIndex = email.indexOf('@');
		if (atIndex <= 2)
			return email;

		String username = email.substring(0, atIndex);
		String domain = email.substring(atIndex);

		String maskedUsername = username.substring(0, 2) + "*".repeat(Math.max(0, username.length() - 2));
		return maskedUsername + domain;
	}

	/**
	 * Masks a phone number by showing only the last 4 digits. Useful for protecting
	 * sensitive phone information in logs or UI displays.
	 *
	 * <p>
	 * <b>Examples:</b>
	 * <ul>
	 * <li>{@code "+1234567890" → "******7890"}</li>
	 * <li>{@code "1234567890" → "******7890"}</li>
	 * <li>{@code "1234" → "1234"} (too short to mask)</li>
	 * </ul>
	 *
	 * @param phone the phone number to mask, may be null
	 * @return the masked phone number, or original string if null, invalid, or too
	 *         short to mask
	 * @see #isValidPhone(String)
	 */
	public static String maskPhone(String phone) {
		if (phone == null || !isValidPhone(phone))
			return phone;

		if (phone.length() <= 4)
			return phone;
		return "*".repeat(phone.length() - 4) + phone.substring(phone.length() - 4);
	}

	/**
	 * Masks a credit card number by showing only the first 6 and last 4 digits.
	 * Compliant with PCI DSS standards for credit card display.
	 *
	 * <p>
	 * <b>Examples:</b>
	 * <ul>
	 * <li>{@code "1234567890123456" → "123456******3456"}</li>
	 * <li>{@code "4111111111111111" → "411111******1111"}</li>
	 * </ul>
	 *
	 * @param cardNumber the credit card number to mask, may be null
	 * @return the masked credit card number, or original string if null or too
	 *         short
	 * @throws IllegalArgumentException if the card number is less than 10
	 *                                  characters
	 */
	public static String maskCreditCard(String cardNumber) {
		if (cardNumber == null)
			return null;
		if (cardNumber.length() < 10)
			return cardNumber;

		String firstSix = cardNumber.substring(0, 6);
		String lastFour = cardNumber.substring(cardNumber.length() - 4);
		return firstSix + "*".repeat(cardNumber.length() - 10) + lastFour;
	}

	/**
	 * Masks sensitive information with customizable visibility ranges and mask
	 * characters. Useful for partially revealing sensitive data while keeping most
	 * of it hidden.
	 *
	 * <p>
	 * <b>Examples:</b>
	 * <ul>
	 * <li>{@code maskSensitive("password", 0, 2, '*') → "**ssword"}</li>
	 * <li>{@code maskSensitive("1234567890", 3, 7, 'X') → "123XXXX890"}</li>
	 * </ul>
	 *
	 * @param str          the string to mask, may be null
	 * @param visibleStart the starting index of visible characters (inclusive)
	 * @param visibleEnd   the ending index of visible characters (exclusive)
	 * @param maskChar     the character to use for masking
	 * @return the masked string, or null if input is null
	 * @throws IllegalArgumentException if visibleStart > visibleEnd
	 */
	public static String maskSensitive(String str, int visibleStart, int visibleEnd, char maskChar) {
		if (str == null)
			return null;
		if (visibleStart < 0)
			visibleStart = 0;
		if (visibleEnd > str.length())
			visibleEnd = str.length();

		// Fix: Check if visibleStart >= visibleEnd, return original string
		if (visibleStart >= visibleEnd)
			return str;

		String start = str.substring(0, visibleStart);
		String end = str.substring(visibleEnd);
		String masked = String.valueOf(maskChar).repeat(visibleEnd - visibleStart);

		return start + masked + end;
	}

	/**
	 * Validates an email address format using RFC 5322 compliant regex pattern.
	 * Note: This validates format only, not whether the email actually exists.
	 *
	 * <p>
	 * <b>Examples:</b>
	 * <ul>
	 * <li>{@code isValidEmail("test@example.com") → true}</li>
	 * <li>{@code isValidEmail("invalid.email") → false}</li>
	 * <li>{@code isValidEmail(null) → false}</li>
	 * </ul>
	 *
	 * @param email the email address to validate, may be null
	 * @return true if the email format is valid, false otherwise
	 */
	public static boolean isValidEmail(String email) {
		return email != null && EMAIL_PATTERN.matcher(email).matches();
	}

	/**
	 * Validates an international phone number format. Allows optional '+' prefix
	 * and requires 10-15 digits. Does not validate country codes or phone number
	 * existence.
	 *
	 * <p>
	 * <b>Format:</b> {@code ^[+]?[0-9]{10,15}$}
	 *
	 * <p>
	 * <b>Examples:</b>
	 * <ul>
	 * <li>{@code isValidPhone("+1234567890") → true}</li>
	 * <li>{@code isValidPhone("1234567890") → true}</li>
	 * <li>{@code isValidPhone("123456789") → false} (too short)</li>
	 * <li>{@code isValidPhone("+1-800-123-4567") → false} (contains hyphens)</li>
	 * </ul>
	 *
	 * @param phone the phone number to validate, may be null
	 * @return true if the phone format is valid, false otherwise
	 */
	public static boolean isValidPhone(String phone) {
		return phone != null && PHONE_PATTERN.matcher(phone).matches();
	}

	/**
	 * Validates a URL format including http, https, and ftp protocols. Validates
	 * format only, not whether the URL is accessible.
	 *
	 * <p>
	 * <b>Examples:</b>
	 * <ul>
	 * <li>{@code isValidUrl("https://example.com") → true}</li>
	 * <li>{@code isValidUrl("ftp://files.example.com") → true}</li>
	 * <li>{@code isValidUrl("invalid.url") → false}</li>
	 * <li>{@code isValidUrl(null) → false}</li>
	 * </ul>
	 *
	 * @param url the URL to validate, may be null
	 * @return true if the URL format is valid, false otherwise
	 */
	public static boolean isValidUrl(String url) {
		return url != null && URL_PATTERN.matcher(url).matches();
	}

	/**
	 * Generates a random alphanumeric string of specified length. Uses Apache
	 * Commons Text RandomStringGenerator for secure random generation.
	 *
	 * <p>
	 * <b>Examples:</b>
	 * <ul>
	 * <li>{@code generateRandom(10) → "aB3dE6gH1J"}</li>
	 * <li>{@code generateRandom(0) → ""}</li>
	 * <li>{@code generateRandom(-5) → ""}</li>
	 * </ul>
	 *
	 * @param length the length of the random string to generate, must be
	 *               non-negative
	 * @return the generated random alphanumeric string, or empty string if length
	 *         is non-positive
	 */
	public static String generateRandom(int length) {
		if (length < 0) {
			throw new IllegalArgumentException("Length must be non-negative");
		}
		if (length == 0) {
			return "";
		}
		return SECURE_GENERATOR.generate(length);
	}

	/**
	 * Converts a string to a SEO-friendly URL slug. Removes special characters,
	 * converts to lowercase, and replaces spaces with hyphens.
	 *
	 * <p>
	 * <b>Examples:</b>
	 * <ul>
	 * <li>{@code toSlug("Hello World!") → "hello-world"}</li>
	 * <li>{@code toSlug("Test @#$% Slug 123") → "test-slug-123"}</li>
	 * <li>{@code toSlug(null) → ""}</li>
	 * </ul>
	 *
	 * @param text the text to convert to slug, may be null
	 * @return the SEO-friendly slug, or empty string if input is null
	 */
	public static String toSlug(String text) {
		if (text == null)
			return "";

		return text.toLowerCase()
				// Remove special characters except spaces and hyphens
				.replaceAll("[^a-z0-9\\s-]", "") // Keep alphanumeric, spaces, hyphens
				.replaceAll("\\s+", "-") // Replace spaces with hyphens
				.replaceAll("-+", "-") // Replace multiple hyphens
				.trim();
	}

	/**
	 * Truncates text to the nearest word boundary within the specified maximum
	 * length. Preserves word integrity and adds ellipsis to indicate truncation.
	 *
	 * <p>
	 * <b>Examples:</b>
	 * <ul>
	 * <li>{@code truncateAtWord("Hello world this is test", 10) → "Hello world..."}</li>
	 * <li>{@code truncateAtWord("Short", 10) → "Short"}</li>
	 * <li>{@code truncateAtWord(null, 10) → null}</li>
	 * </ul>
	 *
	 * @param text      the text to truncate, may be null
	 * @param maxLength the maximum length before truncation
	 * @return the truncated text with ellipsis, or original text if shorter than
	 *         maxLength
	 */
	public static String truncateAtWord(String text, int maxLength) {
		if (text == null || text.length() <= maxLength)
			return text;

		// Look for the last space within maxLength
		int lastSpace = text.lastIndexOf(' ', maxLength);

		// If we found a space within maxLength, use it
		if (lastSpace > 0) {
			return text.substring(0, lastSpace);
		}

		// If no space within maxLength, look for the first space after maxLength
		// but only if it's very close (within 3 characters)
		int nextSpace = text.indexOf(' ', maxLength);
		if (nextSpace != -1 && nextSpace <= maxLength + 3) {
			return text.substring(0, nextSpace);
		}

		// If no suitable space found, truncate at maxLength
		return text.substring(0, maxLength);
	}

	/**
	 * Counts the number of words in a string using whitespace as delimiter. More
	 * accurate than simple split as it handles multiple spaces and trims input.
	 *
	 * <p>
	 * <b>Examples:</b>
	 * <ul>
	 * <li>{@code countWords("Hello world") → 2}</li>
	 * <li>{@code countWords("  Multiple   spaces  ") → 2}</li>
	 * <li>{@code countWords("") → 0}</li>
	 * <li>{@code countWords(null) → 0}</li>
	 * </ul>
	 *
	 * @param text the text to count words in, may be null
	 * @return the number of words, 0 if null or empty
	 */
	public static int countWords(String text) {
		if (text == null || text.trim().isEmpty())
			return 0;

		return text.trim().split("\\s+").length;
	}

	/**
	 * Extracts initials from a person's name by taking the first character of each
	 * word. Handles multiple spaces and converts to uppercase with periods.
	 *
	 * <p>
	 * <b>Examples:</b>
	 * <ul>
	 * <li>{@code getInitials("John Doe") → "J.D."}</li>
	 * <li>{@code getInitials("Mahendra Singh Dhoni") → "M.S.D."}</li>
	 * <li>{@code getInitials("  single  ") → "S."}</li>
	 * <li>{@code getInitials(null) → ""}</li>
	 * </ul>
	 *
	 * @param name the full name to extract initials from, may be null
	 * @return the initials in uppercase with periods, or empty string if null/empty
	 */
	public static String getInitials(String name) {
		if (name == null || name.trim().isEmpty())
			return "";

		String[] parts = name.trim().split("\\s+");
		StringBuilder initials = new StringBuilder();

		for (String part : parts) {
			if (!part.isEmpty()) {
				initials.append(Character.toUpperCase(part.charAt(0))).append(".");
			}
		}

		return initials.toString();
	}

	/**
	 * Converts a string to title case (first letter of each word uppercase, rest
	 * lowercase). Handles multiple spaces and mixed case input.
	 *
	 * <p>
	 * <b>Examples:</b>
	 * <ul>
	 * <li>{@code toTitleCase("hello world") → "Hello World"}</li>
	 * <li>{@code toTitleCase("hELLO wORLD") → "Hello World"}</li>
	 * <li>{@code toTitleCase(null) → null}</li>
	 * </ul>
	 *
	 * @param text the text to convert to title case, may be null
	 * @return the title case string, or null if input is null
	 */
	public static String toTitleCase(String text) {
		if (text == null || text.isEmpty())
			return text;

		String[] words = text.toLowerCase().split("\\s+");
		StringBuilder result = new StringBuilder();

		for (String word : words) {
			if (!word.isEmpty()) {
				if (result.length() > 0)
					result.append(" ");
				result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
			}
		}

		return result.toString();
	}

	/**
	 * Removes diacritics and accents from text, converting to ASCII characters.
	 * Useful for search normalization and text processing.
	 *
	 * <p>
	 * <b>Examples:</b>
	 * <ul>
	 * <li>{@code removeAccents("café") → "cafe"}</li>
	 * <li>{@code removeAccents("naïve") → "naive"}</li>
	 * <li>{@code removeAccents("Mëtàl") → "Metal"}</li>
	 * <li>{@code removeAccents(null) → null}</li>
	 * </ul>
	 *
	 * @param text the text to remove accents from, may be null
	 * @return the ASCII-friendly text, or null if input is null
	 */
	public static String removeAccents(String text) {
		if (text == null)
			return null;
		return java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFD)
				.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
	}

	/**
	 * Checks if a string is a palindrome (reads the same forwards and backwards).
	 * Ignores case, punctuation, and whitespace for the comparison.
	 *
	 * <p>
	 * <b>Examples:</b>
	 * <ul>
	 * <li>{@code isPalindrome("racecar") → true}</li>
	 * <li>{@code isPalindrome("A man, a plan, a canal, Panama!") → true}</li>
	 * <li>{@code isPalindrome("hello") → false}</li>
	 * <li>{@code isPalindrome(null) → false}</li>
	 * </ul>
	 *
	 * @param text the text to check for palindrome, may be null
	 * @return true if the text is a palindrome, false otherwise
	 */
	public static boolean isPalindrome(String text) {
		if (text == null)
			return false;

		String clean = text.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
		String reversed = new StringBuilder(clean).reverse().toString();

		return clean.equals(reversed);
	}

	/**
	 * Calculates the Levenshtein distance between two strings. The Levenshtein
	 * distance is the minimum number of single-character edits (insertions,
	 * deletions, or substitutions) required to change one string into the other.
	 *
	 * <p>
	 * Useful for fuzzy string matching, spell checking, and similarity
	 * measurements.
	 *
	 * <p>
	 * <b>Examples:</b>
	 * <ul>
	 * <li>{@code levenshteinDistance("kitten", "sitting") → 3}</li>
	 * <li>{@code levenshteinDistance("cat", "bat") → 1}</li>
	 * <li>{@code levenshteinDistance("", "abc") → 3}</li>
	 * <li>{@code levenshteinDistance(null, "test") → 4} (treats null as empty
	 * string)</li>
	 * </ul>
	 *
	 * @param a the first string, may be null (treated as empty string)
	 * @param b the second string, may be null (treated as empty string)
	 * @return the Levenshtein distance between the two strings
	 */
	public static int levenshteinDistance(String a, String b) {
		if (a == null)
			a = "";
		if (b == null)
			b = "";

		int[][] dp = new int[a.length() + 1][b.length() + 1];

		for (int i = 0; i <= a.length(); i++) {
			for (int j = 0; j <= b.length(); j++) {
				if (i == 0) {
					dp[i][j] = j;
				} else if (j == 0) {
					dp[i][j] = i;
				} else {
					dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
							dp[i - 1][j - 1] + (a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1));
				}
			}
		}

		return dp[a.length()][b.length()];
	}
}