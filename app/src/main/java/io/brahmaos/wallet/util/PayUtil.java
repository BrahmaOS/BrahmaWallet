package io.brahmaos.wallet.util;

import android.content.Context;
import android.widget.TextView;

import io.brahmaos.wallet.brahmawallet.R;

public class PayUtil {

    public static void setTextByStatus(Context context, TextView tvPayStatus, int status) {
        if (null == tvPayStatus) {
            return;
        }
        switch (status) {
            case 1:
                tvPayStatus.setText(context.getString(R.string.pay_trans_status_unpaid));
                tvPayStatus.setTextColor(context.getColor(R.color.tx_send));
                return;
            case 2:
                tvPayStatus.setText(context.getString(R.string.pay_trans_status_confirm));
                tvPayStatus.setTextColor(context.getColor(R.color.tx_send));
                return;
            case 3:
                tvPayStatus.setText(context.getString(R.string.pay_trans_status_succ));
                return;
            case 4:
                tvPayStatus.setText(context.getString(R.string.pay_trans_status_fail));
                tvPayStatus.setTextColor(context.getColor(R.color.tx_error));
                return;
            default:
                return;
        }

    }

}
