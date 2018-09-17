package io.brahmaos.wallet.brahmawallet.ui.token;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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
import io.brahmaos.wallet.brahmawallet.db.entity.AllTokenEntity;
import io.brahmaos.wallet.brahmawallet.db.entity.TokenEntity;
import io.brahmaos.wallet.brahmawallet.service.ImageManager;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.brahmawallet.viewmodel.AccountViewModel;
import io.brahmaos.wallet.util.BLog;
import io.brahmaos.wallet.util.CommonUtil;
import rx.Observer;
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
    private List<TokenEntity> chooseTokes = null;
    private List<AllTokenEntity> allTokens = new ArrayList<>();
    // test rinkerby token
    private AllTokenEntity testToken = new AllTokenEntity(0, "BrahmaOS", "BRM(TEST)",
                                          "0xb958c57d1896823b8f4178a21e1bf6796371eac4", "", 1);
    // test ropsten
    private AllTokenEntity ropstenKyberToken = new AllTokenEntity(0, "Kyber Network Test", "KNC(TEST)",
            "0x4E470dc7321E84CA96FcAEDD0C8aBCebbAEB68C6", "", 1);

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

        mViewModel.getShowTokens().observe(this, allTokenEntities -> {
            if (allTokenEntities != null) {
                BLog.i(tag(), "the length is:" + allTokenEntities.size());
                allTokens = allTokenEntities;
                //allTokens.add(ropstenKyberToken);
            }
            refreshTokenList();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mViewModel.getChosenTokens()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<TokenEntity>>() {
                    @Override
                    public void onNext(List<TokenEntity> tokenEntities) {
                        if (tokenEntities == null) {
                            chooseTokes = new ArrayList<>();
                        } else {
                            chooseTokes = tokenEntities;
                        }
                        refreshTokenList();
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onCompleted() {

                    }
                });
    }

    private void refreshTokenList() {
        if (chooseTokes != null && chooseTokes.size() > 0 && allTokens.size() > 0) {
            recyclerViewTokens.getAdapter().notifyDataSetChanged();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //noinspection SimplifiableIfStatement
        if (item.getItemId() == R.id.menu_search) {
            Intent intent = new Intent(this, TokenSearchActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
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
                AllTokenEntity tokenEntity = allTokens.get(position);
                setData(itemViewHolder, tokenEntity);
            }
        }

        /**
         * set account view
         */
        private void setData(TokenRecyclerAdapter.ItemViewHolder holder, final AllTokenEntity token) {
            if (token == null) {
                return ;
            }

            holder.tvTokenShoreName.setText(token.getShortName());
            holder.tvTokenAddress.setText(CommonUtil.generateSimpleAddress(token.getAddress()));
            holder.tvTokenName.setText(token.getName());
            // BRM and ETH cannot be cancelled
            if (token.getShortName().equals("ETH")) {
                holder.tvTokenAddress.setVisibility(View.GONE);
                holder.switchToken.setVisibility(View.GONE);
                ImageManager.showTokenIcon(TokensActivity.this, holder.ivTokenAvatar, R.drawable.icon_eth);
            } else if (token.getShortName().equals("BRM")) {
                holder.tvTokenAddress.setVisibility(View.VISIBLE);
                holder.switchToken.setVisibility(View.GONE);
                ImageManager.showTokenIcon(TokensActivity.this, holder.ivTokenAvatar, R.drawable.icon_brm);
            } else {
                holder.tvTokenAddress.setVisibility(View.VISIBLE);
                holder.switchToken.setVisibility(View.VISIBLE);
                ImageManager.showTokenIcon(TokensActivity.this, holder.ivTokenAvatar,
                        token.getName(), token.getAddress());

                // Determine if the token is selected
                boolean checked = false;
                if (chooseTokes != null && chooseTokes.size() > 0) {
                    for (TokenEntity tokenEntity : chooseTokes) {
                        if (tokenEntity.getAddress().toLowerCase().equals(token.getAddress().toLowerCase())) {
                            checked = true;
                            break;
                        }
                    }
                }
                TokenEntity currentToken = new TokenEntity();
                currentToken.setAddress(token.getAddress());
                currentToken.setName(token.getName());
                currentToken.setShortName(token.getShortName());
                currentToken.setAvatar(token.getAvatar());
                holder.switchToken.setOnCheckedChangeListener(null);
                holder.switchToken.setChecked(checked);
                holder.switchToken.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        mViewModel.checkToken(currentToken).subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(() -> {
                                            BLog.e(tag(), "Success to check token:" + token.getName());
                                        },
                                        throwable -> {
                                            BLog.e(tag(), "Unable to check token", throwable);
                                        });
                    } else {
                        mViewModel.uncheckToken(currentToken).subscribeOn(Schedulers.io())
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
