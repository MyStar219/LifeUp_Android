package ru.johnlife.lifetools.data;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public abstract class AbstractData {
	private static final DateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);

	protected static final long parseDate(String date) {
		try {
			return isoDateFormat.parse(date).getTime();
		} catch (ParseException e) {
			return 0;
		}
	}
}
