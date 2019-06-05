package org.odk.share.views.ui.send;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;

import org.odk.share.R;
import org.odk.share.views.ui.common.ViewPagerAdapter;
import org.odk.share.views.ui.common.injectable.InjectableActivity;
import org.odk.share.views.ui.send.fragment.BlankFormsFragment;
import org.odk.share.views.ui.send.fragment.FilledFormsFragment;

import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;

public class SendFormsActivity extends InjectableActivity {

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