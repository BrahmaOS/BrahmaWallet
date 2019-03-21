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
                tvPayStatus.setText("待支付");
                tvPayStatus.setTextColor(context.getColor(R.color.tx_send));
                return;
            case 2:
                tvPayStatus.setText("支付确认中");
                tvPayStatus.setTextColor(context.getColor(R.color.tx_send));
                return;
            case 3:
                tvPayStatus.setText("");
                return;
            case 4:
                tvPayStatus.setText("支付失败");
                tvPayStatus.setTextColor(context.getColor(R.color.tx_error));
                return;
            default:
                return;
        }

    }

}
