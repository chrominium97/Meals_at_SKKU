package me.u67f3.mealsskku;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private final String PREF_LAST_SELECTED_PAGE = "last_selected_page";

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setCollapsible(false);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit()
                        .putInt(PREF_LAST_SELECTED_PAGE, position).apply();
            }
        });
        mViewPager.setCurrentItem(
                PreferenceManager.getDefaultSharedPreferences(
                        getBaseContext()).getInt(PREF_LAST_SELECTED_PAGE, 0),
                false
        );

        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabLayout.setupWithViewPager(mViewPager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = getString(R.string.switched_to);
                switch (mSectionsPagerAdapter.switchCampus()) {
                    case SectionsPagerAdapter.YULJEON:
                        text = text.replace("%s", getString(R.string.campus_yuljeon));
                        break;
                    case SectionsPagerAdapter.MYEONGRYUN:
                        text = text.replace("%s", getString(R.string.campus_myeongryun));
                        break;
                }
                tabLayout.setupWithViewPager(mViewPager);
                Snackbar.make(view, text, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        final private String PREF_SELECTED_CAMPUS = "selected_campus";

        final static int MYEONGRYUN = 0;
        final static int YULJEON = 1;

        int campus;

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            campus = PreferenceManager.getDefaultSharedPreferences(getBaseContext())
                    .getInt(PREF_SELECTED_CAMPUS, YULJEON);
        }

        int switchCampus() {
            if (this.campus == YULJEON) {
                this.campus = MYEONGRYUN;
            } else {
                this.campus = YULJEON;
            }
            PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit()
                    .putInt(PREF_SELECTED_CAMPUS, this.campus).apply();

            this.notifyDataSetChanged();
            return this.campus;
        }



        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a MealViewerFragment (defined as a static inner class below).
            if (this.campus == YULJEON) {
                position += 6;
            }
            return MealViewerFragment.newInstance(position);
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            switch (this.campus) {
                case YULJEON:
                    return 3;
                case MYEONGRYUN:
                    return 6;
            }
            return 0;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String[] cafeterias = getResources().getStringArray(R.array.cafeterias);
            if (this.campus == YULJEON) {
                position += 6;
            }
            return cafeterias[position];
        }
    }
}
