package org.odk.share.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import org.odk.share.R;
import org.odk.share.adapters.AboutAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import static org.odk.share.activities.WebViewActivity.OPEN_URL;

public class AboutActivity extends AppCompatActivity {

    private static final String LICENSES_HTML_PATH = "file:///android_asset/open_source_licenses.html";

    @BindView(R.id.recyclerview)
    RecyclerView recyclerView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private AboutAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);

        setTitle(getString(R.string.about));
        setSupportActionBar(toolbar);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
        int[][] listItems = {{R.string.open_source_licenses, R.drawable.ic_stars}};
        adapter = new AboutAdapter(this, listItems, this::onItemClick);
        recyclerView.setAdapter(adapter);

    }

    private void onItemClick(View view, int position) {
        Timber.d("Position " + position);
        if (position == 0) {
            Intent intent = new Intent(this, WebViewActivity.class);
            intent.putExtra(OPEN_URL, LICENSES_HTML_PATH);
            startActivity(intent);
        }
    }
}
