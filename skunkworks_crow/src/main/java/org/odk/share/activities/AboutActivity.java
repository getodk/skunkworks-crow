package org.odk.share.activities;

import android.content.Intent;
import android.os.Bundle;

import android.view.View;

import org.odk.share.R;
import org.odk.share.adapters.AboutAdapter;
import org.odk.share.listeners.OnItemClickListener;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

import static org.odk.share.activities.WebViewActivity.OPEN_URL;

public class AboutActivity extends AppCompatActivity implements OnItemClickListener {

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

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(llm);
        int[][] listItems = {{R.string.open_source_licenses, R.drawable.ic_stars}};
        adapter = new AboutAdapter(this, listItems, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(View view, int position) {
        if (position == 0) {
            Intent intent = new Intent(this, WebViewActivity.class);
            intent.putExtra(OPEN_URL, LICENSES_HTML_PATH);
            startActivity(intent);
        }
    }
}
