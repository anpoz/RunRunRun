package com.playcode.runrunrun.activity;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.widget.TextView;

import com.playcode.runrunrun.R;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        initView();
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(R.string.about);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        TextView dev1 = (TextView) findViewById(R.id.devBlog1);
        TextView dev2 = (TextView) findViewById(R.id.devBlog2);
        TextView repo = (TextView) findViewById(R.id.repo);
        TextView version = (TextView) findViewById(R.id.tvVersion);
        String webStr1 = "<a href='http://weibo.com/anpoz'>@anpoz_stereo</a>";
        String webStr2 = "<a href='http://weibo.com/u/1847339335'>@昵称什么的最讨厌了-</a>";
        String webStr3 = "<a href='http://github.com/anpoz/RunRunRun'>anpoz</a>";

        assert dev1 != null;
        dev1.setText(Html.fromHtml(webStr1));
        assert dev2 != null;
        dev2.setText(Html.fromHtml(webStr2));
        assert repo != null;
        repo.setText(Html.fromHtml(webStr3));

        try {
            assert version != null;
            version.setText(getString(R.string.version) + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            version.setText(R.string.app_version);
        }
    }
}
