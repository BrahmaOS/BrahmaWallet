package io.brahmaos.wallet.brahmawallet.ui.account;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import butterknife.BindView;
import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.util.BLog;

public class ImportAccountActivity extends BaseActivity {

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    @BindView(R.id.sliding_tabs)
    TabLayout mTabLayout;
    @BindView(R.id.viewpager)
    ViewPager mViewPager;

    @Override
    protected String tag() {
        return ImportAccountActivity.class.getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_account);
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
                AlertDialog dialogTip = new AlertDialog.Builder(ImportAccountActivity.this)
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
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        final int PAGE_COUNT = 2;
        private String tabTitles[] = new String[]{"Official", "Private Key"};
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
