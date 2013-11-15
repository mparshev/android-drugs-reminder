package my.example.drugsreminder;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.DateFormat;
import android.util.Log;

public class DrugsData extends ContentProvider {

	public static final String AUTHORITY = "my.example.drugsreminder";
	
	public final static Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
	
	public static final class DRUGS {
		public static final String TABLE = "drugs";
		
		public static final Uri URI = Uri.withAppendedPath(CONTENT_URI, TABLE);

		public static final String _ID = BaseColumns._ID;
		public static final String DRUG = "drug";
		public static final String DOSAGE = "dosage";
		public static final String QUANTITY = "quantity";
		public static final String DIRECTIONS = "directions";
		public static final String MORNING = "morning";
		public static final String AFTERNOON = "afternoon";
		public static final String EVENING = "evening";
		public static final String FROM_DATE = "from_date";
		public static final String DURATION = "duration";
		public static final String DAYS_APART = "days_apart";
		public static final String NOTES = "notes";

		private static final String _CREATE_SQL = "CREATE TABLE " + TABLE + " (" +
				_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
				DRUG + " TEXT, " + 
				DOSAGE + " TEXT, " + 
				QUANTITY + " INTEGER, " +
				DIRECTIONS + " INTEGER, " + 
				MORNING + " INTEGER, " +
				AFTERNOON + " INTEGER, " +
				EVENING + " INTEGER, " +
				FROM_DATE + " INTEGER, " +
				DURATION + " INTEGER, " +
				DAYS_APART + " INTEGER, " +
				NOTES  + " TEXT "+ ")";
		
	}

	public static final class INTAKES {
		
		public static final String TABLE = "intakes";

		public static final Uri URI = Uri.withAppendedPath(CONTENT_URI, TABLE);
		
		public static final String _ID = BaseColumns._ID;
		public static final String DRUG_ID = "drug_id";
		public static final String DATE = "intake_date";
		public static final String TIME = "intake_time";
		public static final String DRUG = "drug";
		public static final String DOSAGE = "dosage";
		public static final String QUANTITY = "quantity";
		public static final String DIRECTIONS = "directions";
		public static final String TAKEN = "taken";

		private static final String _CREATE_SQL = "CREATE TABLE " + TABLE + " (" +
				_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
				DRUG_ID + " INTEGER, " +
				DATE + " INTEGER, " + 
				TIME + " INTEGER, " +
				DRUG + " TEXT, " + 
				DOSAGE + " TEXT, " + 
				QUANTITY + " INTEGER, " +
				DIRECTIONS + " INTEGER, " + 
				TAKEN + " BOOLEAN," +
				" UNIQUE (" + DRUG_ID + "," + DATE + "," + TIME + ")" + ")";
		
	}
	
	private static final UriMatcher sUriMatcher;
	
	public static final class PREFS {
		
		public static final String NAME = "DrugsPrefs";
		
		public static final String MORNING = "morning";
		public static final String AFTERNOON = "afternoon";
		public static final String EVENING = "evening";
		
		public static final String ALARMS_ENABLED = "alarms_enabled";
		
	}

