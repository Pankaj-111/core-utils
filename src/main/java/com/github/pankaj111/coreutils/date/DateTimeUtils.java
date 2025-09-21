package com.github.pankaj111.coreutils.date;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Utility class for common date and time operations and conversions. Handles
 * conversions between {@link LocalDate}, {@link LocalDateTime}, and legacy
 * {@link Date}. All methods are null-safe and return null if any input
 * parameter is null.
 * <p>
 * This class is thread-safe and uses caching for better performance with
 * frequently used patterns.
 * </p>
 * 
 * @author Pankaj Singh
 * @version 1.1
 */
public final class DateTimeUtils {

	// Private constructor to prevent instantiation
	private DateTimeUtils() {
		throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
	}

	// Pre-defined formatters
	private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_DATE;
	private static final DateTimeFormatter ISO_DATE_TIME = DateTimeFormatter.ISO_DATE_TIME;

	// Thread-safe cache for formatters
	private static final ConcurrentMap<String, DateTimeFormatter> FORMATTER_CACHE = new ConcurrentHashMap<>();

	// Default timezone (can be customized if needed)
	private static volatile ZoneId defaultZoneId = ZoneId.systemDefault();

	/**
	 * Sets the default timezone to be used by methods that don't explicitly accept
	 * a ZoneId.
	 *
	 * @param zoneId the timezone to set as default (cannot be null)
	 * @throws IllegalArgumentException if zoneId is null
	 */
	public static void setDefaultZoneId(ZoneId zoneId) {
		if (zoneId == null) {
			throw new IllegalArgumentException("ZoneId cannot be null");
		}
		defaultZoneId = zoneId;
	}

	/**
	 * Gets the currently configured default timezone.
	 *
	 * @return the default ZoneId
	 */
	public static ZoneId getDefaultZoneId() {
		return defaultZoneId;
	}

	/**
	 * Gets a DateTimeFormatter from cache or creates a new one if not present.
	 *
	 * @param pattern the pattern for the formatter
	 * @return the DateTimeFormatter for the pattern
	 * @throws IllegalArgumentException if the pattern is invalid
	 */
	private static DateTimeFormatter getFormatter(String pattern) {
		return FORMATTER_CACHE.computeIfAbsent(pattern, DateTimeFormatter::ofPattern);
	}

	/**
	 * Formats a LocalDate to a string using the standard ISO-8601 format
	 * (yyyy-MM-dd).
	 *
	 * @param date the date to format
	 * @return the formatted string, or null if the input is null
	 */
	public static String formatDateInISO_DATE(LocalDate date) {
		return date != null ? date.format(ISO_DATE) : null;
	}

	/**
	 * Formats a LocalDate to a string using the provided pattern.
	 *
	 * @param date    the date to format
	 * @param pattern the pattern to use
	 * @return the formatted string, or null if any input is null
	 * @throws IllegalArgumentException if the pattern is invalid
	 */
	public static String formatDate(LocalDate date, String pattern) {
		if (date == null || pattern == null) {
			return null;
		}
		final DateTimeFormatter formatter = getFormatter(pattern);
		return date.format(formatter);
	}

	/**
	 * Formats a java.util.Date to a string using the provided pattern and the
	 * default timezone.
	 *
	 * @param date    the date to format
	 * @param pattern the pattern to use
	 * @return the formatted string, or null if any input is null
	 * @throws IllegalArgumentException if the pattern is invalid
	 */
	public static String formatDate(Date date, String pattern) {
		return formatDate(date, pattern, defaultZoneId);
	}

