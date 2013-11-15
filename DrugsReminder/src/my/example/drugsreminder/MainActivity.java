package my.example.drugsreminder;

import java.text.ParseException;
import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

public class MainActivity extends ActionBarActivity {
	
	private static final int FRAGMENT_INTAKES = 0;
	private static final int FRAGMENT_DRUGS = 1;

	int mFragmentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mFragmentId = FRAGMENT_INTAKES;
        
        showCurrentFragment();
        
        //SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.navigation,
        //        android.R.layout.simple_spinner_dropdown_item);
    	//final ActionBar bar = getSupportActionBar();	
        //bar.setDisplayShowTitleEnabled(false);
        //bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        //bar.setListNavigationCallbacks(mSpinnerAdapter, this);
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.action_intakes:
			mFragmentId = FRAGMENT_INTAKES;
			item.setChecked(true);
			showCurrentFragment();
			return true;
		case R.id.action_drugs:
			mFragmentId = FRAGMENT_DRUGS;
			item.setChecked(true);
			showCurrentFragment();
			return true;
		case R.id.action_new:
			FragmentTransaction tr = getSupportFragmentManager().beginTransaction();
			tr.replace(R.id.fragment_container, new DrugsFormFragment());
			tr.addToBackStack(null);
			tr.commit();
			return true;
		case R.id.action_settings:
			tr = getSupportFragmentManager().beginTransaction();
			tr.replace(R.id.fragment_container, new SettingsFragment());
			tr.addToBackStack(null);
			tr.commit();
			return true;
			
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		switch(mFragmentId) {
		case FRAGMENT_INTAKES:
			menu.findItem(R.id.action_intakes).setChecked(true); break;
		case FRAGMENT_DRUGS:
			menu.findItem(R.id.action_drugs).setChecked(true); break;
		}
//		menu.findItem(R.id.action_intakes).setChecked(mFragmentId == FRAGMENT_INTAKES);
//		menu.findItem(R.id.action_drugs).setChecked(mFragmentId == FRAGMENT_DRUGS);
		return true;
		//return super.onPrepareOptionsMenu(menu);
	}

	private static final String ARG_RES_ID = "arg_res_id";
	
	public void showDatePicker(View view) {
		DialogFragment fragment = new DatePickerFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_RES_ID, view.getId());
		fragment.setArguments(args);
    	fragment.show(getSupportFragmentManager(), null);
    }
	
	public static class DatePickerFragment extends DialogFragment 
							implements OnDateSetListener {

		EditText mEditText;
		boolean mDateSet;
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			
			Calendar calendar = Calendar.getInstance();
			
			mEditText = (EditText)getActivity().findViewById(getArguments().getInt(ARG_RES_ID));
			try {
				calendar.setTime(DateFormat.getDateFormat(getActivity()).parse(mEditText.getText().toString()));
			} catch (ParseException e) {
				// Do nothing
			}
			mDateSet = false;
			return new DatePickerDialog(getActivity(), this, 
					calendar.get(Calendar.YEAR), 
					calendar.get(Calendar.MONTH), 
					calendar.get(Calendar.DAY_OF_MONTH));
		}

		@Override
		public void onDateSet(DatePicker view, int year, int month, int day) {
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.YEAR, year);
			calendar.set(Calendar.MONTH, month);
			calendar.set(Calendar.DAY_OF_MONTH, day);
			mEditText.setText(DrugsData.formatDate(getActivity(), calendar.getTime()));
			mDateSet = true;
		}

		@Override
		public void onDismiss(DialogInterface dialog) {
			if(!mDateSet) mEditText.setText("");
			super.onDismiss(dialog);
		}
		
	}

	public void showTimePicker(View view) {
		DialogFragment fragment = new TimePickerFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_RES_ID, view.getId());
		fragment.setArguments(args);
    	fragment.show(getSupportFragmentManager(), null);
    }
	
	public static class TimePickerFragment extends DialogFragment 
							implements OnTimeSetListener {

		EditText mEditText;
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			Calendar calendar = Calendar.getInstance();

			mEditText = (EditText)getActivity().findViewById(getArguments().getInt(ARG_RES_ID));
			try {
				calendar.setTime(DateFormat.getTimeFormat(getActivity()).parse(mEditText.getText().toString()));
			} catch (ParseException e) {
				// Do nothing
			}
			
			return new TimePickerDialog(getActivity(), this, 
					calendar.get(Calendar.HOUR_OF_DAY), 
					calendar.get(Calendar.MINUTE), 
					DateFormat.is24HourFormat(getActivity()));
		}

		@Override
		public void onTimeSet(TimePicker view, int hh, int mm) {
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.HOUR_OF_DAY, hh);
			calendar.set(Calendar.MINUTE, mm);
			mEditText.setText(DateFormat.getTimeFormat(getActivity()).format(calendar.getTime()));
		}

	}


	@Override
	public void onBackPressed() {
		if(getSupportFragmentManager().popBackStackImmediate()) {
			showCurrentFragment();
		} else {
			super.onBackPressed();
		}
	}

	
	public void onItemClick(View view) {
		CheckBox checkBox = (CheckBox)view;
		ContentValues values = new ContentValues();
		values.put(DrugsData.INTAKES.TAKEN, checkBox.isChecked() ? 1 : 0);
		getContentResolver().update(Uri.withAppendedPath(DrugsData.INTAKES.URI, ""+checkBox.getTag()), values, null, null);
	}


	private void showCurrentFragment() {
		Fragment fragment = null;
		switch(mFragmentId) {
		case 0:
			fragment = new IntakesPagerFragment();
			break;
		case 1:
			fragment = new DrugsListFragment();
			break;
		}
        if(fragment != null) {
			FragmentTransaction tr = getSupportFragmentManager().beginTransaction();
        	tr.replace(R.id.fragment_container, fragment);
        	tr.disallowAddToBackStack();
	        tr.commit();
        }
	}

}
