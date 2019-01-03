package io.brahmaos.wallet.brahmawallet.ui.home;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.IntentParam;
import io.brahmaos.wallet.brahmawallet.model.Dapp;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseFragment;
import io.brahmaos.wallet.brahmawallet.ui.dapp.DappActivity;

public class DiscoverFragment extends BaseFragment {
    @Override
    protected String tag() {
        return DiscoverFragment.class.getName();
    }

    private RecyclerView recyclerViewDapp;
    private List<Dapp> dapps = new ArrayList<>();

    /**
     * instance
     *
     * @param layoutResId  layout resource，e.g. R.layout.fragment_home
     * @return  return fragment
     */
    public static DiscoverFragment newInstance(int layoutResId, int titleResId) {
        DiscoverFragment fragment = new DiscoverFragment();
        fragment.setArguments(newArguments(layoutResId, titleResId));
        return fragment;
    }

    @Override
    protected boolean initView() {
        recyclerViewDapp = parentView.findViewById(R.id.dapp_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerViewDapp.setLayoutManager(layoutManager);
        recyclerViewDapp.setAdapter(new DappRecyclerAdapter());

        // Solve the sliding lag problem
        recyclerViewDapp.setHasFixedSize(true);
        recyclerViewDapp.setNestedScrollingEnabled(false);

        initData();
        return true;
    }

    private void initData() {
        Dapp etherscan = new Dapp(R.drawable.icon_etherscan, getString(R.string.dapp_name_etherscan),
                getString(R.string.dapp_desc_etherscan), "https://etherscan.io/");
        Dapp ethFans = new Dapp(R.drawable.icon_ehtfans, getString(R.string.dapp_name_ethfans),
                getString(R.string.dapp_desc_ethfans), "https://ethfans.org/");
        Dapp blockcypher = new Dapp(R.drawable.icon_blockcypher, getString(R.string.dapp_name_blockcypher),
                getString(R.string.dapp_desc_blockcypher), "https://www.blockcypher.com/");
        Dapp ddex = new Dapp(R.drawable.icon_ddex, getString(R.string.dapp_name_ddex),
                getString(R.string.dapp_desc_ddex), "https://ddex.io/");
        Dapp kyberSwap = new Dapp(R.drawable.icon_kyber, getString(R.string.dapp_name_kyberswap),
                getString(R.string.dapp_desc_kyberswap), "https://kyber.network/swap/eth_knc");
        dapps.add(etherscan);
        dapps.add(ethFans);
        dapps.add(blockcypher);
        dapps.add(ddex);
        dapps.add(kyberSwap);

        recyclerViewDapp.getAdapter().notifyDataSetChanged();
    }

    /**
     * list item account
     */
    private class DappRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_dapp, parent, false);
            return new DappRecyclerAdapter.ItemViewHolder(rootView);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof DappRecyclerAdapter.ItemViewHolder) {
                DappRecyclerAdapter.ItemViewHolder itemViewHolder = (DappRecyclerAdapter.ItemViewHolder) holder;
                Dapp dapp = dapps.get(position);
                setDappData(itemViewHolder, dapp);
            }
        }

        /**
         * set assets view
         */
        private void setDappData(DappRecyclerAdapter.ItemViewHolder holder, final Dapp dapp) {
            if (dapp == null) {
                return;
            }
            holder.layoutDapp.setOnClickListener(v -> {
                if (dapp.getUrl() != null && dapp.getUrl().length() > 0) {
                    Intent intent = new Intent(getActivity(), DappActivity.class);
                    intent.putExtra(IntentParam.PARAM_DAPP_URL, dapp.getUrl());
                    startActivity(intent);
                }
            });
            Glide.with(getContext())
                    .load(dapp.getIcon())
                    .into(holder.ivDappIcon);
            holder.tvDappName.setText(dapp.getName());
            holder.tvDappDesc.setText(dapp.getDesc());
        }

        @Override
        public int getItemCount() {
            return dapps.size();
        }

        class ItemViewHolder extends RecyclerView.ViewHolder {

            LinearLayout layoutDapp;
            ImageView ivDappIcon;
            TextView tvDappName;
            TextView tvDappDesc;

            ItemViewHolder(View itemView) {
                super(itemView);
                layoutDapp = itemView.findViewById(R.id.layout_dapp);
                ivDappIcon = itemView.findViewById(R.id.iv_dapp_icon);
                tvDappName = itemView.findViewById(R.id.tv_dapp_name);
                tvDappDesc = itemView.findViewById(R.id.tv_dapp_desc);
            }
        }
    }
}

