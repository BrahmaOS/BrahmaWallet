package io.brahmaos.wallet.brahmawallet.view;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;

import io.brahmaos.wallet.brahmawallet.R;

/**
 *  custom progress dialog
 */
public class CustomProgressDialog extends ProgressDialog {

    private String title;
    private TextView tvTitle;

    public CustomProgressDialog(Context context) {
        super(context);
    }

    public CustomProgressDialog(Context context, int theme) {
        super(context, theme);
    }

    public CustomProgressDialog(Context context, int theme, String title) {
        super(context, theme);
        this.title = title;
    }

    public CustomProgressDialog(Context context, int theme, int titleResId) {
        super(context, theme);
        this.title = getContext().getString(titleResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        setContentView(R.layout.custom_progress_dialog);
        if (getWindow() != null) {
            WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
            if (layoutParams != null) {
                layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
                layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
                getWindow().setAttributes(layoutParams);
            }
        }

        if (title != null) {
            tvTitle =  findViewById(R.id.title_tv);
            tvTitle.setText(title);
        }
    }

    public void setProgressMessage(String title) {
        if (title != null && tvTitle != null) {
            tvTitle.setText(title);
        }
    }
}
