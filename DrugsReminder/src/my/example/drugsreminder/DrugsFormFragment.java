package my.example.drugsreminder;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
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
import android.widget.Spinner;

public class DrugsFormFragment extends Fragment {
	
	Long mDrugId;
	
	EditText mDrug;
	EditText mDosage;
	EditText mQuantity;
	CheckBox mMorning;
	CheckBox mAfternoon;
	CheckBox mEvening;
	Spinner  mDirections;
	EditText mFromDate;
	EditText mDuration;
	EditText mDaysApart;
	EditText mNotes;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.drugs_form, container, false);
		mDrug = (EditText)view.findViewById(R.id.drug);
		mDosage = (EditText)view.findViewById(R.id.dosage);
		mQuantity = (EditText)view.findViewById(R.id.quantity);
		mMorning = (CheckBox)view.findViewById(R.id.morning);
		mAfternoon = (CheckBox)view.findViewById(R.id.afternoon);
		mEvening = (CheckBox)view.findViewById(R.id.evening);
		mDirections = (Spinner)view.findViewById(R.id.directions);
		mFromDate = (EditText)view.findViewById(R.id.from_date);
		mDuration = (EditText)view.findViewById(R.id.duration);
		mDaysApart = (EditText)view.findViewById(R.id.days_apart);
		mNotes = (EditText)view.findViewById(R.id.notes);
		setHasOptionsMenu(true);
	
		// TODO: first try to get id from bundle
		if(getArguments() != null) mDrugId = getArguments().getLong(DrugsData.DRUGS._ID);

		if(mDrugId != null) {
			Cursor cursor = getActivity().getContentResolver()
					.query(Uri.withAppendedPath(DrugsData.DRUGS.URI, ""+mDrugId), null, null, null, null);
			if(cursor!=null && cursor.moveToFirst()) {
				mDrug.setText(DrugsData.getString(cursor, DrugsData.DRUGS.DRUG));
				mDosage.setText(DrugsData.getString(cursor, DrugsData.DRUGS.DOSAGE));
				mQuantity.setText(DrugsData.getString(cursor, DrugsData.DRUGS.QUANTITY));
				mMorning.setChecked(DrugsData.getBoolean(cursor, DrugsData.DRUGS.MORNING));
				mAfternoon.setChecked(DrugsData.getBoolean(cursor, DrugsData.DRUGS.AFTERNOON));
				mEvening.setChecked(DrugsData.getBoolean(cursor, DrugsData.DRUGS.EVENING));
				mDirections.setSelection(DrugsData.getInt(cursor, DrugsData.DRUGS.DIRECTIONS));
				mFromDate.setText(DrugsData.formatDate(getActivity(), DrugsData.getDate(cursor, DrugsData.DRUGS.FROM_DATE)));
				mDuration.setText(DrugsData.getString(cursor, DrugsData.DRUGS.DURATION));
				mDaysApart.setText(DrugsData.getString(cursor, DrugsData.DRUGS.DAYS_APART));
				mNotes.setText(DrugsData.getString(cursor, DrugsData.DRUGS.NOTES));
			}
		}
		
		return view;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.drugs_form, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.action_accept:
			ContentValues values = new ContentValues();
			values.put(DrugsData.DRUGS.DRUG, mDrug.getText().toString());
			values.put(DrugsData.DRUGS.DOSAGE, mDosage.getText().toString());
			values.put(DrugsData.DRUGS.QUANTITY, mQuantity.getText().toString());
			values.put(DrugsData.DRUGS.MORNING, mMorning.isChecked());
			values.put(DrugsData.DRUGS.AFTERNOON, mAfternoon.isChecked());
			values.put(DrugsData.DRUGS.EVENING, mEvening.isChecked());
			values.put(DrugsData.DRUGS.DIRECTIONS, mDirections.getSelectedItemPosition());
			values.put(DrugsData.DRUGS.DURATION, mDuration.getText().toString());
			values.put(DrugsData.DRUGS.DAYS_APART, mDaysApart.getText().toString());
			values.put(DrugsData.DRUGS.FROM_DATE, DrugsData.parseDate(getActivity(), mFromDate.getText().toString()));
			values.put(DrugsData.DRUGS.NOTES, mNotes.getText().toString());
			if(mDrugId == null)
				getActivity().getContentResolver().insert(DrugsData.DRUGS.URI, values);
			else
				getActivity().getContentResolver().update(
						Uri.withAppendedPath(DrugsData.DRUGS.URI, "" + mDrugId), 
						values, null, null);
			
			//getActivity().getSupportFragmentManager().popBackStack();
			getActivity().onBackPressed();
			return true;
		case R.id.action_cancel:
			if(mDrugId != null)
				getActivity().getContentResolver().delete(
						Uri.withAppendedPath(DrugsData.DRUGS.URI, "" + mDrugId),	null, null);
			getActivity().onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
}
