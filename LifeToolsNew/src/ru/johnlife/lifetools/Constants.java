package ru.johnlife.lifetools;

import java.util.Locale;

public interface Constants {
//	static final Pattern CURLY_SPLITTER = Pattern.compile("(?<=[}])");
//	static final Pattern ANGLE_SPLITTER = Pattern.compile("(?<=[>])");
	static final String UTF_8 = "UTF-8";
	static final Locale RUSSIAN = new Locale("ru");
	static final Locale ENGLISH_US = Locale.US;
	
	static final String PACKAGE = "ru.johnlife.lifetools.";
	static final String ACTION = PACKAGE + "action.";
	
	public static final String ACTION_REFRESH_FRAGMENT = ACTION + "refresh.fragment";
	public static final String ACTION_BACK_FRAGMENT = ACTION + "back.fragment";
	public static final String EXTRA_MASTER_NAME = "name.master.extra";
	public static final String EXTRA_MASTER_ARGS = "args.master.extra";
	public static final String EXTRA_MASTER_STATE = "state.master.extra";
	public static final String EXTRA_DETAIL_NAME = "name.detail.extra";
	public static final String EXTRA_DETAIL_ARGS = "args.detail.extra";
	public static final String EXTRA_ARGS = "args.extra";
}
