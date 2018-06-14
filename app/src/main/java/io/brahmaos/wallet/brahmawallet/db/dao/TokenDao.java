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
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.db.entity.TokenEntity;

@Dao
public interface TokenDao {
    @Query("SELECT * FROM tokens")
    LiveData<List<TokenEntity>> loadAllTokens();

    @Query("SELECT * FROM tokens")
    List<TokenEntity> loadAllTokensSync();

    @Insert
    void insertAll(List<TokenEntity> tokens);

    @Insert
    void insertToken(TokenEntity token);

    @Query("delete from tokens where LOWER(address) = LOWER(:address)")
    void deleteToken(String address);

    @Query("select * from tokens where id = :tokenId")
    LiveData<TokenEntity> loadToken(int tokenId);

    @Query("select * from tokens where id = :tokenId")
    TokenEntity loadTokenSync(int tokenId);
}
