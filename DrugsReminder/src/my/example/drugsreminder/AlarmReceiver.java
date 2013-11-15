package my.example.drugsreminder;

import java.util.Calendar;
import java.util.Date;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

public class AlarmReceiver extends BroadcastReceiver {
	
	private static final String ARG_TIME = "arg_time";

	@Override
	public void onReceive(Context context, Intent intent) {
		//Log.d("alarm","alarm received");
		Cursor cursor = context.getContentResolver().query(DrugsData.INTAKES.URI, null, 
				DrugsData.INTAKES.DATE + " = " + " ? " + " AND " +
						DrugsData.INTAKES.TIME + " = " + " ? ",
						new String[] { 
							"" + DrugsData.trimDate(new Date()).getTime(),
							"" + intent.getIntExtra(ARG_TIME, -1) 
						}, null);
		if(cursor.getCount()>0) {
			showNotification(context, 
					context.getString(R.string.app_name), 
					context.getString(R.string.notification_text));
		}
		cursor.close();
	}

	
	public static void scheduleAlarms(Context context) {
		Date[] times = new Date[] {
				DrugsData.parseTime(context, DrugsData.getPref(context, DrugsData.PREFS.MORNING)),
				DrugsData.parseTime(context, DrugsData.getPref(context, DrugsData.PREFS.AFTERNOON)),
				DrugsData.parseTime(context, DrugsData.getPref(context, DrugsData.PREFS.EVENING))
//				addMinutes(new Date(),1)
		};
		boolean enabled = DrugsData.getBoolPref(context, DrugsData.PREFS.ALARMS_ENABLED);
		for(int i=0; i < times.length; i++) {
			scheduleAlarm(context, i, times[i], enabled);
		}
	}

	
	private static void scheduleAlarm(Context context, int id, Date time, boolean enabled) {

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(time);
		int hh = calendar.get(Calendar.HOUR_OF_DAY);
		int mm = calendar.get(Calendar.MINUTE);
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.set(Calendar.HOUR_OF_DAY, hh);
		calendar.set(Calendar.MINUTE, mm);
		if(calendar.getTime().before(new Date())) calendar.add(Calendar.DATE, 1);

		AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, AlarmReceiver.class);
		intent.putExtra(ARG_TIME, id);
		PendingIntent alarmIntent = PendingIntent.getBroadcast(
				context.getApplicationContext(), id, 
				intent, PendingIntent.FLAG_CANCEL_CURRENT);
		if(enabled) {
			alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, 
				calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);
			//Log.d("alarms","Alarm scheduled at "+time);
		} else {
			alarmManager.cancel(alarmIntent);
			//Log.d("alarms","Alarm canceled");
		}
	}

/*
	private static Date addMinutes(Date date, int minutes) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.MINUTE, minutes);
		return calendar.getTime();
	}
*/
	
	private void showNotification(Context context, String title, String text) {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		builder.setSmallIcon(R.drawable.ic_launcher);
		builder.setContentTitle(title);
		builder.setContentText(text);
		builder.setAutoCancel(true);
		Intent resultIntent = new Intent(context, MainActivity.class);
		TaskStackBuilder taskBuilder = TaskStackBuilder.create(context);
		taskBuilder.addParentStack(MainActivity.class);
		taskBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent = 
				taskBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(resultPendingIntent);
		NotificationManager notificationManager = 
				(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(1, builder.build());
		
	}
}
