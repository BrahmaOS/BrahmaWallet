package io.brahmaos.wallet.brahmawallet.ui.account;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import butterknife.BindView;
import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.ReqCode;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.brahmawallet.ui.common.barcode.CaptureActivity;
import io.brahmaos.wallet.brahmawallet.ui.common.barcode.Intents;
import io.brahmaos.wallet.util.BLog;

public class ImportEthereumAccountActivity extends BaseActivity {

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    @BindView(R.id.sliding_tabs)
    TabLayout mTabLayout;
    @BindView(R.id.viewpager)
    ViewPager mViewPager;

    @Override
    protected String tag() {
        return ImportEthereumAccountActivity.class.getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_ethereum_account);
        showNavBackBtn();
        initView();
    }

    private void initView() {
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), this);

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.viewpager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mTabLayout = findViewById(R.id.sliding_tabs);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.setTabMode(TabLayout.MODE_FIXED);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            boolean cancel = true;
            if (mViewPager.getCurrentItem() == 0) {
                SectionsPagerAdapter adapter = (SectionsPagerAdapter) mViewPager.getAdapter();
                if (adapter != null) {
                    ImportOfficialFragment fragment = (ImportOfficialFragment) adapter.getCurrentFragment();
                    View view = fragment.getView();
                    if (view != null) {
                        EditText etKeystore =  view.findViewById(R.id.et_official_json);
                        cancel = TextUtils.isEmpty(etKeystore.getText().toString().trim());
                    }
                }
            } else if (mViewPager.getCurrentItem() == 1) {
                SectionsPagerAdapter adapter = (SectionsPagerAdapter) mViewPager.getAdapter();
                if (adapter != null) {
                    ImportMnemonicsFragment fragment = (ImportMnemonicsFragment) adapter.getCurrentFragment();
                    View view = fragment.getView();
                    if (view != null) {
                        EditText etPrivateKey =  view.findViewById(R.id.et_mnemonic);
                        cancel = TextUtils.isEmpty(etPrivateKey.getText().toString().trim());
                    }
                }
            } else if (mViewPager.getCurrentItem() == 2) {
                SectionsPagerAdapter adapter = (SectionsPagerAdapter) mViewPager.getAdapter();
                if (adapter != null) {
                    ImportPrivateKeyFragment fragment = (ImportPrivateKeyFragment) adapter.getCurrentFragment();
                    View view = fragment.getView();
                    if (view != null) {
                        EditText etPrivateKey =  view.findViewById(R.id.et_private_key);
                        cancel = TextUtils.isEmpty(etPrivateKey.getText().toString().trim());
                    }
                }
            }
            if (cancel) {
                finish();
            } else {
                // dialog show tip
                AlertDialog dialogTip = new AlertDialog.Builder(ImportEthereumAccountActivity.this)
                        .setMessage(R.string.dialog_title_quit_import)
                        .setPositiveButton(R.string.quit, (dialog, which) -> {
                            dialog.cancel();
                            finish();
                        })
                        .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel())
                        .create();
                dialogTip.show();
            }
            return true;
        }  else if (id == R.id.menu_scan) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                requestCameraScanPermission();
            } else {
                scanAddressCode();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_scan, menu);
        return true;
    }

    private void scanAddressCode() {
        Intent intent = new Intent(this, CaptureActivity.class);
        intent.putExtra(Intents.Scan.PROMPT_MESSAGE, "");
        startActivityForResult(intent, ReqCode.SCAN_QR_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        BLog.d(tag(), "requestCode: " + requestCode + "  ;resultCode" + resultCode);
        if (requestCode == ReqCode.SCAN_QR_CODE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    String qrCode = data.getStringExtra(Intents.Scan.RESULT);
                    if (qrCode != null && qrCode.length() > 0) {
                        BLog.d(tag(), "the content is: " + qrCode);
                        SectionsPagerAdapter adapter = (SectionsPagerAdapter) mViewPager.getAdapter();
                        if (mViewPager.getCurrentItem() == 0) {
                            if (adapter != null) {
                                ImportOfficialFragment fragment = (ImportOfficialFragment) adapter.getCurrentFragment();
                                View view = fragment.getView();
                                if (view != null) {
                                    EditText etKeystore =  view.findViewById(R.id.et_official_json);
                                    etKeystore.setText(qrCode);
                                }
                            }
                        } else if (mViewPager.getCurrentItem() == 1) {
                            if (adapter != null) {
                                ImportMnemonicsFragment fragment = (ImportMnemonicsFragment) adapter.getCurrentFragment();
                                View view = fragment.getView();
                                if (view != null) {
                                    EditText etMnemonic =  view.findViewById(R.id.et_mnemonic);
                                    etMnemonic.setText(qrCode);
                                }
                            }
                        } else if (mViewPager.getCurrentItem() == 2) {
                            if (adapter != null) {
                                ImportPrivateKeyFragment fragment = (ImportPrivateKeyFragment) adapter.getCurrentFragment();
                                View view = fragment.getView();
                                if (view != null) {
                                    EditText etPrivateKey =  view.findViewById(R.id.et_private_key);
                                    etPrivateKey.setText(qrCode);
                                }
                            }
                        }
                    } else {
                        showLongToast(R.string.tip_scan_code_failed);
                    }
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void handleCameraScanPermission() {
        scanAddressCode();
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        final int PAGE_COUNT = 3;
        private String tabTitles[] = new String[]{getString(R.string.title_tab_import_keystore),
                getString(R.string.title_tab_import_mnemonic),
                getString(R.string.title_tab_import_private_key)};
        private Fragment mCurrentFragment;
        private Context context;

        SectionsPagerAdapter(FragmentManager fm, Context context) {
            super(fm);
            this.context = context;
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            if (position == 0) {
                return ImportOfficialFragment.newInstance(position + 1);
            } else if (position == 1) {
                return ImportMnemonicsFragment.newInstance(position + 1);
            } else {
                return ImportPrivateKeyFragment.newInstance(position + 1);
            }
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            mCurrentFragment = (Fragment) object;
            super.setPrimaryItem(container, position, object);
        }


        Fragment getCurrentFragment() {
            return mCurrentFragment;
        }
    }
}
