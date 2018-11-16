package io.brahmaos.wallet.brahmawallet.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.Nullable;

import java.util.List;

import io.brahmaos.wallet.brahmawallet.common.BrahmaConst;
import io.brahmaos.wallet.brahmawallet.db.database.WalletDatabase;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.db.entity.AllTokenEntity;
import io.brahmaos.wallet.brahmawallet.db.entity.ContactEntity;
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

    public void changeAccountName(int accountId, String name) {
        mDatabase.accountDao().changeAccountName(accountId, name);
    }

    public void deleteAccount(int accountId) {
        mDatabase.accountDao().deleteAccount(accountId);
    }

    public void changeAccountFilename(int accountId, String filename) {
        mDatabase.accountDao().changeAccountFilename(accountId, filename);
    }

    public void changeAccountEncryptMnemonics(int accountId, String encryptMnemonics) {
        mDatabase.accountDao().changeAccountEncryptMnemonics(accountId, encryptMnemonics);
    }

    public LiveData<AccountEntity> getAccountById(int accountId) {
        return mDatabase.accountDao().loadAccount(accountId);
    }

    /**
     *  process the tokens
     */
    public LiveData<List<TokenEntity>> getTokens() {
        return mObservableTokens;
    }

    public List<TokenEntity> queryChosenTokensSync() {
        return mDatabase.tokenDao().loadAllTokensSync();
    }

    public LiveData<TokenEntity> loadToken(final int tokenId) {
        return mDatabase.tokenDao().loadToken(tokenId);
    }

    public void createToken(TokenEntity token) {
        mDatabase.tokenDao().insertToken(token);
    }

    public void deleteToken(String address) {
        mDatabase.tokenDao().deleteToken(address);
    }

    /**
     *  process the all tokens,first delete all tokens.
     */
    public void insertAllTokens(List<AllTokenEntity> tokenEntities) {
        mDatabase.allTokenDao().deleteAllToken();
        mDatabase.allTokenDao().insertAll(tokenEntities);
    }

    public void deleteAllTokens() {
        mDatabase.allTokenDao().deleteAllToken();
    }

    public LiveData<List<AllTokenEntity>> getShowTokens() {
        return mDatabase.allTokenDao().loadShowTokens();
    }

    public LiveData<List<AllTokenEntity>> getAllTokens() {
        return mDatabase.allTokenDao().loadAllTokens();
    }

    public LiveData<List<AllTokenEntity>> queryAllTokens(String param) {
        return mDatabase.allTokenDao().queryToken(param);
    }

    public List<AllTokenEntity> queryAllTokensSync(String param) {
        return mDatabase.allTokenDao().queryTokenSync(param);
    }

    public void showAllToken(AllTokenEntity allTokenEntity) {
        mDatabase.allTokenDao().updateTokenShowFlag(allTokenEntity.getAddress(),
                BrahmaConst.DEFAULT_TOKEN_SHOW_FLAG);
    }

    public void hideAllToken(AllTokenEntity allTokenEntity) {
        mDatabase.allTokenDao().updateTokenShowFlag(allTokenEntity.getAddress(),
                BrahmaConst.DEFAULT_TOKEN_HIDE_FLAG);
    }

    public LiveData<Integer> getAllTokensCount() {
        return mDatabase.allTokenDao().getAllTokensCount();
    }

    public LiveData<List<ContactEntity>> loadAllContact() {
        return mDatabase.contactDao().loadAllContacts();
    }

    public void insertContact(ContactEntity contact) {
        mDatabase.contactDao().insertContact(contact);
    }

    public void deleteContact(int contactId) {
        mDatabase.contactDao().deleteContact(contactId);
    }

    public LiveData<ContactEntity> getContactById(int contactId) {
        return mDatabase.contactDao().loadContact(contactId);
    }

    public void updateContact(int contactId, ContactEntity contact) {
        mDatabase.contactDao().changeContact(contactId, contact.getFamilyName(),
                contact.getName(), contact.getAddress(), contact.getAvatar(),
                contact.getRemark());
    }

}
