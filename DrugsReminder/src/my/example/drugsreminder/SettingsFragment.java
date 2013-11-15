package my.example.drugsreminder;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

public class SettingsFragment extends Fragment {
	
	EditText mEditMorning;
	EditText mEditAfternoon;
	EditText mEditEvening;
	CheckBox mAlarmsCheck;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.settings, container, false);
		mEditMorning = (EditText)view.findViewById(R.id.edit_morning);
		mEditAfternoon = (EditText)view.findViewById(R.id.edit_afternoon);
		mEditEvening = (EditText)view.findViewById(R.id.edit_evening);
		mAlarmsCheck = (CheckBox)view.findViewById(R.id.alarms_enabled);
		
		setHasOptionsMenu(true);
		
		mEditMorning.setText(DrugsData.getPref(getActivity(), DrugsData.PREFS.MORNING));
		mEditAfternoon.setText(DrugsData.getPref(getActivity(), DrugsData.PREFS.AFTERNOON));
		mEditEvening.setText(DrugsData.getPref(getActivity(), DrugsData.PREFS.EVENING));
		mAlarmsCheck.setChecked(DrugsData.getBoolPref(getActivity(), DrugsData.PREFS.ALARMS_ENABLED));
		
		return view;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.settings, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.action_accept:
			SharedPreferences.Editor ed = getActivity()
				.getSharedPreferences(DrugsData.PREFS.NAME, 0).edit();
			ed.putString(DrugsData.PREFS.MORNING, mEditMorning.getText().toString());
			ed.putString(DrugsData.PREFS.AFTERNOON, mEditAfternoon.getText().toString());
			ed.putString(DrugsData.PREFS.EVENING, mEditEvening.getText().toString());
			ed.putBoolean(DrugsData.PREFS.ALARMS_ENABLED, mAlarmsCheck.isChecked());
			if(ed.commit()) {
				AlarmReceiver.scheduleAlarms(getActivity());
				getActivity().onBackPressed();
			}
		}
		return super.onOptionsItemSelected(item);
	}
	
	
}
