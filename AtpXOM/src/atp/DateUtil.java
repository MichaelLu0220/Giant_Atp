package atp;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class DateUtil implements Serializable{
	private static final long serialVersionUID = 1L;
	
	public DateUtil() throws ParseException{
	}
	
	public static Date now() {
		return Calendar.getInstance().getTime();
	}

	public static Date makeDate(int year, int month, int day) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month);
		cal.set(Calendar.DAY_OF_MONTH, day);
		return cal.getTime();
	}

	public static Date dateAsDay(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		return cal.getTime();
	}

	public static Date addDays(Date date, int days) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DATE, days);
		return cal.getTime();
	}

	/**
	 * Compute the number of days between 2 dates. This method is simply done for
	 * the purpose of this sample. It is more accurate (but less efficent) than the
	 * following : long startTime = startDate.getTime(); long endTime =
	 * endDate.getTime(); return (int)((endTime - startTime) / (24*3600*1000)); For
	 * example the above should fail if the beginning of Daylight Saving Time is
	 * between startDate and endDate For accurate algorithm use dedicated date
	 * library.
	 * 
	 * @param startDate
	 * @param endDate
	 * @return The duration in days
	 */
	public static int getDuration(Date startDate, Date endDate) {
		int tempDifference = 0;
		Calendar earlier = Calendar.getInstance();
		Calendar later = Calendar.getInstance();
		if (startDate.compareTo(endDate) < 0) {
			earlier.setTime(startDate);
			later.setTime(endDate);
		} else {
			earlier.setTime(endDate);
			later.setTime(startDate);
		}

		earlier.set(Calendar.HOUR_OF_DAY, 0);
		earlier.set(Calendar.MINUTE, 0);
		earlier.set(Calendar.SECOND, 0);
		earlier.set(Calendar.MILLISECOND, 0);

		later.set(Calendar.HOUR_OF_DAY, 0);
		later.set(Calendar.MINUTE, 0);
		later.set(Calendar.SECOND, 0);
		later.set(Calendar.MILLISECOND, 0);

		while (true) {
			// Did we reach the endDate ?
			if (earlier.equals(later))
				return tempDifference;
			// Date incrementation
			earlier.add(Calendar.DAY_OF_MONTH, 2);
			// Add 2 days
			tempDifference = tempDifference + 2;
			// Guard
			if (earlier.getTime().after(later.getTime())) {
				return tempDifference - 1;
			}
		}
	}

	public static Iterator<Object> iterator(final Date startDate, final Date endDate) {
		return new Iterator<Object>() {
			Date currentDate = startDate;

			public boolean hasNext() {
				if (currentDate.after(endDate))
					return false;
				return true;
			}

			public Object next() {
				Date returnDate = currentDate;
				if (hasNext()) {
					currentDate = addDays(currentDate, 1);
					return returnDate;
				} else {
					throw new NoSuchElementException(currentDate + " is after " + endDate);
				}
			}

			public void remove() {
				throw new UnsupportedOperationException("Date iterator is read only");
			}
		};
	}

	public static String format(Date date) {
		DateFormat formatter = DateFormat.getDateInstance();
		return formatter.format(date);
	}
}