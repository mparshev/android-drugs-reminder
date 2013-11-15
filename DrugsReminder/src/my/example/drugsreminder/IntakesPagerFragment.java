package my.example.drugsreminder;

import java.util.Calendar;
import java.util.Date;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

public class IntakesPagerFragment extends Fragment {
	
	ViewPager mPager;
	IntakesPagerAdapter mPagerAdapter;
	
	String mTitle;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.intakes_pager, container, false);
		
		setHasOptionsMenu(true);
		
		mPager = (ViewPager)view.findViewById(R.id.pager);
		mPagerAdapter = new IntakesPagerAdapter(getActivity().getSupportFragmentManager());
		mPager.setAdapter(mPagerAdapter);
		mTitle = "" + getActivity().getTitle();
		mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				getActivity().setTitle(mPagerAdapter.getPageTitle(position));
				super.onPageSelected(position);
			}
			
		});
		mPager.setCurrentItem(mPagerAdapter.getTodayPage());
		return view;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.action_new, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}
	
	
	@Override
	public void onPause() {
		getActivity().setTitle(mTitle);
		super.onPause();
	}


	public class IntakesPagerAdapter extends FragmentStatePagerAdapter {
		
		public static final int DAYS_COUNT = 33;
		public static final int TODAY_PAGE = 11;

		private int[] timeResIds = new int[] { R.string.morning, R.string.afternoon, R.string.evening };
		
		Date mToday;

		public IntakesPagerAdapter(FragmentManager fm) {
			super(fm);
			mToday = new Date();
		}

		@Override
		public Fragment getItem(int page) {
			Fragment fragment = new IntakesListFragment();
			Bundle args = new Bundle();
			args.putLong(DrugsData.INTAKES.DATE, getPageDate(page).getTime());
			args.putInt(DrugsData.INTAKES.TIME, page % 3);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return DAYS_COUNT * 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return DateFormat.getDateFormat(getActivity()).format(getPageDate(position)) + 
					" " + getString(timeResIds[position % 3]);
		}
		
		public Date getPageDate(int position) {
			return DrugsData.trimDate(DrugsData.addDays(mToday, (position/3) - TODAY_PAGE));
		}
		
		public int getTodayPage() {
			// TODO Calculate page based on current system time
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(mToday);
			Date currentTime = DrugsData.parseTime(getActivity(), 
					DrugsData.formatTime(getActivity(), 
							calendar.get(Calendar.HOUR_OF_DAY), 
							calendar.get(Calendar.MINUTE)));
			int time = 0;
			if(currentTime.after(DrugsData.getTimePref(getActivity(), DrugsData.PREFS.AFTERNOON))) 
				time = 1;
			if(currentTime.after(DrugsData.getTimePref(getActivity(), DrugsData.PREFS.EVENING))) 
				time = 2;
						
			return TODAY_PAGE * 3 + time;
		}

	}

}
