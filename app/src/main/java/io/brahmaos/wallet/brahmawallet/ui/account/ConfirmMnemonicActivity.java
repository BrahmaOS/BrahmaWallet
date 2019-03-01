package io.brahmaos.wallet.brahmawallet.ui.account;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.IntentParam;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.service.MainService;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.brahmawallet.view.MyAutoLineLayout;

public class ConfirmMnemonicActivity extends BaseActivity {

    private LinearLayout backupSuccessLayout;

    private TextView mTvConfirmErrorInfo;
    private List<String> mOrigMnemonicArray = new ArrayList<String>();
    private List<String> mConfirmedMnemonicArrayList = new ArrayList<String>();
    private List<String> mRemainMnemonicArrayList = new ArrayList<String>();
    private MyAutoLineLayout mConfirmedParentLayout;
    private MyAutoLineLayout mParentLinearLayout;

    @Override
    protected String tag() {
        return ConfirmMnemonicActivity.class.getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_mnemonic);
        showNavBackBtn();

        mTvConfirmErrorInfo = (TextView) findViewById(R.id.tv_confirm_error_info);
        mConfirmedParentLayout = (MyAutoLineLayout) findViewById(R.id.layout_confirmed_mnemonic);
        mParentLinearLayout = (MyAutoLineLayout) findViewById(R.id.layout_mnemonic_total);

        mOrigMnemonicArray = getIntent().getStringArrayListExtra(IntentParam.PARAM_MNEMONIC_CODE);
        if (mOrigMnemonicArray == null || mOrigMnemonicArray.size() == 0 || mOrigMnemonicArray.size() % 3 > 0) {
            finish();
            return;
        }

        backupSuccessLayout = findViewById(R.id.layout_backup_success);
        Button finishButton = findViewById(R.id.btn_finish);
        finishButton.setOnClickListener(v -> {
            Intent intent = this.getIntent();
            setResult(RESULT_OK, intent);
            finish();
        });

        mRemainMnemonicArrayList.addAll(mOrigMnemonicArray);
        if (mRemainMnemonicArrayList != null) {
            Collections.shuffle(mRemainMnemonicArrayList);
        }

        refreshView();
    }

    private void refreshView() {
        int remainSize = (null == mRemainMnemonicArrayList) ? 0 : mRemainMnemonicArrayList.size();
        int confirmedSize = (null == mConfirmedMnemonicArrayList) ? 0 : mConfirmedMnemonicArrayList.size();


        // redraw total remain mnemonics which wait to be confirmed.
        for (int i = 0; i < remainSize; i++) {
            String item = mRemainMnemonicArrayList.get(i);
            addRemainItem(item);
        }

        // redraw total confirmed mnemonics.
        for (int i = 0; i < confirmedSize; i++) {
            String item = mConfirmedMnemonicArrayList.get(i);
            addConfirmItem(item);
        }

    }

    private void addRemainItem(String item) {
        LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        Button child = (Button) LayoutInflater.from(this).inflate(R.layout.mnemonic_button, null);
        child.setText(item);
        child.setLayoutParams(itemParams);
        child.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mConfirmedMnemonicArrayList.add(item);
                mRemainMnemonicArrayList.remove(item);
                if (checkOrderOfMnemonics(mConfirmedMnemonicArrayList, mOrigMnemonicArray)) {
                    mTvConfirmErrorInfo.setVisibility(View.INVISIBLE);
                } else {
                    mTvConfirmErrorInfo.setVisibility(View.VISIBLE);
                }
                mParentLinearLayout.removeView(child);
                addConfirmItem(item);

                if (mConfirmedMnemonicArrayList.size() == mOrigMnemonicArray.size() &&
                        mTvConfirmErrorInfo.getVisibility() == View.INVISIBLE) {
                    backupSuccessLayout.setVisibility(View.VISIBLE);
                }
            }
        });
        mParentLinearLayout.addView(child, itemParams);
    }

    private void addConfirmItem(String item) {
        LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        Button child = (Button) LayoutInflater.from(this).inflate(R.layout.mnemonic_button, null);
        child.setText(item);
        child.setLayoutParams(itemParams);
        child.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRemainMnemonicArrayList.add(item);
                mConfirmedMnemonicArrayList.remove(item);
                if (checkOrderOfMnemonics(mConfirmedMnemonicArrayList, mOrigMnemonicArray)) {
                    mTvConfirmErrorInfo.setVisibility(View.INVISIBLE);
                } else {
                    mTvConfirmErrorInfo.setVisibility(View.VISIBLE);
                }
                mConfirmedParentLayout.removeView(child);
                addRemainItem(item);

                backupSuccessLayout.setVisibility(View.GONE);
            }
        });
        mConfirmedParentLayout.addView(child);
    }

    private boolean checkOrderOfMnemonics(List<String> confirmed, List<String> original) {
        if (null == confirmed || confirmed.size() <= 0) {
            return true;
        }
        if (null == original || original.size() <= 0 ||
                confirmed.size() > original.size()) {
            return false;
        }
        for (int i = 0; i < confirmed.size(); i++) {
            if (!confirmed.get(i).equals(original.get(i))) {
                return false;
            }
        }
        return true;
    }
}
