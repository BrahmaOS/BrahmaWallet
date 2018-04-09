package io.brahmaos.wallet.brahmawallet.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseFragment;
import io.brahmaos.wallet.brahmawallet.ui.market.MarketFragment;
import io.brahmaos.wallet.brahmawallet.ui.profile.ProfileFragment;
import io.brahmaos.wallet.brahmawallet.ui.wallet.WalletFragment;
import io.brahmaos.wallet.brahmawallet.view.HomeViewPager;


public class MainActivity extends AppCompatActivity {

    private HomeViewPager contentPager;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_wallet:
                    contentPager.setCurrentItem(0);
                    return true;
                case R.id.navigation_market:
                    contentPager.setCurrentItem(1);
                    return true;
                case R.id.navigation_me:
                    contentPager.setCurrentItem(2);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        contentPager = (HomeViewPager) findViewById(R.id.content_pager);
        contentPager.setLocked(true);
        contentPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                //mOnNavigationItemSelectedListener.setCurrentItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        final MainViewPagerAdapter adapter = new MainViewPagerAdapter(getSupportFragmentManager());
        contentPager.setAdapter(adapter);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    /**
     * Represent each page as a fragment that is persistently
     * kept in the fragment manager
     */
    private class MainViewPagerAdapter extends FragmentPagerAdapter {

        private List<BaseFragment> fragments = new ArrayList<>();

        MainViewPagerAdapter(FragmentManager fm) {
            super(fm);
            fragments.clear();
            fragments.add(WalletFragment.newInstance(R.layout.fragment_wallets, R.id.fragment_wallet_toolbar, R.string.title_wallets));
            fragments.add(MarketFragment.newInstance(R.layout.fragment_markets, R.id.fragment_market_toolbar, R.string.title_markets));
            fragments.add(ProfileFragment.newInstance(R.layout.fragment_profile, R.id.fragment_profile_toolbar, R.string.title_profile));
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }

}
