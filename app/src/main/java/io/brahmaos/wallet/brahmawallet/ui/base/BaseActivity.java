package io.brahmaos.wallet.brahmawallet.ui.base;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Locale;

import butterknife.ButterKnife;
import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConfig;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConst;
import io.brahmaos.wallet.brahmawallet.statistic.utils.StatisticEventAgent;
import io.brahmaos.wallet.util.BLog;
import io.brahmaos.wallet.util.PermissionUtil;


/**
 * Activity Base
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected abstract String tag();

    @Override
    protected void attachBaseContext(Context newBase) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Configuration config = newBase.getResources().getConfiguration();

            Locale newLocale = Locale.ENGLISH;
            if (BrahmaConfig.getInstance().getLanguageLocale()
                    .equals(BrahmaConst.LANGUAGE_CHINESE)) {
                newLocale = Locale.CHINESE;
            }
            config.setLocale(newLocale);
            newBase = newBase.createConfigurationContext(config);
        }
        super.attachBaseContext(newBase);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        BLog.d(tag(), "onCreate");
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        StatisticEventAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        StatisticEventAgent.onPause(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        ButterKnife.bind(this);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        BLog.d(tag(), "onDestroy");
        super.onDestroy();
    }

    protected void showShortToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    protected void showShortToast(int res) {
        showShortToast(getString(res));
    }

    protected void showLongToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    protected void showLongToast(int res) {
        showLongToast(getString(res));
    }

    // Request camera permission
    public void requestCameraScanPermission() {
        PermissionUtil.requestMultiPermissions(this, PermissionUtil.CAMERA_PERMISSIONS, PermissionUtil.CODE_CAMERA_SCAN);
    }

    // Request write external storage
    public void requestExternalStorage() {
        PermissionUtil.requestMultiPermissions(this, PermissionUtil.EXTERNAL_STORAGE_PERMISSIONS, PermissionUtil.CODE_EXTERNAL_STORAGE);
    }

    public void handleCameraScanPermission() {}

    public void handleExternalStoragePermission() {}

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PermissionUtil.CODE_CAMERA_SCAN) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                handleCameraScanPermission();
            } else {
                PermissionUtil.openSettingActivity(this, getString(R.string.tip_camera_permission));
            }
        } else if (requestCode == PermissionUtil.CODE_EXTERNAL_STORAGE) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                handleExternalStoragePermission();
            } else {
                PermissionUtil.openSettingActivity(this, getString(R.string.tip_external_storage_permission));
            }
        }
    }

    // show navigation backup action icon
    protected void showNavBackBtn() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            ActionBar ab = getSupportActionBar();
            if (ab != null) {
                ab.setDisplayHomeAsUpEnabled(true);
                ab.setDisplayShowHomeEnabled(true);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * set toolbar title
     */
    protected void setToolbarTitle(String title) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(title);
        }
    }
}
