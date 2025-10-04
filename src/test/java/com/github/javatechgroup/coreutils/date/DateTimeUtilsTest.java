package com.github.javatechgroup.coreutils.date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;

import com.github.javatechgroup.coreutils.date.DateTimeUtils;

/**
 * Unit tests for {@link DateTimeUtils}
 */
class DateTimeUtilsTest {

	private static final ZoneId UTC = ZoneId.of("UTC");
	private static final ZoneId EST = ZoneId.of("America/New_York");

	private Date testDate;
	private LocalDate testLocalDate;
	private LocalDateTime testLocalDateTime;

	@BeforeEach
	void setUp() {
		// Fixed date for consistent testing: 2024-01-15T10:30:45
		testLocalDate = LocalDate.of(2024, 1, 15);
		testLocalDateTime = LocalDateTime.of(2024, 1, 15, 10, 30, 45);
		testDate = Date.from(testLocalDateTime.atZone(ZoneId.systemDefault()).toInstant());
	}

	// Test constructor
	@Test
	void testConstructor() throws Exception {
		java.lang.reflect.Constructor<DateTimeUtils> constructor = DateTimeUtils.class.getDeclaredConstructor();
		constructor.setAccessible(true);

		// The constructor throws UnsupportedOperationException, but it gets wrapped
		// in an InvocationTargetException when called via reflection
		Exception exception = assertThrows(Exception.class, constructor::newInstance);

		// Check that it's an InvocationTargetException wrapping our expected exception
		assertTrue(exception instanceof InvocationTargetException);

		// Get the actual cause (our UnsupportedOperationException)
		Throwable cause = exception.getCause();
		assertTrue(cause instanceof UnsupportedOperationException);
		assertEquals("This is a utility class and cannot be instantiated", cause.getMessage());
	}

	// Test formatDateInISO_DATE
	@Test
	void testFormatDateInISO_DATE_ValidDate() {
		String result = DateTimeUtils.formatDateInISO_DATE(testLocalDate);
		assertEquals("2024-01-15", result);
	}

	@Test
	void testFormatDateInISO_DATE_NullDate() {
		assertNull(DateTimeUtils.formatDateInISO_DATE(null));
	}

	// Test formatDate with pattern
	@Test
	void testFormatDate_ValidDateAndPattern() {
		String result = DateTimeUtils.formatDate(testLocalDate, "dd/MM/yyyy");
		assertEquals("15/01/2024", result);
	}

	@Test
	void testFormatDate_NullInput() {
		assertNull(DateTimeUtils.formatDate(testLocalDate, null));
	}

	@Test
	void testFormatDate_InvalidPattern() {
		assertThrows(IllegalArgumentException.class, () -> DateTimeUtils.formatDate(testLocalDate, "invalid pattern"));
	}

	// Test formatDate with Date object
	@Test
	void testFormatDate_WithDateObject() {
		String result = DateTimeUtils.formatDate(testDate, "yyyy-MM-dd HH:mm:ss");
		assertNotNull(result);
		assertTrue(result.startsWith("2024-01-15"));
	}

	// Test parseToDate
	@Test
	void testParseToDate_ValidString() {
		Date result = DateTimeUtils.parseToDate("2024-01-15 10:30:45", "yyyy-MM-dd HH:mm:ss");
		assertNotNull(result);
		// Allow for small differences due to timezone handling
		assertTrue(Math.abs(result.getTime() - testDate.getTime()) < 1000);
	}

	@Test
	void testParseToDate_NullInput() {
		assertNull(DateTimeUtils.parseToDate(null, "yyyy-MM-dd"));
		assertNull(DateTimeUtils.parseToDate("2024-01-15", null));
		assertNull(DateTimeUtils.parseToDate(null, null));
	}

	@Test
	void testParseToDate_InvalidDateString() {
		assertThrows(DateTimeParseException.class, () -> DateTimeUtils.parseToDate("invalid-date", "yyyy-MM-dd"));
	}

	// Test toDate conversions
	@Test
	void testToDate_FromLocalDate() {
		Date result = DateTimeUtils.toDate(testLocalDate);
		assertNotNull(result);

		// Convert back to verify
		LocalDate convertedBack = DateTimeUtils.toLocalDate(result);
		assertEquals(testLocalDate, convertedBack);
	}

	@Test
	void testToDate_FromLocalDateTime() {
		Date result = DateTimeUtils.toDate(testLocalDateTime);
		assertNotNull(result);

		// Should preserve time information
		LocalDateTime convertedBack = DateTimeUtils.toLocalDateTime(result);
		assertEquals(testLocalDateTime.getHour(), convertedBack.getHour());
		assertEquals(testLocalDateTime.getMinute(), convertedBack.getMinute());
	}

