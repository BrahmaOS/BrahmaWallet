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
import io.brahmaos.wallet.brahmawallet.common.IntentParam;
import io.brahmaos.wallet.brahmawallet.common.ReqCode;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.db.entity.ContactEntity;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.brahmawallet.viewmodel.ContactViewModel;
import me.yokeyword.indexablerv.IndexableAdapter;
import me.yokeyword.indexablerv.IndexableLayout;
import me.yokeyword.indexablerv.SimpleHeaderAdapter;

public class ChooseContactActivity extends BaseActivity {
    private ContactAdapter mAdapter;
    private ContactViewModel mViewModel;
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

        // setAdapter
        mAdapter = new ContactAdapter(this);
        indexableLayout.setAdapter(mAdapter);
        // init Datas
        initData();

        // set Listener
        mAdapter.setOnItemContentClickListener((v, originalPosition, currentPosition, entity) -> {
            Intent intent = ChooseContactActivity.this.getIntent();
            intent.putExtra(IntentParam.PARAM_CONTACT_ADDRESS, entity.getAddress());
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
                indexableLayout.addHeaderAdapter(new SimpleHeaderAdapter<>(mAdapter, "☆", getString(R.string.title_my_accounts), initWalletAccount(accountEntities)));
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

    private List<ContactEntity> initWalletAccount(List<AccountEntity> accounts) {
        List<ContactEntity> list = new ArrayList<>();
        for (AccountEntity accountEntity : accounts) {
            list.add(new ContactEntity(0, "", accountEntity.getName(), accountEntity.getAddress(), "", ""));
        }
        return list;
    }
}
