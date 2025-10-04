package com.github.javatechgroup.coreutils.date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;

/**
 * Unit tests for {@link CalendarUtils}
 */
class CalendarUtilsTest {

	private Calendar testCalendar;
	private Date testDate;
	private LocalDateTime testLocalDateTime;

	@BeforeEach
	void setUp() {
		// Fixed date for consistent testing: 2024-01-15T10:30:45
		testCalendar = Calendar.getInstance();
		testCalendar.set(2024, Calendar.JANUARY, 15, 10, 30, 45);
		testCalendar.set(Calendar.MILLISECOND, 123);

		testDate = testCalendar.getTime();
		testLocalDateTime = LocalDateTime.of(2024, 1, 15, 10, 30, 45);
	}

	@Test
	void testConstructor() throws Exception {
		Constructor<CalendarUtils> constructor = CalendarUtils.class.getDeclaredConstructor();
		constructor.setAccessible(true);

		InvocationTargetException exception = assertThrows(InvocationTargetException.class, constructor::newInstance);

		assertTrue(exception.getCause() instanceof UnsupportedOperationException);
		assertEquals("This is a utility class and cannot be instantiated", exception.getCause().getMessage());
	}

	@Test
	void testNow() {
		Calendar result = CalendarUtils.now();
		assertNotNull(result);
		assertTrue(result instanceof Calendar);
	}

	@Test
	void testNow_WithTimezone() {
		Calendar result = CalendarUtils.now(ZoneId.of("UTC"));
		assertNotNull(result);
		assertEquals("UTC", result.getTimeZone().getID());
	}

	@Test
	void testNow_WithNullTimezone() {
		assertNull(CalendarUtils.now(null));
	}

	@Test
	void testToCalendar_FromDate() {
		Calendar result = CalendarUtils.toCalendar(testDate);
		assertNotNull(result);
		assertEquals(testDate, result.getTime());
	}

	@Test
	void testToCalendar_FromDateWithTimezone() {
		Calendar result = CalendarUtils.toCalendar(testDate, ZoneId.of("UTC"));
		assertNotNull(result);
		assertEquals("UTC", result.getTimeZone().getID());
	}

	@Test
	void testToCalendar_FromLocalDateTime() {
		Calendar result = CalendarUtils.toCalendar(testLocalDateTime);
		assertNotNull(result);
		// Compare individual components due to potential timezone differences
		assertEquals(2024, result.get(Calendar.YEAR));
		assertEquals(Calendar.JANUARY, result.get(Calendar.MONTH));
		assertEquals(15, result.get(Calendar.DAY_OF_MONTH));
	}

	@Test
	void testToCalendar_FromLocalDateTimeWithTimezone() {
		Calendar result = CalendarUtils.toCalendar(testLocalDateTime, ZoneId.of("America/New_York"));
		assertNotNull(result);
		assertEquals("America/New_York", result.getTimeZone().getID());
	}

	@Test
	void testToDate() {
		Date result = CalendarUtils.toDate(testCalendar);
		assertNotNull(result);
		assertEquals(testDate, result);
	}

	@Test
	void testToDate_FromNullCalendar() {
		assertNull(CalendarUtils.toDate(null));
	}

	@Test
	void testToLocalDateTime() {
		LocalDateTime result = CalendarUtils.toLocalDateTime(testCalendar);
		assertNotNull(result);
		assertEquals(2024, result.getYear());
		assertEquals(1, result.getMonthValue());
		assertEquals(15, result.getDayOfMonth());
	}

	@Test
	void testToLocalDate() {
		// Create a calendar with time cleared for accurate LocalDate comparison
		Calendar dateOnlyCalendar = (Calendar) testCalendar.clone();
		dateOnlyCalendar.set(Calendar.HOUR_OF_DAY, 0);
		dateOnlyCalendar.set(Calendar.MINUTE, 0);
		dateOnlyCalendar.set(Calendar.SECOND, 0);
		dateOnlyCalendar.set(Calendar.MILLISECOND, 0);

		var result = CalendarUtils.toLocalDate(dateOnlyCalendar);
		assertNotNull(result);
		assertEquals(2024, result.getYear());
		assertEquals(1, result.getMonthValue());
		assertEquals(15, result.getDayOfMonth());
	}

