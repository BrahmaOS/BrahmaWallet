package io.brahmaos.wallet.brahmawallet.ui.token;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConfig;
import io.brahmaos.wallet.brahmawallet.db.entity.TokenEntity;
import io.brahmaos.wallet.brahmawallet.service.ImageManager;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.brahmawallet.viewmodel.AccountViewModel;
import io.brahmaos.wallet.util.BLog;
import io.brahmaos.wallet.util.CommonUtil;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class TokensActivity extends BaseActivity {

    @Override
    protected String tag() {
        return TokensActivity.class.getName();
    }

    // UI references.
    @BindView(R.id.tokens_recycler)
    RecyclerView recyclerViewTokens;

    private AccountViewModel mViewModel;
    private List<TokenEntity> chooseTokes = new ArrayList<>();
    private List<TokenEntity> allTokens = BrahmaConfig.getInstance().getTokenEntities();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tokens);
        ButterKnife.bind(this);
        showNavBackBtn();
        mViewModel = ViewModelProviders.of(this).get(AccountViewModel.class);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewTokens.setLayoutManager(layoutManager);
        recyclerViewTokens.setAdapter(new TokenRecyclerAdapter());

        mViewModel.getTokens().observe(this, tokenEntities -> {
            if (tokenEntities == null) {
                chooseTokes = new ArrayList<>();
            } else {
                chooseTokes = tokenEntities;
            }
        });
    }

    /**
     * list item account
     */
    private class TokenRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_token, parent, false);
            rootView.setOnClickListener(v -> {

            });
            return new TokenRecyclerAdapter.ItemViewHolder(rootView);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof TokenRecyclerAdapter.ItemViewHolder) {
                TokenRecyclerAdapter.ItemViewHolder itemViewHolder = (TokenRecyclerAdapter.ItemViewHolder) holder;
                TokenEntity tokenEntity = allTokens.get(position);
                setData(itemViewHolder, tokenEntity);
            }
        }

        /**
         * set account view
         */
        private void setData(TokenRecyclerAdapter.ItemViewHolder holder, final TokenEntity token) {
            if (token == null) {
                return ;
            }
            ImageManager.showTokenIcon(TokensActivity.this, holder.ivTokenAvatar, token.getIcon());
            holder.tvTokenShoreName.setText(token.getShortName());
            holder.tvTokenAddress.setText(CommonUtil.generateSimpleAddress(token.getAddress()));
            holder.tvTokenName.setText(token.getName());
            // BRM and ETH cannot be cancelled
            if (token.getShortName().equals("ETH")) {
                holder.tvTokenAddress.setVisibility(View.GONE);
                holder.switchToken.setVisibility(View.GONE);
            } else if (token.getShortName().equals("BRM")) {
                holder.tvTokenAddress.setVisibility(View.VISIBLE);
                holder.switchToken.setVisibility(View.GONE);
            } else {
                holder.tvTokenAddress.setVisibility(View.VISIBLE);
                holder.switchToken.setVisibility(View.VISIBLE);

                // Determine if the token is selected
                boolean checked = false;
                if (chooseTokes != null && chooseTokes.size() > 0) {
                    for (TokenEntity tokenEntity : chooseTokes) {
                        if (tokenEntity.getAddress().equals(token.getAddress())) {
                            checked = true;
                            break;
                        }
                    }
                }
                holder.switchToken.setOnCheckedChangeListener(null);
                holder.switchToken.setChecked(checked);
                holder.switchToken.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        mViewModel.checkToken(token).subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(() -> {
                                            BLog.e(tag(), "Success to check token:" + token.getName());
                                        },
                                        throwable -> {
                                            BLog.e(tag(), "Unable to check token", throwable);
                                        });
                    } else {
                        mViewModel.uncheckToken(token).subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(() -> {
                                            BLog.e(tag(), "Success to uncheck token" + token.getName());
                                        },
                                        throwable -> {
                                            BLog.e(tag(), "Unable to uncheck token", throwable);
                                        });;
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return allTokens.size();
        }

        class ItemViewHolder extends RecyclerView.ViewHolder {

            ImageView ivTokenAvatar;
            TextView tvTokenShoreName;
            TextView tvTokenName;
            TextView tvTokenAddress;
            Switch switchToken;

            ItemViewHolder(View itemView) {
                super(itemView);
                ivTokenAvatar = itemView.findViewById(R.id.iv_token_icon);
                tvTokenShoreName = itemView.findViewById(R.id.tv_token_short_name);
                tvTokenName = itemView.findViewById(R.id.tv_token_name);
                tvTokenAddress = itemView.findViewById(R.id.tv_token_address);
                switchToken = itemView.findViewById(R.id.switch_token);
            }
        }
    }
}
