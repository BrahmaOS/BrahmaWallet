package io.brahmaos.wallet.brahmawallet.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.Nullable;

import java.util.List;

import io.brahmaos.wallet.brahmawallet.db.database.WalletDatabase;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.db.entity.TokenEntity;
import io.brahmaos.wallet.util.BLog;
import rx.Completable;

/**
 * Repository handling the work.
 */
public class DataRepository {

    private static DataRepository sInstance;

    private final WalletDatabase mDatabase;
    private MediatorLiveData<List<AccountEntity>> mObservableAccounts;
    private MediatorLiveData<List<TokenEntity>> mObservableTokens;

    private DataRepository(final WalletDatabase database) {
        mDatabase = database;
        mObservableAccounts = new MediatorLiveData<>();
        mObservableTokens = new MediatorLiveData<>();

        mObservableAccounts.addSource(mDatabase.accountDao().loadAllAccounts(),
                new Observer<List<AccountEntity>>() {
                    @Override
                    public void onChanged(@Nullable List<AccountEntity> accountEntities) {
                        if (mDatabase.getDatabaseCreated().getValue() != null) {
                            mObservableAccounts.postValue(accountEntities);
                        }
                    }
                });

        mObservableTokens.addSource(mDatabase.tokenDao().loadAllTokens(),
                new Observer<List<TokenEntity>>() {
                    @Override
                    public void onChanged(@Nullable List<TokenEntity> tokenEntities) {
                        if (mDatabase.getDatabaseCreated().getValue() != null) {
                            mObservableTokens.postValue(tokenEntities);
                        }
                    }
                });
    }

    public static DataRepository getInstance(final WalletDatabase database) {
        if (sInstance == null) {
            synchronized (DataRepository.class) {
                if (sInstance == null) {
                    sInstance = new DataRepository(database);
                }
            }
        }
        return sInstance;
    }

    /**
     * Get the list of account from the database and get notified when the data changes.
     */
    public LiveData<List<AccountEntity>> getAccounts() {
        return mObservableAccounts;
    }

    public LiveData<AccountEntity> loadAccount(final int accountId) {
        return mDatabase.accountDao().loadAccount(accountId);
    }

    public void createAccount(AccountEntity account) {
        mDatabase.accountDao().insertAccount(account);
    }

    /**
     *  process the tokens
     */
    public LiveData<List<TokenEntity>> getTokens() {
        return mObservableTokens;
    }

    public LiveData<TokenEntity> loadToken(final int tokenId) {
        return mDatabase.tokenDao().loadToken(tokenId);
    }

    public void createToken(TokenEntity token) {
        mDatabase.tokenDao().insertToken(token);
    }
}
