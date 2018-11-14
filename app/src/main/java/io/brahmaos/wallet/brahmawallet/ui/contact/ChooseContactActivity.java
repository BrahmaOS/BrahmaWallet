package io.brahmaos.wallet.brahmawallet.ui.contact;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConst;
import io.brahmaos.wallet.brahmawallet.common.IntentParam;
import io.brahmaos.wallet.brahmawallet.common.ReqCode;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.db.entity.ContactEntity;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.brahmawallet.viewmodel.ContactViewModel;
import me.yokeyword.indexablerv.IndexableAdapter;
import me.yokeyword.indexablerv.IndexableHeaderAdapter;
import me.yokeyword.indexablerv.IndexableLayout;
import me.yokeyword.indexablerv.SimpleHeaderAdapter;

public class ChooseContactActivity extends BaseActivity {
    private ContactAdapter mAdapter;
    private ContactViewModel mViewModel;
    private ContactsHeaderAdapter mHeaderAdapter;
    private int accountType;
    IndexableLayout indexableLayout;

    @Override
    protected String tag() {
        return ChooseContactActivity.class.getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        showNavBackBtn();
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(getString(R.string.title_choose_contact));
        }
        mViewModel = ViewModelProviders.of(this).get(ContactViewModel.class);

        indexableLayout = findViewById(R.id.indexableLayout);
        indexableLayout.setLayoutManager(new LinearLayoutManager(this));
        accountType = getIntent().getIntExtra(IntentParam.PARAM_ACCOUNT_TYPE, BrahmaConst.ETH_ACCOUNT_TYPE);
        // setAdapter
        mAdapter = new ContactAdapter(this);
        indexableLayout.setAdapter(mAdapter);
        // init Datas
        initData();

        // set Listener
        mAdapter.setOnItemContentClickListener((v, originalPosition, currentPosition, entity) -> {
            String backAddress = "";
            if (accountType == BrahmaConst.ETH_ACCOUNT_TYPE) {
                if (entity.getAddress() == null || entity.getAddress().length() < 1) {
                    showLongToast(R.string.tip_no_ethereum_address);
                    return;
                }
                backAddress = entity.getAddress();
            }

            if (accountType == BrahmaConst.BTC_ACCOUNT_TYPE) {
                if (entity.getBtcAddress() == null || entity.getBtcAddress().length() < 1) {
                    showLongToast(R.string.tip_no_bitcoin_address);
                    return;
                }
                backAddress = entity.getBtcAddress();
            }
            Intent intent = ChooseContactActivity.this.getIntent();
            intent.putExtra(IntentParam.PARAM_CONTACT_ADDRESS, backAddress);
            setResult(RESULT_OK, intent);
            finish();
        });

        mHeaderAdapter.setOnItemHeaderClickListener((v, currentPosition, entity) -> {
            String backAddress = "";
            if (accountType == BrahmaConst.ETH_ACCOUNT_TYPE) {
                if (entity.getType() != BrahmaConst.ETH_ACCOUNT_TYPE) {
                    showLongToast(R.string.tip_no_ethereum_address);
                    return;
                }
                backAddress = entity.getAddress();
            }

            if (accountType == BrahmaConst.BTC_ACCOUNT_TYPE) {
                if (entity.getType() != BrahmaConst.BTC_ACCOUNT_TYPE) {
                    showLongToast(R.string.tip_no_bitcoin_address);
                    return;
                }
                backAddress = entity.getAddress();
            }
            Intent intent = ChooseContactActivity.this.getIntent();
            intent.putExtra(IntentParam.PARAM_CONTACT_ADDRESS, backAddress);
            setResult(RESULT_OK, intent);
            finish();
        });
    }

    private void initData() {
        mViewModel.getContacts().observe(this, contactEntities -> {
            if (contactEntities == null) {
                mAdapter.setDatas(new ArrayList<>());
            } else {
                mAdapter.setDatas(contactEntities);
            }
            indexableLayout.setOverlayStyle_MaterialDesign(Color.RED);
            indexableLayout.setCompareMode(IndexableLayout.MODE_ALL_LETTERS);
        });
        mViewModel.getAccounts().observe(this, accountEntities -> {
            if (accountEntities != null) {
                mHeaderAdapter = new ContactsHeaderAdapter(this, "☆", getString(R.string.title_my_accounts), accountEntities);
                indexableLayout.addHeaderAdapter(mHeaderAdapter);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add) {
            Intent intent = new Intent(this, AddContactActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
