package io.brahmaos.wallet.brahmawallet.ui.token;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

public class TokenSearchActivity extends BaseActivity {

    @Override
    protected String tag() {
        return TokenSearchActivity.class.getName();
    }

    // UI references.
    @BindView(R.id.tokens_recycler)
    RecyclerView recyclerViewTokens;
    @BindView(R.id.layout_no_result)
    LinearLayout layoutNoResult;
    @BindView(R.id.layout_default)
    LinearLayout layoutDefault;

    private AccountViewModel mViewModel;
    private List<TokenEntity> chooseTokes = null;
    private List<AllTokenEntity> allTokens = new ArrayList<>();
    private String currentData = "";

    private SearchView.SearchAutoComplete searchAutoComplete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_token_search);
        ButterKnife.bind(this);
        showNavBackBtn();

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle("");
        }

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search_token, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchAutoComplete = searchView.findViewById(R.id.search_src_text);
        searchView.onActionViewExpanded();
        searchView.setMaxWidth(30000);
        searchView.setQueryHint(getString(R.string.prompt_search_token));

        searchView.setOnCloseListener(() -> {
            recyclerViewTokens.setVisibility(View.GONE);
            layoutNoResult.setVisibility(View.GONE);
            layoutDefault.setVisibility(View.VISIBLE);
            return false;
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if (s.length() > 0) {
                    queryTokens(s);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                /*currentData = s;
                if (delayRun != null) {
                    handler.removeCallbacks(delayRun);
                }
                handler.postDelayed(delayRun, 1000);*/

                return false;
            }

        });

        return super.onCreateOptionsMenu(menu);
    }

    private Handler handler = new Handler();

    private Runnable delayRun = new Runnable() {
        @Override
        public void run() {
            String inputStr = searchAutoComplete.getText().toString();
            if (inputStr.equals(currentData) && inputStr.length() > 0) {
                queryTokens(inputStr);
            }
        }
    };

    private void queryTokens(String param) {
        mViewModel.queryAllTokensSync("%" + param + "%")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<AllTokenEntity>>() {
                    @Override
                    public void onNext(List<AllTokenEntity> allTokenEntities) {
                        if (allTokenEntities == null || allTokenEntities.size() <= 0) {
                            recyclerViewTokens.setVisibility(View.GONE);
                            layoutNoResult.setVisibility(View.VISIBLE);
                            layoutDefault.setVisibility(View.GONE);
                        } else {
                            recyclerViewTokens.setVisibility(View.VISIBLE);
                            layoutNoResult.setVisibility(View.GONE);
                            layoutDefault.setVisibility(View.GONE);
                            allTokens = allTokenEntities;
                            // When change database, don't need refresh page
                            recyclerViewTokens.getAdapter().notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onCompleted() {
                        BLog.i(tag(), "the param is: " + param);
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
            View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_token_search, parent, false);
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
                ImageManager.showTokenIcon(TokenSearchActivity.this, holder.ivTokenAvatar, R.drawable.icon_eth);
            } else if (token.getShortName().equals("BRM")) {
                holder.tvTokenAddress.setVisibility(View.VISIBLE);
                holder.switchToken.setVisibility(View.GONE);
                ImageManager.showTokenIcon(TokenSearchActivity.this, holder.ivTokenAvatar, R.drawable.icon_brm);
            } else {
                holder.tvTokenAddress.setVisibility(View.VISIBLE);
                holder.switchToken.setVisibility(View.VISIBLE);
                ImageManager.showTokenIcon(TokenSearchActivity.this, holder.ivTokenAvatar, token.getAvatar(), token.getName());

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
