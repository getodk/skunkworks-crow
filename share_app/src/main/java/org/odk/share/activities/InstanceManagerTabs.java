package org.odk.share.activities;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.odk.share.R;
import org.odk.share.fragments.ReceivedInstancesFragment;
import org.odk.share.fragments.ReviewedInstancesFragment;
import org.odk.share.fragments.SentInstancesFragment;
import org.odk.share.fragments.StatisticsFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static org.odk.share.activities.MainActivity.FORM_ID;

public class InstanceManagerTabs extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tabs)
    TabLayout tabLayout;
    @BindView(R.id.viewpager)
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instance_manager_tabs);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        String formId = getIntent().getStringExtra(FORM_ID);

        if (formId == null) {
            finish();
        }

        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        setupTab();
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                View view = tab.getCustomView();
                TextView title = view.findViewById(R.id.tvTabTitle);
                title.setVisibility(View.VISIBLE);
                ImageView imageView = view.findViewById(R.id.ivTabIcon);
                DrawableCompat.setTint(imageView.getDrawable(), ContextCompat.getColor(InstanceManagerTabs.this,
                        R.color.colorTabActive));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                View view = tab.getCustomView();
                TextView title = view.findViewById(R.id.tvTabTitle);
                title.setVisibility(View.GONE);
                ImageView imageView = view.findViewById(R.id.ivTabIcon);
                DrawableCompat.setTint(imageView.getDrawable(), ContextCompat.getColor(InstanceManagerTabs.this,
                        R.color.colorTabInactive));
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void setupTab() {
        setupTabIcon(getString(R.string.statistics), 0, R.drawable.ic_stats, true);
        setupTabIcon(getString(R.string.sent), 1, R.drawable.ic_upload, false);
        setupTabIcon(getString(R.string.received), 2, R.drawable.ic_download, false);
        setupTabIcon(getString(R.string.reviewed), 3, R.drawable.ic_assignment, false);
    }

    private void setupTabIcon(String title, int position, int resId, boolean visible) {
        View view = LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        TextView titleView = view.findViewById(R.id.tvTabTitle);
        ImageView iconView = view.findViewById(R.id.ivTabIcon);

        titleView.setText(title);
        iconView.setImageResource(resId);

        if (visible) {
            titleView.setVisibility(View.VISIBLE);
            DrawableCompat.setTint(iconView.getDrawable(), ContextCompat.getColor(this,
                    R.color.colorTabActive));
        } else {
            titleView.setVisibility(View.GONE);
            DrawableCompat.setTint(iconView.getDrawable(), ContextCompat.getColor(this,
                    R.color.colorTabInactive));
        }

        tabLayout.getTabAt(position).setCustomView(view);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new StatisticsFragment(), getString(R.string.statistics));
        adapter.addFrag(new SentInstancesFragment(), getString(R.string.sent));
        adapter.addFrag(new ReceivedInstancesFragment(), getString(R.string.received));
        adapter.addFrag(new ReviewedInstancesFragment(), getString(R.string.reviewed));
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> fragmentList = new ArrayList<>();
        private final List<String> fragmentTitleList = new ArrayList<>();

        ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            fragmentList.add(fragment);
            fragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitleList.get(position);
        }
    }

}