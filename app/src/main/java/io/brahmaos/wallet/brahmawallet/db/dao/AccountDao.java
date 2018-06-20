/*
 * Copyright 2017, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.brahmaos.wallet.brahmawallet.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;

@Dao
public interface AccountDao {
    @Query("SELECT * FROM accounts order by id desc")
    LiveData<List<AccountEntity>> loadAllAccounts();

    @Insert
    void insertAll(List<AccountEntity> accounts);

    @Insert
    void insertAccount(AccountEntity account);

    @Query("select * from accounts where id = :accountId")
    LiveData<AccountEntity> loadAccount(int accountId);

    @Query("select * from accounts where id = :accountId")
    AccountEntity loadAccountSync(int accountId);

    @Query("update accounts set name = :accountName where id = :accountId")
    void changeAccountName(int accountId, String accountName);

    @Query("update accounts set filename = :filename where id = :accountId")
    void changeAccountFilename(int accountId, String filename);

    @Query("delete from accounts where id = :accountId")
    void deleteAccount(int accountId);
}
