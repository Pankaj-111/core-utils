package com.github.pankaj111.coreutils.date;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;

/**
 * Utility class for Calendar operations and conversions. Provides methods for
 * complex date manipulations using Calendar API. All methods are null-safe and
 * return null if any input parameter is null.
 * <p>
 * This class is thread-safe for most operations (returns new Calendar
 * instances).
 * </p>
 * 
 * @author Pankaj Singh
 * @version 1.0
 */
public final class CalendarUtils {

	// Private constructor to prevent instantiation
	private CalendarUtils() {
		throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
	}

	/**
	 * Creates a new Calendar instance with the current date and time.
	 *
	 * @return a new Calendar instance, or null if creation fails
	 */
	public static Calendar now() {
		return Calendar.getInstance();
	}

	/**
	 * Creates a new Calendar instance with the current date and time in the
	 * specified timezone.
	 *
	 * @param zoneId the timezone to use
	 * @return a new Calendar instance, or null if any input is null
	 */
	public static Calendar now(ZoneId zoneId) {
		if (zoneId == null) {
			return null;
		}
		return Calendar.getInstance(java.util.TimeZone.getTimeZone(zoneId));
	}

	/**
	 * Converts a java.util.Date to Calendar using the default timezone.
	 *
	 * @param date the Date to convert
	 * @return the Calendar instance, or null if input is null
	 */
	public static Calendar toCalendar(Date date) {
		if (date == null) {
			return null;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar;
	}

	/**
	 * Converts a java.util.Date to Calendar using the specified timezone.
	 *
	 * @param date   the Date to convert
	 * @param zoneId the timezone to use for conversion
	 * @return the Calendar instance, or null if any input is null
	 */
	public static Calendar toCalendar(Date date, ZoneId zoneId) {
		if (date == null || zoneId == null) {
			return null;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeZone(java.util.TimeZone.getTimeZone(zoneId));
		calendar.setTime(date);
		return calendar;
	}

	/**
	 * Converts a LocalDateTime to Calendar using the system default timezone.
	 *
	 * @param localDateTime the LocalDateTime to convert
	 * @return the Calendar instance, or null if input is null
	 */
	public static Calendar toCalendar(LocalDateTime localDateTime) {
		if (localDateTime == null) {
			return null;
		}
		return toCalendar(Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()));
	}

	/**
	 * Converts a LocalDateTime to Calendar using the specified timezone.
	 *
	 * @param localDateTime the LocalDateTime to convert
	 * @param zoneId        the timezone to use
	 * @return the Calendar instance, or null if any input is null
	 */
	public static Calendar toCalendar(LocalDateTime localDateTime, ZoneId zoneId) {
		if (localDateTime == null || zoneId == null) {
			return null;
		}
		return toCalendar(Date.from(localDateTime.atZone(zoneId).toInstant()), zoneId);
	}

	/**
	 * Converts a Calendar to java.util.Date.
	 *
	 * @param calendar the Calendar to convert
	 * @return the Date instance, or null if input is null
	 */
	public static Date toDate(Calendar calendar) {
		return calendar != null ? calendar.getTime() : null;
	}

	/**
	 * Converts a Calendar to LocalDateTime using the calendar's timezone.
	 *
	 * @param calendar the Calendar to convert
	 * @return the LocalDateTime instance, or null if input is null
	 */
	public static LocalDateTime toLocalDateTime(Calendar calendar) {
		if (calendar == null) {
			return null;
		}
		return LocalDateTime.ofInstant(calendar.toInstant(), calendar.getTimeZone().toZoneId());
	}

	/**
	 * Converts a Calendar to LocalDate using the calendar's timezone.
	 *
	 * @param calendar the Calendar to convert
	 * @return the LocalDate instance, or null if input is null
	 */
	public static LocalDate toLocalDate(Calendar calendar) {
		LocalDateTime localDateTime = toLocalDateTime(calendar);
		return localDateTime != null ? localDateTime.toLocalDate() : null;
	}

	/**
	 * Adds a specified amount of time to a Calendar using Calendar field constants.
	 *
	 * @param calendar the Calendar to add time to
	 * @param field    the calendar field (e.g., Calendar.DAY_OF_MONTH,
	 *                 Calendar.HOUR)
	 * @param amount   the amount of time to add (can be negative to subtract)
	 * @return a new Calendar with the time added, or null if any input is null
	 */
	public static Calendar add(Calendar calendar, int field, int amount) {
		if (calendar == null) {
			return null;
		}
		Calendar result = (Calendar) calendar.clone();
		result.add(field, amount);
		return result;
	}

	/**
	 * Subtracts a specified amount of time from a Calendar using Calendar field
	 * constants.
	 *
	 * @param calendar the Calendar to subtract time from
	 * @param field    the calendar field (e.g., Calendar.DAY_OF_MONTH,
	 *                 Calendar.HOUR)
	 * @param amount   the amount of time to subtract
	 * @return a new Calendar with the time subtracted, or null if any input is null
	 */
	public static Calendar subtract(Calendar calendar, int field, int amount) {
		return add(calendar, field, -amount);
	}

	/**
	 * Sets a specific field in a Calendar to a given value.
	 *
	 * @param calendar the Calendar to modify
	 * @param field    the calendar field to set (e.g., Calendar.HOUR_OF_DAY)
	 * @param value    the value to set
	 * @return a new Calendar with the field set, or null if any input is null
	 */
	public static Calendar set(Calendar calendar, int field, int value) {
		if (calendar == null) {
			return null;
		}
		Calendar result = (Calendar) calendar.clone();
		result.set(field, value);
		return result;
	}

	/**
	 * Gets the value of a specific field from a Calendar.
	 *
	 * @param calendar the Calendar to read from
	 * @param field    the calendar field to get (e.g., Calendar.HOUR_OF_DAY)
	 * @return the field value, or -1 if any input is null
	 */
	public static int get(Calendar calendar, int field) {
		if (calendar == null) {
			return -1;
		}
		return calendar.get(field);
	}

	/**
	 * Checks if two Calendar instances represent the same day (ignoring time).
	 *
	 * @param cal1 the first Calendar
	 * @param cal2 the second Calendar
	 * @return true if both represent the same day, false otherwise or if any input
	 *         is null
	 */
	public static boolean isSameDay(Calendar cal1, Calendar cal2) {
		if (cal1 == null || cal2 == null) {
			return false;
		}
		return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
				&& cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
	}

	/**
	 * Gets the first day of the month for a given Calendar.
	 *
	 * @param calendar the Calendar
	 * @return a new Calendar set to the first day of the month, or null if input is
	 *         null
	 */
	public static Calendar getFirstDayOfMonth(Calendar calendar) {
		if (calendar == null) {
			return null;
		}
		Calendar result = (Calendar) calendar.clone();
		result.set(Calendar.DAY_OF_MONTH, 1);
		return clearTime(result);
	}

	/**
	 * Gets the last day of the month for a given Calendar.
	 *
	 * @param calendar the Calendar
	 * @return a new Calendar set to the last day of the month, or null if input is
	 *         null
	 */
	public static Calendar getLastDayOfMonth(Calendar calendar) {
		if (calendar == null) {
			return null;
		}
		Calendar result = (Calendar) calendar.clone();
		result.set(Calendar.DAY_OF_MONTH, result.getActualMaximum(Calendar.DAY_OF_MONTH));
		return clearTime(result);
	}

	/**
	 * Checks if a Calendar instance represents a weekend.
	 *
	 * @param calendar the Calendar to check
	 * @return true if it's a weekend (Saturday or Sunday), false otherwise or if
	 *         input is null
	 */
	public static boolean isWeekend(Calendar calendar) {
		if (calendar == null) {
			return false;
		}
		int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
		return dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY;
	}

	/**
	 * Checks if a Calendar instance represents a weekday.
	 *
	 * @param calendar the Calendar to check
	 * @return true if it's a weekday (Monday to Friday), false otherwise or if
	 *         input is null
	 */
	public static boolean isWeekday(Calendar calendar) {
		return !isWeekend(calendar);
	}

	/**
	 * Gets the start of day for a Calendar (sets time to 00:00:00.000).
	 *
	 * @param calendar the Calendar
	 * @return a new Calendar with time set to start of day, or null if input is
	 *         null
	 */
	public static Calendar startOfDay(Calendar calendar) {
		if (calendar == null) {
			return null;
		}
		Calendar result = (Calendar) calendar.clone();
		result.set(Calendar.HOUR_OF_DAY, 0);
		result.set(Calendar.MINUTE, 0);
		result.set(Calendar.SECOND, 0);
		result.set(Calendar.MILLISECOND, 0);
		return result;
	}

	/**
	 * Gets the end of day for a Calendar (sets time to 23:59:59.999).
	 *
	 * @param calendar the Calendar
	 * @return a new Calendar with time set to end of day, or null if input is null
	 */
	public static Calendar endOfDay(Calendar calendar) {
		if (calendar == null) {
			return null;
		}
		Calendar result = (Calendar) calendar.clone();
		result.set(Calendar.HOUR_OF_DAY, 23);
		result.set(Calendar.MINUTE, 59);
		result.set(Calendar.SECOND, 59);
		result.set(Calendar.MILLISECOND, 999);
		return result;
	}

	/**
	 * Clears the time portion of a Calendar (sets time to 00:00:00.000).
	 *
	 * @param calendar the Calendar
	 * @return a new Calendar with time cleared, or null if input is null
	 */
	public static Calendar clearTime(Calendar calendar) {
		return startOfDay(calendar);
	}

	/**
	 * Calculates the difference between two Calendars in the specified unit.
	 *
	 * @param cal1 the first Calendar
	 * @param cal2 the second Calendar
	 * @param unit the unit to calculate difference in
	 * @return the difference in the specified unit, or -1 if any input is null
	 */
	public static long difference(Calendar cal1, Calendar cal2, ChronoUnit unit) {
		if (cal1 == null || cal2 == null || unit == null) {
			return -1;
		}
		return unit.between(cal1.toInstant(), cal2.toInstant());
	}

	/**
	 * Checks if a Calendar is before another Calendar.
	 *
	 * @param cal1 the first Calendar
	 * @param cal2 the second Calendar
	 * @return true if cal1 is before cal2, false otherwise or if any input is null
	 */
	public static boolean isBefore(Calendar cal1, Calendar cal2) {
		if (cal1 == null || cal2 == null) {
			return false;
		}
		return cal1.before(cal2);
	}

	/**
	 * Checks if a Calendar is after another Calendar.
	 *
	 * @param cal1 the first Calendar
	 * @param cal2 the second Calendar
	 * @return true if cal1 is after cal2, false otherwise or if any input is null
	 */
	public static boolean isAfter(Calendar cal1, Calendar cal2) {
		if (cal1 == null || cal2 == null) {
			return false;
		}
		return cal1.after(cal2);
	}

	/**
	 * Checks if a Calendar is between two other Calendars (inclusive).
	 *
	 * @param calendar the Calendar to check
	 * @param start    the start Calendar
	 * @param end      the end Calendar
	 * @return true if calendar is between start and end (inclusive), false
	 *         otherwise or if any input is null
	 */
	public static boolean isBetween(Calendar calendar, Calendar start, Calendar end) {
		if (calendar == null || start == null || end == null) {
			return false;
		}
		return !calendar.before(start) && !calendar.after(end);
	}
}