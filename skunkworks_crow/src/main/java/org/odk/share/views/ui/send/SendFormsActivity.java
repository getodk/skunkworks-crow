package org.odk.share.views.ui.send;

import android.os.Bundle;
import android.view.View;

import com.google.android.material.tabs.TabLayout;

import org.odk.share.R;
import org.odk.share.views.ui.common.ViewPagerAdapter;
import org.odk.share.views.ui.common.injectable.InjectableActivity;
import org.odk.share.views.ui.send.fragment.BlankFormsFragment;
import org.odk.share.views.ui.send.fragment.FilledFormsFragment;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
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

    ViewPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instance_manager_tabs);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition(), true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

                View view = adapter.getItem(tab.getPosition()).getView();
                if (view != null) {
                    RecyclerView recyclerView = view.findViewById(R.id.recyclerview);
                    recyclerView.smoothScrollToPosition(0);
                }
            }
        });
    }

    private void setupViewPager(ViewPager viewPager) {
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new FilledFormsFragment(), getString(R.string.filled_form));
        adapter.addFrag(new BlankFormsFragment(), getString(R.string.blank_form));
        viewPager.setAdapter(adapter);
    }
}