	@Test
	void testAdd() {
		Calendar result = CalendarUtils.add(testCalendar, Calendar.DAY_OF_MONTH, 5);
		assertNotNull(result);

		Calendar expected = (Calendar) testCalendar.clone();
		expected.add(Calendar.DAY_OF_MONTH, 5);

		assertEquals(expected.getTime(), result.getTime());
	}

	@Test
	void testAdd_NegativeAmount() {
		Calendar result = CalendarUtils.add(testCalendar, Calendar.DAY_OF_MONTH, -3);
		assertNotNull(result);

		Calendar expected = (Calendar) testCalendar.clone();
		expected.add(Calendar.DAY_OF_MONTH, -3);

		assertEquals(expected.getTime(), result.getTime());
	}

	@Test
	void testAdd_WithNullCalendar() {
		assertNull(CalendarUtils.add(null, Calendar.DAY_OF_MONTH, 5));
	}

	@Test
	void testSubtract() {
		Calendar result = CalendarUtils.subtract(testCalendar, Calendar.DAY_OF_MONTH, 3);
		assertNotNull(result);

		Calendar expected = (Calendar) testCalendar.clone();
		expected.add(Calendar.DAY_OF_MONTH, -3);

		assertEquals(expected.getTime(), result.getTime());
	}

	@Test
	void testSet() {
		Calendar result = CalendarUtils.set(testCalendar, Calendar.HOUR_OF_DAY, 15);
		assertNotNull(result);
		assertEquals(15, result.get(Calendar.HOUR_OF_DAY));
		// Other fields should remain unchanged
		assertEquals(2024, result.get(Calendar.YEAR));
		assertEquals(Calendar.JANUARY, result.get(Calendar.MONTH));
		assertEquals(15, result.get(Calendar.DAY_OF_MONTH));
	}

	@Test
	void testGet() {
		int hour = CalendarUtils.get(testCalendar, Calendar.HOUR_OF_DAY);
		assertEquals(10, hour);
	}

	@Test
	void testGet_WithNullCalendar() {
		assertEquals(-1, CalendarUtils.get(null, Calendar.HOUR_OF_DAY));
	}

	@Test
	void testIsSameDay() {
		Calendar sameDay = (Calendar) testCalendar.clone();
		sameDay.set(Calendar.HOUR_OF_DAY, 15); // Different time, same day

		Calendar differentDay = (Calendar) testCalendar.clone();
		differentDay.add(Calendar.DAY_OF_MONTH, 1);

		assertTrue(CalendarUtils.isSameDay(testCalendar, sameDay));
		assertFalse(CalendarUtils.isSameDay(testCalendar, differentDay));
	}

	@Test
	void testIsSameDay_WithNullInput() {
		assertFalse(CalendarUtils.isSameDay(null, testCalendar));
		assertFalse(CalendarUtils.isSameDay(testCalendar, null));
		assertFalse(CalendarUtils.isSameDay(null, null));
	}

	@Test
	void testGetFirstDayOfMonth() {
		Calendar result = CalendarUtils.getFirstDayOfMonth(testCalendar);
		assertNotNull(result);
		assertEquals(1, result.get(Calendar.DAY_OF_MONTH));
		assertEquals(0, result.get(Calendar.HOUR_OF_DAY)); // Time should be cleared
	}

	@Test
	void testGetLastDayOfMonth() {
		Calendar result = CalendarUtils.getLastDayOfMonth(testCalendar);
		assertNotNull(result);
		assertEquals(31, result.get(Calendar.DAY_OF_MONTH)); // January has 31 days
		assertEquals(0, result.get(Calendar.HOUR_OF_DAY)); // Time should be cleared
	}

