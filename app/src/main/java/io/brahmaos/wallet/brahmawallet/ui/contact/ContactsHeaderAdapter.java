package io.brahmaos.wallet.brahmawallet.ui.contact;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConst;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.service.ImageManager;
import io.brahmaos.wallet.util.BitcoinAddressUtil;
import io.brahmaos.wallet.util.EthereumAddressUtil;
import me.yokeyword.indexablerv.IndexableHeaderAdapter;


public class ContactsHeaderAdapter extends IndexableHeaderAdapter<AccountEntity> {

    public static final String TAG = ContactsHeaderAdapter.class.getName();

    private LayoutInflater mInflater;
    private Context ctx;

    ContactsHeaderAdapter(Context context, String index, String indexTitle, List<AccountEntity> datas) {
        super(index, indexTitle, datas);
        this.ctx = context;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getItemViewType() {
        return 0;
    }

    @Override
    public RecyclerView.ViewHolder onCreateContentViewHolder(ViewGroup parent) {
        View view = mInflater.inflate(R.layout.item_contact_my_account, parent, false);
        return new ContentVH(view);
    }

    @Override
    public void onBindContentViewHolder(RecyclerView.ViewHolder holder, AccountEntity entity) {

        Log.i(TAG, "entity - " + entity);
        ContentVH vh = (ContentVH) holder;
        vh.tvName.setText(entity.getName());
        ImageManager.showAccountAvatar(ctx, vh.ivAvatar, entity);

        if (entity.getType() == BrahmaConst.ETH_ACCOUNT_TYPE) {
            String ethAddress = entity.getAddress();
            if (ethAddress != null && ethAddress.length() > 0) {
                vh.layoutEthAddress.setVisibility(View.VISIBLE);
                vh.layoutBtcAddress.setVisibility(View.GONE);
                vh.tvEthAddress.setText(EthereumAddressUtil.simplifyDisplay(ethAddress));
            } else {
                vh.layoutEthAddress.setVisibility(View.GONE);
            }
        } else {
            vh.layoutEthAddress.setVisibility(View.GONE);
        }

        if (entity.getType() == BrahmaConst.BTC_ACCOUNT_TYPE) {
            String btcAddress = entity.getAddress();
            if (btcAddress != null && btcAddress.length() > 0) {
                vh.layoutBtcAddress.setVisibility(View.VISIBLE);
                vh.layoutEthAddress.setVisibility(View.GONE);
                vh.tvBtcAddress.setText(BitcoinAddressUtil.simplifyDisplay(btcAddress));
            } else {
                vh.layoutBtcAddress.setVisibility(View.GONE);
            }
        } else {
            vh.layoutBtcAddress.setVisibility(View.GONE);
        }
    }

    private class ContentVH extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageView ivAvatar;

        LinearLayout layoutEthAddress;
        TextView tvEthAddress;

        LinearLayout layoutBtcAddress;
        TextView tvBtcAddress;

        ContentVH(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);

            layoutEthAddress = itemView.findViewById(R.id.eth_address_layout);
            tvEthAddress = itemView.findViewById(R.id.eth_address_tv);

            layoutBtcAddress = itemView.findViewById(R.id.btc_address_layout);
            tvBtcAddress = itemView.findViewById(R.id.btc_address_tv);
        }
    }
}