	// Test toLocalDate and toLocalDateTime
	@Test
	void testToLocalDate_FromDate() {
		LocalDate result = DateTimeUtils.toLocalDate(testDate);
		assertEquals(testLocalDate, result);
	}

	@Test
	void testToLocalDateTime_FromDate() {
		LocalDateTime result = DateTimeUtils.toLocalDateTime(testDate);
		// Compare individual components due to potential millisecond differences
		assertEquals(testLocalDateTime.getYear(), result.getYear());
		assertEquals(testLocalDateTime.getMonth(), result.getMonth());
		assertEquals(testLocalDateTime.getDayOfMonth(), result.getDayOfMonth());
	}

	// Test formatDateTime
	@Test
	void testFormatDateTime_ValidDateTime() {
		String result = DateTimeUtils.formatDateTime(testLocalDateTime, "HH:mm:ss");
		assertEquals("10:30:45", result);
	}

	// Test formatDateTimeInISO_DATE_TIME
	@Test
	void testFormatDateTimeInISO_DATE_TIME() {
		String result = DateTimeUtils.formatDateTimeInISO_DATE_TIME(testLocalDateTime);
		assertTrue(result.startsWith("2024-01-15T10:30:45"));
	}

	// Test parseISODateStr and parseISODateTimeStr
	@Test
	void testParseISODateStr_ValidString() {
		LocalDate result = DateTimeUtils.parseISODateStr("2024-01-15");
		assertEquals(testLocalDate, result);
	}

	@Test
	void testParseISODateTimeStr_ValidString() {
		LocalDateTime result = DateTimeUtils.parseISODateTimeStr("2024-01-15T10:30:45");
		assertEquals(testLocalDateTime, result);
	}

	@ParameterizedTest
	@NullSource
	void testParseISODateStr_NullInput(String input) {
		assertNull(DateTimeUtils.parseISODateStr(input));
	}

	// Test removeTime methods
	@Test
	void testRemoveTime_FromDate() {
		Date result = DateTimeUtils.removeTime(testDate);
		assertNotNull(result);

		// When converted back to LocalDateTime, time should be 00:00:00
		LocalDateTime converted = DateTimeUtils.toLocalDateTime(result);
		assertEquals(0, converted.getHour());
		assertEquals(0, converted.getMinute());
		assertEquals(0, converted.getSecond());
	}

	@Test
	void testRemoveTime_FromLocalDateTime() {
		LocalDate result = DateTimeUtils.removeTime(testLocalDateTime);
		assertEquals(testLocalDate, result);
	}

	// Test addDays and subtractDays methods
	@Test
	void testAddDays_ToLocalDate() {
		LocalDate result = DateTimeUtils.addDays(testLocalDate, 5);
		assertEquals(LocalDate.of(2024, 1, 20), result);
	}

	@Test
	void testSubtractDays_FromLocalDate() {
		LocalDate result = DateTimeUtils.subtractDays(testLocalDate, 5);
		assertEquals(LocalDate.of(2024, 1, 10), result);
	}

	@Test
	void testAddDays_ToLocalDateTime() {
		LocalDateTime result = DateTimeUtils.addDays(testLocalDateTime, 1);
		assertEquals(LocalDateTime.of(2024, 1, 16, 10, 30, 45), result);
	}

	@Test
	void testSubtractDays_FromLocalDateTime() {
		LocalDateTime result = DateTimeUtils.subtractDays(testLocalDateTime, 1);
		assertEquals(LocalDateTime.of(2024, 1, 14, 10, 30, 45), result);
	}

	@Test
	void testAddDays_ToDate() {
		Date result = DateTimeUtils.addDays(testDate, 2);
		assertNotNull(result);

		LocalDate converted = DateTimeUtils.toLocalDate(result);
		assertEquals(LocalDate.of(2024, 1, 17), converted);
	}

	@Test
	void testSubtractDays_FromDate() {
		Date result = DateTimeUtils.subtractDays(testDate, 2);
		assertNotNull(result);

		LocalDate converted = DateTimeUtils.toLocalDate(result);
		assertEquals(LocalDate.of(2024, 1, 13), converted);
	}

