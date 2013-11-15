package my.example.drugsreminder;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ListView;

public class DrugsListFragment extends ListFragment 
								implements LoaderCallbacks<Cursor> {
	
	public static final int DRUGS_LIST_LOADER = 0;

	SimpleCursorAdapter mAdapter;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		setHasOptionsMenu(true);

		setEmptyText(getString(R.string.empty_list));
		
		mAdapter = new SimpleCursorAdapter(getActivity(), 
				android.R.layout.simple_list_item_2, 
				null, 
				new String[] { DrugsData.DRUGS.DRUG, DrugsData.DRUGS.DOSAGE }, 
				new int[] { android.R.id.text1, android.R.id.text2 }, 0);
		
		setListAdapter(mAdapter);

		getLoaderManager().initLoader(DRUGS_LIST_LOADER, null, this);
		
	}

	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.action_new, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}


	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Fragment fragment = new DrugsFormFragment();
		
		Bundle args = new Bundle();
		args.putLong(DrugsData.DRUGS._ID, id);
		fragment.setArguments(args);

		FragmentTransaction tr = getActivity().getSupportFragmentManager().beginTransaction();
		tr.replace(R.id.fragment_container, fragment);
		tr.addToBackStack(null);
		tr.commit();
		super.onListItemClick(l, v, position, id);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		
		return new CursorLoader(getActivity(),DrugsData.DRUGS.URI,null,null,null,null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor c) {
		
		mAdapter.swapCursor(c);
		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		
		mAdapter.swapCursor(null);
		
	}
	
	

}
