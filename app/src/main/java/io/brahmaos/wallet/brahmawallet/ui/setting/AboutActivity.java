package io.brahmaos.wallet.brahmawallet.ui.setting;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import io.brahmaos.wallet.brahmawallet.BuildConfig;
import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;

public class AboutActivity extends BaseActivity {

    @Override
    protected String tag() {
        return AboutActivity.class.getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        showNavBackBtn();
        TextView tvCurrentVersion = findViewById(R.id.tv_version_name);
        tvCurrentVersion.setText(BuildConfig.VERSION_NAME);
    }

}