	@Test
	void testIsWeekend() {
		// Set to a known weekend (Saturday)
		Calendar weekendCalendar = (Calendar) testCalendar.clone();
		weekendCalendar.set(2024, Calendar.JANUARY, 13); // January 13, 2024 is Saturday

		// Set to a known weekday (Monday)
		Calendar weekdayCalendar = (Calendar) testCalendar.clone();
		weekdayCalendar.set(2024, Calendar.JANUARY, 15); // January 15, 2024 is Monday

		assertTrue(CalendarUtils.isWeekend(weekendCalendar));
		assertFalse(CalendarUtils.isWeekend(weekdayCalendar));
	}

	@Test
	void testIsWeekday() {
		// Set to a known weekend (Saturday)
		Calendar weekendCalendar = (Calendar) testCalendar.clone();
		weekendCalendar.set(2024, Calendar.JANUARY, 13); // January 13, 2024 is Saturday

		// Set to a known weekday (Monday)
		Calendar weekdayCalendar = (Calendar) testCalendar.clone();
		weekdayCalendar.set(2024, Calendar.JANUARY, 15); // January 15, 2024 is Monday

		assertFalse(CalendarUtils.isWeekday(weekendCalendar));
		assertTrue(CalendarUtils.isWeekday(weekdayCalendar));
	}

	@Test
	void testStartOfDay() {
		Calendar result = CalendarUtils.startOfDay(testCalendar);
		assertNotNull(result);
		assertEquals(0, result.get(Calendar.HOUR_OF_DAY));
		assertEquals(0, result.get(Calendar.MINUTE));
		assertEquals(0, result.get(Calendar.SECOND));
		assertEquals(0, result.get(Calendar.MILLISECOND));
	}

	@Test
	void testEndOfDay() {
		Calendar result = CalendarUtils.endOfDay(testCalendar);
		assertNotNull(result);
		assertEquals(23, result.get(Calendar.HOUR_OF_DAY));
		assertEquals(59, result.get(Calendar.MINUTE));
		assertEquals(59, result.get(Calendar.SECOND));
		assertEquals(999, result.get(Calendar.MILLISECOND));
	}

	@Test
	void testClearTime() {
		Calendar result = CalendarUtils.clearTime(testCalendar);
		assertNotNull(result);
		assertEquals(0, result.get(Calendar.HOUR_OF_DAY));
		assertEquals(0, result.get(Calendar.MINUTE));
		assertEquals(0, result.get(Calendar.SECOND));
		assertEquals(0, result.get(Calendar.MILLISECOND));
	}

	@Test
	void testDifference() {
		Calendar futureDate = (Calendar) testCalendar.clone();
		futureDate.add(Calendar.DAY_OF_MONTH, 5);

		long daysDifference = CalendarUtils.difference(testCalendar, futureDate, java.time.temporal.ChronoUnit.DAYS);
		assertEquals(5, daysDifference);

		long hoursDifference = CalendarUtils.difference(testCalendar, futureDate, java.time.temporal.ChronoUnit.HOURS);
		assertEquals(5 * 24, hoursDifference);
	}

	@Test
	void testIsBefore() {
		Calendar futureDate = (Calendar) testCalendar.clone();
		futureDate.add(Calendar.DAY_OF_MONTH, 1);

		assertTrue(CalendarUtils.isBefore(testCalendar, futureDate));
		assertFalse(CalendarUtils.isBefore(futureDate, testCalendar));
	}

	@Test
	void testIsAfter() {
		Calendar pastDate = (Calendar) testCalendar.clone();
		pastDate.add(Calendar.DAY_OF_MONTH, -1);

		assertTrue(CalendarUtils.isAfter(testCalendar, pastDate));
		assertFalse(CalendarUtils.isAfter(pastDate, testCalendar));
	}