	/**
	 * Formats a java.util.Date to a string using the provided pattern and timezone.
	 *
	 * @param date    the date to format
	 * @param pattern the pattern to use
	 * @param zoneId  the timezone to use for conversion
	 * @return the formatted string, or null if any input is null
	 * @throws IllegalArgumentException if the pattern is invalid
	 */
	public static String formatDate(Date date, String pattern, ZoneId zoneId) {
		if (date == null || pattern == null || zoneId == null) {
			return null;
		}
		Instant instant = date.toInstant();
		LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zoneId);
		DateTimeFormatter formatter = getFormatter(pattern);
		return localDateTime.format(formatter);
	}

	/**
	 * Parses a string representation of a date into a java.util.Date object using
	 * the default timezone.
	 *
	 * @param dateStr the string to parse
	 * @param pattern the pattern to use for parsing
	 * @return the parsed Date, or {@code null} if either input is null
	 * @throws DateTimeParseException   if the text cannot be parsed
	 * @throws IllegalArgumentException if the pattern is invalid
	 */
	public static Date parseToDate(String dateStr, String pattern) {
		return parseToDate(dateStr, pattern, defaultZoneId);
	}

	/**
	 * Parses a string representation of a date into a java.util.Date object using
	 * the specified timezone.
	 *
	 * @param dateStr the string to parse
	 * @param pattern the pattern to use for parsing
	 * @param zoneId  the timezone to use for conversion
	 * @return the parsed Date, or {@code null} if any input is null
	 * @throws DateTimeParseException   if the text cannot be parsed
	 * @throws IllegalArgumentException if the pattern is invalid
	 */
	public static Date parseToDate(String dateStr, String pattern, ZoneId zoneId) {
		if (dateStr == null || pattern == null || zoneId == null) {
			return null;
		}
		DateTimeFormatter formatter = getFormatter(pattern);
		LocalDateTime localDateTime = LocalDateTime.parse(dateStr, formatter);
		Instant instant = localDateTime.atZone(zoneId).toInstant();
		return Date.from(instant);
	}

	/**
	 * Converts a LocalDate to java.util.Date using the start of day in the default
	 * timezone.
	 *
	 * @param localDate the LocalDate to convert
	 * @return the converted Date, or null if the input is null
	 */
	public static Date toDate(LocalDate localDate) {
		return toDate(localDate, defaultZoneId);
	}

	/**
	 * Converts a LocalDate to java.util.Date using the start of day in the
	 * specified timezone.
	 *
	 * @param localDate the LocalDate to convert
	 * @param zoneId    the timezone to use for conversion
	 * @return the converted Date, or null if any input is null
	 */
	public static Date toDate(LocalDate localDate, ZoneId zoneId) {
		if (localDate == null || zoneId == null) {
			return null;
		}
		return Date.from(localDate.atStartOfDay(zoneId).toInstant());
	}

	/**
	 * Converts a LocalDateTime to java.util.Date using the default timezone.
	 *
	 * @param localDateTime the LocalDateTime to convert
	 * @return the converted Date, or null if the input is null
	 */
	public static Date toDate(LocalDateTime localDateTime) {
		return toDate(localDateTime, defaultZoneId);
	}

	/**
	 * Converts a LocalDateTime to java.util.Date using the specified timezone.
	 *
	 * @param localDateTime the LocalDateTime to convert
	 * @param zoneId        the timezone to use for conversion
	 * @return the converted Date, or null if any input is null
	 */
	public static Date toDate(LocalDateTime localDateTime, ZoneId zoneId) {
		if (localDateTime == null || zoneId == null) {
			return null;
		}
		return Date.from(localDateTime.atZone(zoneId).toInstant());
	}

	/**
	 * Converts a java.util.Date to LocalDate using the default timezone.
	 *
	 * @param date the Date to convert
	 * @return the converted LocalDate, or null if the input is null
	 */
	public static LocalDate toLocalDate(Date date) {
		return toLocalDate(date, defaultZoneId);
	}

	/**
	 * Converts a java.util.Date to LocalDate using the specified timezone.
	 *
	 * @param date   the Date to convert
	 * @param zoneId the timezone to use for conversion
	 * @return the converted LocalDate, or null if any input is null
	 */
	public static LocalDate toLocalDate(Date date, ZoneId zoneId) {
		if (date == null || zoneId == null) {
			return null;
		}
		return Instant.ofEpochMilli(date.getTime()).atZone(zoneId).toLocalDate();
	}

	/**
	 * Converts a java.util.Date to LocalDateTime using the default timezone.
	 *
	 * @param date the Date to convert
	 * @return the converted LocalDateTime, or null if the input is null
	 */
	public static LocalDateTime toLocalDateTime(Date date) {
		return toLocalDateTime(date, defaultZoneId);
	}

	/**
	 * Converts a java.util.Date to LocalDateTime using the specified timezone.
	 *
	 * @param date   the Date to convert
	 * @param zoneId the timezone to use for conversion
	 * @return the converted LocalDateTime, or null if any input is null
	 */
	public static LocalDateTime toLocalDateTime(Date date, ZoneId zoneId) {
		if (date == null || zoneId == null) {
			return null;
		}
		return Instant.ofEpochMilli(date.getTime()).atZone(zoneId).toLocalDateTime();
	}

	/**
	 * Formats a LocalDateTime to a string using the provided pattern.
	 *
	 * @param dateTime the date-time to format
	 * @param pattern  the pattern to use
	 * @return the formatted string, or null if any input is null
	 * @throws IllegalArgumentException if the pattern is invalid
	 */
	public static String formatDateTime(LocalDateTime dateTime, String pattern) {
		if (dateTime == null || pattern == null) {
			return null;
		}
		DateTimeFormatter formatter = getFormatter(pattern);
		return dateTime.format(formatter);
	}

	/**
	 * Formats a LocalDateTime to a string using the standard ISO-8601 format.
	 *
	 * @param dateTime the date-time to format
	 * @return the formatted string, or null if the input is null
	 */
	public static String formatDateTimeInISO_DATE_TIME(LocalDateTime dateTime) {
		return dateTime != null ? dateTime.format(ISO_DATE_TIME) : null;
	}

	/**
	 * Parses an ISO-8601 date string into a LocalDate.
	 *
	 * @param isoDateStr the ISO date string to parse
	 * @return the parsed LocalDate, or null if the input is null
	 * @throws DateTimeParseException if the text cannot be parsed
	 */
	public static LocalDate parseISODateStr(String isoDateStr) {
		return isoDateStr != null ? LocalDate.parse(isoDateStr, ISO_DATE) : null;
	}

	/**
	 * Parses an ISO-8601 date-time string into a LocalDateTime.
	 *
	 * @param isoDateTimeStr the ISO date-time string to parse
	 * @return the parsed LocalDateTime, or null if the input is null
	 * @throws DateTimeParseException if the text cannot be parsed
	 */
	public static LocalDateTime parseISODateTimeStr(String isoDateTimeStr) {
		return isoDateTimeStr != null ? LocalDateTime.parse(isoDateTimeStr, ISO_DATE_TIME) : null;
	}

	/**
	 * Removes the time component from a java.util.Date, returning a new Date
	 * representing only the date part (at 00:00:00 in the default timezone).
	 *
	 * @param date the Date from which to remove the time component
	 * @return a new Date with time set to 00:00:00, or null if input is null
	 */
	public static Date removeTime(Date date) {
		return removeTime(date, defaultZoneId);
	}

	/**
	 * Removes the time component from a java.util.Date, returning a new Date
	 * representing only the date part (at 00:00:00 in the specified timezone).
	 *
	 * @param date   the Date from which to remove the time component
	 * @param zoneId the timezone to use for conversion
	 * @return a new Date with time set to 00:00:00, or null if any input is null
	 */
	public static Date removeTime(Date date, ZoneId zoneId) {
		if (date == null || zoneId == null) {
			return null;
		}
		LocalDate localDate = toLocalDate(date, zoneId);
		return toDate(localDate, zoneId);
	}

	/**
	 * Removes the time component from a LocalDateTime, returning a LocalDate.
	 *
	 * @param dateTime the LocalDateTime from which to remove the time component
	 * @return the LocalDate part, or null if input is null
	 */
	public static LocalDate removeTime(LocalDateTime dateTime) {
		return dateTime != null ? dateTime.toLocalDate() : null;
	}

	/**
	 * Adds days to a java.util.Date using the default timezone.
	 *
	 * @param date the Date to add days to
	 * @param days the number of days to add (can be negative to subtract)
	 * @return a new Date with the days added, or null if input is null
	 */
	public static Date addDays(Date date, int days) {
		return addDays(date, days, defaultZoneId);
	}

	/**
	 * Adds days to a java.util.Date using the specified timezone.
	 *
	 * @param date   the Date to add days to
	 * @param days   the number of days to add (can be negative to subtract)
	 * @param zoneId the timezone to use for conversion
	 * @return a new Date with the days added, or null if any input is null
	 */
	public static Date addDays(Date date, int days, ZoneId zoneId) {
		if (date == null || zoneId == null) {
			return null;
		}
		LocalDateTime localDateTime = toLocalDateTime(date, zoneId);
		LocalDateTime resultDateTime = localDateTime.plusDays(days);
		return toDate(resultDateTime, zoneId);
	}

	/**
	 * Subtracts days from a java.util.Date using the default timezone.
	 *
	 * @param date the Date to subtract days from
	 * @param days the number of days to subtract
	 * @return a new Date with the days subtracted, or null if input is null
	 */
	public static Date subtractDays(Date date, int days) {
		return subtractDays(date, days, defaultZoneId);
	}

	/**
	 * Subtracts days from a java.util.Date using the specified timezone.
	 *
	 * @param date   the Date to subtract days from
	 * @param days   the number of days to subtract
	 * @param zoneId the timezone to use for conversion
	 * @return a new Date with the days subtracted, or null if any input is null
	 */
	public static Date subtractDays(Date date, int days, ZoneId zoneId) {
		if (date == null || zoneId == null) {
			return null;
		}
		return addDays(date, -days, zoneId);
	}

	/**
	 * Adds days to a LocalDate.
	 *
	 * @param date the LocalDate to add days to
	 * @param days the number of days to add (can be negative to subtract)
	 * @return a new LocalDate with the days added, or null if input is null
	 */
	public static LocalDate addDays(LocalDate date, int days) {
		return date != null ? date.plusDays(days) : null;
	}

	/**
	 * Subtracts days from a LocalDate.
	 *
	 * @param date the LocalDate to subtract days from
	 * @param days the number of days to subtract
	 * @return a new LocalDate with the days subtracted, or null if input is null
	 */
	public static LocalDate subtractDays(LocalDate date, int days) {
		return date != null ? date.minusDays(days) : null;
	}

	/**
	 * Adds days to a LocalDateTime.
	 *
	 * @param dateTime the LocalDateTime to add days to
	 * @param days     the number of days to add (can be negative to subtract)
	 * @return a new LocalDateTime with the days added, or null if input is null
	 */
	public static LocalDateTime addDays(LocalDateTime dateTime, int days) {
		return dateTime != null ? dateTime.plusDays(days) : null;
	}

	/**
	 * Subtracts days from a LocalDateTime.
	 *
	 * @param dateTime the LocalDateTime to subtract days from
	 * @param days     the number of days to subtract
	 * @return a new LocalDateTime with the days subtracted, or null if input is
	 *         null
	 */
	public static LocalDateTime subtractDays(LocalDateTime dateTime, int days) {
		return dateTime != null ? dateTime.minusDays(days) : null;
	}
}