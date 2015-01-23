package com.wk.mailsearch.constants;

/**
 * The Application Constants Class
 * @author anchal.kataria
 *
 */
public final class ApplicationConstants {



	/** The Constant PROPERTY_FILEPATH. */
	public static final String PROPERTY_FILEPATH="D:/wk-emailindexing/email_code/OutlookMailSearch/src/main/resources/config.properties";

	/** The Constant SEPARATOR. */
	public static final String SEPARATOR = "_";
	
	/** The Constant DAILY_UPDATES. */
	// Fire at 12 noon every day
	public static final String DAILY_UPDATES = "0 0 12 * * ?";
	
	/** The Constant WEEKLY_UPDATES. */
	//Fire at 12 noon every week on friday
	public static final String WEEKLY_UPDATES = "0 0 12 ? * FRI" ;
	
	/** The Constant MONTHLY_UPDATES. */
	//Fire at 12:00 am on every 15th of the month
	public static final String MONTHLY_UPDATES = "0 0 15 * *";
	
}