	@Test
	void testIsBetween() {
		Calendar startDate = (Calendar) testCalendar.clone();
		startDate.add(Calendar.DAY_OF_MONTH, -1);

		Calendar endDate = (Calendar) testCalendar.clone();
		endDate.add(Calendar.DAY_OF_MONTH, 1);

		Calendar beforeDate = (Calendar) testCalendar.clone();
		beforeDate.add(Calendar.DAY_OF_MONTH, -2);

		Calendar afterDate = (Calendar) testCalendar.clone();
		afterDate.add(Calendar.DAY_OF_MONTH, 2);

		assertTrue(CalendarUtils.isBetween(testCalendar, startDate, endDate));
		assertFalse(CalendarUtils.isBetween(beforeDate, startDate, endDate));
		assertFalse(CalendarUtils.isBetween(afterDate, startDate, endDate));
	}

	@ParameterizedTest
	@NullSource
	void testNullSafety_CalendarInput(Calendar input) {
		assertNull(CalendarUtils.toDate(input));
		assertNull(CalendarUtils.toLocalDateTime(input));
		assertNull(CalendarUtils.toLocalDate(input));
		assertNull(CalendarUtils.add(input, Calendar.DAY_OF_MONTH, 1));
		assertNull(CalendarUtils.subtract(input, Calendar.DAY_OF_MONTH, 1));
		assertNull(CalendarUtils.set(input, Calendar.HOUR_OF_DAY, 12));
		assertNull(CalendarUtils.getFirstDayOfMonth(input));
		assertNull(CalendarUtils.getLastDayOfMonth(input));
		assertNull(CalendarUtils.startOfDay(input));
		assertNull(CalendarUtils.endOfDay(input));
		assertNull(CalendarUtils.clearTime(input));

		assertEquals(-1, CalendarUtils.get(input, Calendar.HOUR_OF_DAY));
		assertFalse(CalendarUtils.isWeekend(input));
		assertTrue(CalendarUtils.isWeekday(input));
	}

	@Test
	void testEdgeCases() {
		// Test with different timezones
		Calendar utcCalendar = CalendarUtils.toCalendar(testDate, ZoneId.of("UTC"));
		Calendar estCalendar = CalendarUtils.toCalendar(testDate, ZoneId.of("America/New_York"));

		assertNotNull(utcCalendar);
		assertNotNull(estCalendar);
		assertNotEquals(utcCalendar.getTimeZone(), estCalendar.getTimeZone());

		// Test with February (leap year)
		Calendar febCalendar = (Calendar) testCalendar.clone();
		febCalendar.set(2024, Calendar.FEBRUARY, 15); // 2024 is a leap year
		Calendar lastDayFeb = CalendarUtils.getLastDayOfMonth(febCalendar);
		assertEquals(29, lastDayFeb.get(Calendar.DAY_OF_MONTH));
	}

	@Test
	void testVariousCalendarFields() {
		// Test different field types
		Calendar resultDays = CalendarUtils.add(testCalendar, Calendar.DAY_OF_MONTH, 1);
		Calendar resultHours = CalendarUtils.add(testCalendar, Calendar.HOUR_OF_DAY, 1);
		Calendar resultMonths = CalendarUtils.add(testCalendar, Calendar.MONTH, 1);
		Calendar resultYears = CalendarUtils.add(testCalendar, Calendar.YEAR, 1);

		assertNotNull(resultDays);
		assertNotNull(resultHours);
		assertNotNull(resultMonths);
		assertNotNull(resultYears);

		// Verify the operations worked correctly
		assertNotEquals(testCalendar.getTime(), resultDays.getTime());
		assertNotEquals(testCalendar.getTime(), resultHours.getTime());
		assertNotEquals(testCalendar.getTime(), resultMonths.getTime());
		assertNotEquals(testCalendar.getTime(), resultYears.getTime());
	}
}