	public static final int DRUGS_QUERY = 1;
	public static final int DRUGS_ROW_QUERY = 2;
	public static final int INTAKES_QUERY = 3;
	public static final int INTAKES_ROW_QUERY = 4;
	
	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(AUTHORITY, DRUGS.TABLE, DRUGS_QUERY);
		sUriMatcher.addURI(AUTHORITY, DRUGS.TABLE + "/#", DRUGS_ROW_QUERY);
		sUriMatcher.addURI(AUTHORITY, INTAKES.TABLE, INTAKES_QUERY);
		sUriMatcher.addURI(AUTHORITY, INTAKES.TABLE + "/#", INTAKES_ROW_QUERY);
	}
	
	private static final String DATABASE_NAME = "drugsdb";
	private static final int DATABASE_VERSION = 4;

	private DataHelper mDataHelper;
	
	private static class DataHelper extends SQLiteOpenHelper {

		public DataHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DRUGS._CREATE_SQL);
			db.execSQL(INTAKES._CREATE_SQL);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
			db.execSQL(" DROP TABLE IF EXISTS " + DRUGS.TABLE);
			db.execSQL(" DROP TABLE IF EXISTS " + INTAKES.TABLE);
			onCreate(db);
		}
		
	}

	@Override
	public boolean onCreate() {
		mDataHelper = new DataHelper(getContext());
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		switch(sUriMatcher.match(uri)) {
		case DRUGS_QUERY:
			Cursor cursor = mDataHelper.getReadableDatabase()
					.query(DRUGS.TABLE, null, null, null, null, null, null);
			cursor.setNotificationUri(getContext().getContentResolver(), DRUGS.URI);
			return cursor;
		case DRUGS_ROW_QUERY:
			return mDataHelper.getReadableDatabase()
					.query(DRUGS.TABLE, null, DRUGS._ID + " = " + uri.getLastPathSegment(), null, null, null, null);
		case INTAKES_QUERY:
			try {
				Date date = trimDate(new Date(Long.parseLong(selectionArgs[0])));
				int time = Integer.parseInt(selectionArgs[1]);
				populateIntakes(date, time);
			} catch(NumberFormatException ex) {
				throw new RuntimeException("Invalid arguments for INTAKES_QUERY");
			}
			cursor = mDataHelper.getReadableDatabase().query(INTAKES.TABLE, null, 
							selection, selectionArgs, null, null, null);
			return cursor;
		case INTAKES_ROW_QUERY:
			return mDataHelper.getReadableDatabase()
					.query(INTAKES.TABLE, null, INTAKES._ID + " = " + uri.getLastPathSegment(), null, null, null, null);
		}
		return null;
	}

	private void populateIntakes(Date date, int time) {
		String timeColumn = new String[] { DRUGS.MORNING, DRUGS.AFTERNOON, DRUGS.EVENING } [time]; 
		Cursor cursor = mDataHelper.getReadableDatabase()
				.query(DRUGS.TABLE, null, null, null, null, null, null);
		while(cursor.moveToNext()) {
			if(getBoolean(cursor,timeColumn)) {
				ContentValues values = new ContentValues();
				values.put(INTAKES.DRUG_ID, getLong(cursor, DRUGS._ID));
				
				values.put(INTAKES.DRUG, getString(cursor, DRUGS.DRUG));
				values.put(INTAKES.DOSAGE, getFloat(cursor, DRUGS.DOSAGE));
				values.put(INTAKES.QUANTITY, getInt(cursor, DRUGS.QUANTITY));
				values.put(INTAKES.DIRECTIONS, getInt(cursor, DRUGS.DIRECTIONS));
			
				Date fromDate = getDate(cursor, DRUGS.FROM_DATE);
				int duration = getInt(cursor, DRUGS.DURATION);
				if(duration < 1) { 
					fromDate = date;
					duration = 1;
				} else {
					if(fromDate == null) break;
				}
				int days = 1 + getInt(cursor, DRUGS.DAYS_APART);
				for(int i=0; i < duration; i++) {
					if(fromDate.getTime() > date.getTime()) break;
					values.put(INTAKES.DATE, trimDate(fromDate).getTime());
					values.put(INTAKES.TIME, time);
					values.put(INTAKES.TAKEN, 0);
					mDataHelper.getWritableDatabase().insertWithOnConflict(
							INTAKES.TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
					fromDate = addDays(fromDate,days);
				}
			}
		}
		cursor.close();
	}

	private boolean clearIntakes(long id) {
		int rows = mDataHelper.getWritableDatabase().delete(INTAKES.TABLE, INTAKES.DRUG_ID + "=" + id +" AND NOT " + INTAKES.TAKEN, null);
		Log.d("clearIntakes","rows="+rows);
		Cursor cursor = mDataHelper.getReadableDatabase().query(INTAKES.TABLE, null, INTAKES.DRUG_ID + " = " + id, null, null, null, null);
		boolean result = cursor.getCount() == 0;
		cursor.close();
		getContext().getContentResolver().notifyChange(INTAKES.URI, null);
		return result;
	}
	
	public static Date trimDate(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}
	
	public static Date addDays(Date date, int days) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DATE, days);
		return calendar.getTime();
	}
	
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		switch(sUriMatcher.match(uri)) {
		case DRUGS_QUERY:
			long id = mDataHelper.getWritableDatabase()
				.insert(DRUGS.TABLE, null, values);
			getContext().getContentResolver().notifyChange(DRUGS.URI, null);
			return Uri.withAppendedPath(uri, ""+id);
		}
		return null;
	}


	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		switch (sUriMatcher.match(uri)) {
		case DRUGS_ROW_QUERY:
			long id = Long.valueOf(uri.getLastPathSegment());
			clearIntakes(id);
			int rows = mDataHelper.getWritableDatabase().update(DRUGS.TABLE, values, 
					DRUGS._ID + " = " + id, null);
			if(rows > 0) getContext().getContentResolver().notifyChange(DRUGS.URI, null);
			return rows;
		case INTAKES_ROW_QUERY:
			rows = mDataHelper.getWritableDatabase().update(INTAKES.TABLE, values, 
					INTAKES._ID + " = " + Long.valueOf(uri.getLastPathSegment()), null);
			if(rows > 0) getContext().getContentResolver().notifyChange(INTAKES.URI, null);
			return rows;
		}
		return -1;
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		switch (sUriMatcher.match(uri)) {
		case DRUGS_ROW_QUERY:
			long id = Long.valueOf(uri.getLastPathSegment());
			if(clearIntakes(id)) {
				int rows = mDataHelper.getWritableDatabase().delete(DRUGS.TABLE, 
					DRUGS._ID + " = " + Long.valueOf(uri.getLastPathSegment()), null);
				if(rows > 0) getContext().getContentResolver().notifyChange(DRUGS.URI, null);
				return rows;
			}

		}
		return -1;
	}

	@Override
	public String getType(Uri uri) {
		// Auto-generated method stub
		return null;
	}

	
	public static String getString(Cursor cursor, String columnName) {
		int columnIndex = cursor.getColumnIndex(columnName);
		if(cursor.isNull(columnIndex)) return "";
		return cursor.getString(columnIndex);
	}

	public static int getInt(Cursor cursor, String columnName) {
		int columnIndex = cursor.getColumnIndex(columnName);
		if(cursor.isNull(columnIndex)) return 0;
		return cursor.getInt(columnIndex);
	}

	public static long getLong(Cursor cursor, String columnName) {
		int columnIndex = cursor.getColumnIndex(columnName);
		if(cursor.isNull(columnIndex)) return 0;
		return cursor.getLong(columnIndex);
	}
	
	public static float getFloat(Cursor cursor, String columnName) {
		int columnIndex = cursor.getColumnIndex(columnName);
		if(cursor.isNull(columnIndex)) return 0;
		return cursor.getFloat(columnIndex);
	}
	
	public static boolean getBoolean(Cursor cursor, String columnName) {
		int columnIndex = cursor.getColumnIndex(columnName);
		if(cursor.isNull(columnIndex)) return false;
		return cursor.getInt(columnIndex)>0;
	}
	
	public static Date getDate(Cursor cursor, String columnName) {
		int columnIndex = cursor.getColumnIndex(columnName);
		if(cursor.isNull(columnIndex)) return null;
		return new Date(cursor.getLong(columnIndex));
	}

	public static String formatDate(Context context, Date date) {
		if(date != null) 
			return DateFormat.getDateFormat(context).format(date);
		else
			return "";
	}
	
	public static Long parseDate(Context context, String string) {
		try {
			return DateFormat.getDateFormat(context).parse(string).getTime();
		} catch(ParseException ex) {
			return null;
		}
	}

	public static String formatTime(Context context, int hh, int mm) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, hh);
		calendar.set(Calendar.MINUTE, mm);
		return DateFormat.getTimeFormat(context).format(calendar.getTime());
	}
	
	public static Date parseTime(Context context, String string) {
		try {
			return DateFormat.getTimeFormat(context).parse(string);
		} catch(ParseException ex) {
			return null;
		}
	}

	public static String getPref(Context context, String prefName) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS.NAME, 0);
		if(prefName.equals(PREFS.MORNING))
			return prefs.getString(PREFS.MORNING, formatTime(context, 8, 0));
		if(prefName.equals(PREFS.AFTERNOON))
			return prefs.getString(PREFS.AFTERNOON, formatTime(context, 13, 0));
		if(prefName.equals(PREFS.EVENING))
			return prefs.getString(PREFS.EVENING, formatTime(context, 19, 0));
		return prefs.getString(prefName,"");
	}

	public static boolean getBoolPref(Context context, String prefName) {
		return context.getSharedPreferences(PREFS.NAME, 0).getBoolean(prefName, false);
	}
	
	public static Date getTimePref(Context context, String prefName) {
		return parseTime(context, getPref(context, prefName));
	}
	
}