	// Test timezone-aware methods
	@Test
	void testAddDays_WithTimezone() {
		Date resultUTC = DateTimeUtils.addDays(testDate, 1, UTC);
		Date resultEST = DateTimeUtils.addDays(testDate, 1, EST);

		assertNotNull(resultUTC);
		assertNotNull(resultEST);

		// Dates represent the same instant in time, so they should be equal
		// when comparing the underlying timestamp
		assertEquals(resultUTC, resultEST);
		assertEquals(resultUTC.getTime(), resultEST.getTime());

		// However, when converted back to LocalDateTime in their respective timezones,
		// they should show different local times
		LocalDateTime localUTC = DateTimeUtils.toLocalDateTime(resultUTC, UTC);
		LocalDateTime localEST = DateTimeUtils.toLocalDateTime(resultEST, EST);

		// EST is 5 hours behind UTC, so the local time should be different
		assertNotEquals(localUTC.getHour(), localEST.getHour());
	}

	@Test
	void testRemoveTime_WithTimezone() {
		Date resultUTC = DateTimeUtils.removeTime(testDate, UTC);
		Date resultEST = DateTimeUtils.removeTime(testDate, EST);

		assertNotNull(resultUTC);
		assertNotNull(resultEST);
		// Results should be different due to timezone differences
		assertNotEquals(resultUTC, resultEST);
	}

	// Test edge cases with negative days
	@Test
	void testAddDays_Negative() {
		LocalDate result = DateTimeUtils.addDays(testLocalDate, -3);
		assertEquals(LocalDate.of(2024, 1, 12), result);
	}

	@Test
	void testSubtractDays_Negative() {
		LocalDate result = DateTimeUtils.subtractDays(testLocalDate, -3); // Equivalent to addDays(3)
		assertEquals(LocalDate.of(2024, 1, 18), result);
	}

	// Test null safety for all methods
	@ParameterizedTest
	@NullSource
	void testNullSafety_DateInput(Date input) {
		assertNull(DateTimeUtils.formatDate(input, "yyyy-MM-dd"));
		assertNull(DateTimeUtils.toLocalDate(input));
		assertNull(DateTimeUtils.toLocalDateTime(input));
		assertNull(DateTimeUtils.removeTime(input));
		assertNull(DateTimeUtils.addDays(input, 1));
		assertNull(DateTimeUtils.subtractDays(input, 1));
	}

	@ParameterizedTest
	@NullSource
	void testNullSafety_LocalDateInput(LocalDate input) {
		assertNull(DateTimeUtils.formatDateInISO_DATE(input));
		assertNull(DateTimeUtils.formatDate(input, "yyyy-MM-dd"));
		assertNull(DateTimeUtils.toDate(input));
		assertNull(DateTimeUtils.addDays(input, 1));
		assertNull(DateTimeUtils.subtractDays(input, 1));
	}

	@ParameterizedTest
	@NullSource
	void testNullSafety_LocalDateTimeInput(LocalDateTime input) {
		assertNull(DateTimeUtils.formatDateTimeInISO_DATE_TIME(input));
		assertNull(DateTimeUtils.formatDateTime(input, "yyyy-MM-dd"));
		assertNull(DateTimeUtils.toDate(input));
		assertNull(DateTimeUtils.removeTime(input));
		assertNull(DateTimeUtils.addDays(input, 1));
		assertNull(DateTimeUtils.subtractDays(input, 1));
	}

	// Test pattern caching by calling same pattern multiple times
	@Test
	void testFormatterCaching() {
		// Call same pattern multiple times - should use cached formatter
		String result1 = DateTimeUtils.formatDate(testLocalDate, "yyyy-MM-dd");
		String result2 = DateTimeUtils.formatDate(testLocalDate, "yyyy-MM-dd");
		String result3 = DateTimeUtils.formatDate(testLocalDate, "yyyy-MM-dd");

		assertEquals("2024-01-15", result1);
		assertEquals(result1, result2);
		assertEquals(result2, result3);
	}

	// Test default timezone getter/setter
	@Test
	void testDefaultTimezone() {
		ZoneId original = DateTimeUtils.getDefaultZoneId();

		try {
			DateTimeUtils.setDefaultZoneId(UTC);
			assertEquals(UTC, DateTimeUtils.getDefaultZoneId());

			DateTimeUtils.setDefaultZoneId(EST);
			assertEquals(EST, DateTimeUtils.getDefaultZoneId());
		} finally {
			// Restore original timezone
			DateTimeUtils.setDefaultZoneId(original);
		}
	}

	@Test
	void testSetDefaultZoneId_Null() {
		assertThrows(IllegalArgumentException.class, () -> DateTimeUtils.setDefaultZoneId(null));
	}
}