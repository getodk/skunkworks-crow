package org.odk.share.views.ui.about;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.odk.share.R;
import org.odk.share.views.listeners.OnItemClickListener;

import butterknife.BindView;
import butterknife.ButterKnife;

import static org.odk.share.views.ui.about.WebViewActivity.OPEN_URL;
import static org.odk.share.views.ui.about.WebViewActivity.TITLE;

public class AboutActivity extends AppCompatActivity implements OnItemClickListener {

    private static final String LICENSES_HTML_PATH = "file:///android_asset/open_source_licenses.html";
    private static final String USER_GUIDE_HTML_PATH = "file:///android_asset/user_guide.html";
    private static final String SKUNKWORKS_CROW_README_URL = "https://github.com/opendatakit/skunkworks-crow/blob/master/README.md";

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

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(llm);
        adapter = new AboutAdapter(this, this);
        adapter.addItem(new AboutItem(R.string.app_information, R.drawable.ic_stars));
        adapter.addItem(new AboutItem(R.string.open_source_licenses, R.drawable.ic_stars));
        adapter.addItem(new AboutItem(R.string.user_guide, R.drawable.ic_stars));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(View view, int position) {
        if (position == 0) {

            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();

            builder.setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary));
            builder.addDefaultShareMenuItem();

            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(this, Uri.parse(SKUNKWORKS_CROW_README_URL));
        } else if (position == 1) {
            Intent intent = new Intent(this, WebViewActivity.class);
            intent.putExtra(OPEN_URL, LICENSES_HTML_PATH);
            intent.putExtra(TITLE, getString(R.string.open_source_licenses));
            startActivity(intent);
        } else if (position == 2)  {
            Intent intent = new Intent(this, WebViewActivity.class);
            intent.putExtra(OPEN_URL, USER_GUIDE_HTML_PATH);
            intent.putExtra(TITLE, getString(R.string.user_guide));
            startActivity(intent);
        }
    }
}
