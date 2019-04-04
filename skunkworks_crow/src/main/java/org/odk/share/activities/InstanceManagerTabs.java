package org.odk.share.activities;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;

import org.odk.share.R;
import org.odk.share.adapters.ViewPagerAdapter;
import org.odk.share.fragments.ReceivedInstancesFragment;
import org.odk.share.fragments.ReviewedInstancesFragment;
import org.odk.share.fragments.SentInstancesFragment;
import org.odk.share.fragments.StatisticsFragment;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;

import static org.odk.share.activities.MainActivity.FORM_ID;

public class InstanceManagerTabs extends InjectableActivity implements TabLayout.OnTabSelectedListener {

    private final Object[][] tabs = {
            {R.string.statistics, R.drawable.ic_stats, new StatisticsFragment()},
            {R.string.sent, R.drawable.ic_upload, new SentInstancesFragment()},
            {R.string.received, R.drawable.ic_download, new ReceivedInstancesFragment()},
            {R.string.reviewed, R.drawable.ic_assignment, new ReviewedInstancesFragment()}
    };

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

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String formId = getIntent().getStringExtra(FORM_ID);

        if (formId == null) {
            finish();
        }

        setupViewPager();
        setupTabs();
    }

    private void setupTabs() {
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(this);

        for (int i = 0; i < tabs.length; i++) {
            setupTabIcon(((int) tabs[i][0]), ((int) tabs[i][1]), i, i == 0);
        }
    }

    private void setupTabIcon(int title, int resId, int position, boolean visible) {
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

    private void setupViewPager() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        for (Object[] tab : tabs) {
            adapter.addFrag(((Fragment) tab[2]), getString((int) tab[0]));
        }

        viewPager.setAdapter(adapter);

        // Prevent tabs from being destroyed on swipe. This makes the swipe across tabs smoother
        viewPager.setOffscreenPageLimit(tabs.length - 1);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        View view = tab.getCustomView();
        TextView title = view.findViewById(R.id.tvTabTitle);
        title.setVisibility(View.VISIBLE);
        ImageView imageView = view.findViewById(R.id.ivTabIcon);
        DrawableCompat.setTint(imageView.getDrawable(), ContextCompat.getColor(this, R.color.colorTabActive));
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        View view = tab.getCustomView();
        TextView title = view.findViewById(R.id.tvTabTitle);
        title.setVisibility(View.GONE);
        ImageView imageView = view.findViewById(R.id.ivTabIcon);
        DrawableCompat.setTint(imageView.getDrawable(), ContextCompat.getColor(this, R.color.colorTabInactive));
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }
}