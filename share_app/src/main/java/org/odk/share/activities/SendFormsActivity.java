package org.odk.share.activities;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import org.odk.share.R;
import org.odk.share.adapters.ViewPagerAdapter;
import org.odk.share.fragments.BlankFormsFragment;
import org.odk.share.fragments.FilledFormsFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SendFormsActivity extends AppCompatActivity {

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

        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new FilledFormsFragment(), getString(R.string.filled_form));
        adapter.addFrag(new BlankFormsFragment(), getString(R.string.blank_form));
        viewPager.setAdapter(adapter);
    }
}