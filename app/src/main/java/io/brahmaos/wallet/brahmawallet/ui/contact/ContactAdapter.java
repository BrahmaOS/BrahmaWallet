package io.brahmaos.wallet.brahmawallet.ui.contact;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.db.entity.ContactEntity;
import io.brahmaos.wallet.util.BitcoinAddressUtil;
import io.brahmaos.wallet.util.EthereumAddressUtil;
import io.brahmaos.wallet.util.ImageUtil;
import me.yokeyword.indexablerv.IndexableAdapter;


public class ContactAdapter extends IndexableAdapter<ContactEntity> {

    public static final String TAG = ContactAdapter.class.getName();

    private LayoutInflater mInflater;
    private Context context;

    ContactAdapter(Context context) {
        this.context = context;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateTitleViewHolder(ViewGroup parent) {
        View view = mInflater.inflate(R.layout.item_index_contact, parent, false);
        return new IndexVH(view);
    }

    @Override
    public RecyclerView.ViewHolder onCreateContentViewHolder(ViewGroup parent) {
        View view = mInflater.inflate(R.layout.item_contact, parent, false);
        return new ContentVH(view);
    }

    @Override
    public void onBindTitleViewHolder(RecyclerView.ViewHolder holder, String indexTitle) {
        IndexVH vh = (IndexVH) holder;
        vh.tv.setText(indexTitle);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindContentViewHolder(RecyclerView.ViewHolder holder, ContactEntity entity) {
        Log.i(TAG, "entity - " + entity);
        ContentVH vh = (ContentVH) holder;
        vh.tvName.setText(entity.getName() + " " + entity.getFamilyName());

        if (entity.getAvatar() != null && entity.getAvatar().length() > 0 && !entity.getAvatar().equals("null")) {
            Uri uriAvatar = Uri.parse(entity.getAvatar());
            try {
                Bitmap bmpAvatar = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uriAvatar);
                vh.ivAvatar.setImageBitmap(ImageUtil.getCircleBitmap(bmpAvatar));
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "Crop failed", Toast.LENGTH_SHORT).show();
            }
        } else {
            vh.ivAvatar.setImageBitmap(BitmapFactory.decodeResource(
                    context.getResources(), R.drawable.ic_default_account_avatar_grey));
        }

        String ethAddress = entity.getAddress();
        if (ethAddress != null && ethAddress.length() > 0) {
            vh.layoutEthAddress.setVisibility(View.VISIBLE);
            vh.layoutBtcAddress.setVisibility(View.GONE);
            vh.tvEthAddress.setText(EthereumAddressUtil.simplifyDisplay(ethAddress));
        } else {
            vh.layoutEthAddress.setVisibility(View.GONE);
        }

        String btcAddress = entity.getBtcAddress();
        if (btcAddress != null && btcAddress.length() > 0) {
            vh.layoutBtcAddress.setVisibility(View.VISIBLE);
            vh.tvBtcAddress.setText(BitcoinAddressUtil.simplifyDisplay(btcAddress));
        } else {
            vh.layoutBtcAddress.setVisibility(View.GONE);
        }
    }

    private class IndexVH extends RecyclerView.ViewHolder {
        TextView tv;

        IndexVH(View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.tv_index);
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
