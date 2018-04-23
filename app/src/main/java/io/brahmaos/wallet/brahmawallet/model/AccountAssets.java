package io.brahmaos.wallet.brahmawallet.model;

import java.math.BigInteger;

import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.db.entity.TokenEntity;

public class AccountAssets {
    private AccountEntity accountEntity;
    private TokenEntity tokenEntity;
    private BigInteger balance;

    public AccountEntity getAccountEntity() {
        return accountEntity;
    }

    public void setAccountEntity(AccountEntity accountEntity) {
        this.accountEntity = accountEntity;
    }

    public TokenEntity getTokenEntity() {
        return tokenEntity;
    }

    public void setTokenEntity(TokenEntity tokenEntity) {
        this.tokenEntity = tokenEntity;
    }

    public BigInteger getBalance() {
        return balance;
    }

    public void setBalance(BigInteger balance) {
        this.balance = balance;
    }

    public AccountAssets() {

    }

    public AccountAssets(AccountEntity accountEntity, TokenEntity tokenEntity, BigInteger balance) {
        this.accountEntity = accountEntity;
        this.tokenEntity = tokenEntity;
        this.balance = balance;
    }

    @Override
    public String toString() {
        return "AccountAssets{" +
                "accountEntity=" + accountEntity +
                ", tokenEntity=" + tokenEntity +
                ", balance=" + balance +
                '}';
    }
}
