package my.example.drugsreminder;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

public class IntakesListFragment extends ListFragment 
									implements LoaderCallbacks<Cursor> {
	
	
	public static final int INTAKES_LIST_LOADER = 0;
	public static final String ARGS_BUNDLE = "args_bundle";

	IntakesCursorAdapter mAdapter;

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		setEmptyText(getString(R.string.empty_list));
		
		mAdapter = new IntakesCursorAdapter(getActivity(), null);
		setListAdapter(mAdapter);

		if(savedInstanceState != null) {
			getLoaderManager().restartLoader(INTAKES_LIST_LOADER, savedInstanceState.getBundle(ARGS_BUNDLE), this);
		} else {
			getLoaderManager().initLoader(INTAKES_LIST_LOADER, getArguments(), this);
		}

	}

	
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBundle(ARGS_BUNDLE, getArguments());
	}



	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Fragment fragment = new DrugsFormFragment();
		
		Cursor cursor = getActivity().getContentResolver()
				.query(Uri.withAppendedPath(DrugsData.INTAKES.URI,""+id), null, null, null, null);
		if(cursor != null) {
			if(cursor.moveToFirst()) {
				Bundle args = new Bundle();
				args.putLong(DrugsData.DRUGS._ID, DrugsData.getLong(cursor, DrugsData.INTAKES.DRUG_ID));
				fragment.setArguments(args);
				
				FragmentTransaction tr = getActivity().getSupportFragmentManager().beginTransaction();
				tr.replace(R.id.fragment_container, fragment);
				tr.addToBackStack(null);
				tr.commit();
			}
			cursor.close();
		}
		super.onListItemClick(l, v, position, id);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle args) {
		return new CursorLoader(getActivity(),DrugsData.INTAKES.URI,null,
				DrugsData.INTAKES.DATE + " = " + " ? " + " AND " +
				DrugsData.INTAKES.TIME + " = " + " ? ",
				new String[] { 
					"" + args.getLong(DrugsData.INTAKES.DATE),
					"" + args.getInt(DrugsData.INTAKES.TIME) 
				},null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor c) {
		
		mAdapter.swapCursor(c);
		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		
		mAdapter.swapCursor(null);
		
	}


	public class IntakesCursorAdapter extends CursorAdapter {

		public IntakesCursorAdapter(Context context, Cursor c) {
			super(context, c, true);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			//if(cursor == null) return;
			((TextView)view.findViewById(R.id.intake_drug)).setText(
					DrugsData.getString(cursor, DrugsData.INTAKES.DRUG));
			((TextView)view.findViewById(R.id.intake_qtty)).setText(
					DrugsData.getInt(cursor, DrugsData.INTAKES.QUANTITY) +
					"x" + DrugsData.getFloat(cursor, DrugsData.INTAKES.DOSAGE));
			((TextView)view.findViewById(R.id.intake_directions)).setText(
					context.getResources().getStringArray(R.array.directions)
						[DrugsData.getInt(cursor, DrugsData.INTAKES.DIRECTIONS)]);
			CheckBox checkBox = ((CheckBox)view.findViewById(R.id.intake_taken));
			checkBox.setTag(DrugsData.getLong(cursor, DrugsData.INTAKES._ID));
			checkBox.setChecked(DrugsData.getBoolean(cursor, DrugsData.INTAKES.TAKEN));
			
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			return LayoutInflater.from(context).inflate(R.layout.intakes_list_item, parent, false);
		}
		
	}

}
