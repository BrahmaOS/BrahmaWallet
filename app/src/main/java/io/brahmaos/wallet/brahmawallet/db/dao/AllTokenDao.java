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

import io.brahmaos.wallet.brahmawallet.db.entity.AllTokenEntity;

@Dao
public interface AllTokenDao {
    @Query("SELECT * FROM all_tokens")
    LiveData<List<AllTokenEntity>> loadAllTokens();

    @Query("SELECT * FROM all_tokens where showFlag = 1")
    LiveData<List<AllTokenEntity>> loadShowTokens();

    @Query("update all_tokens set showFlag = :flag where address = :address")
    void updateTokenShowFlag(String address, int flag);

    @Insert
    void insertAll(List<AllTokenEntity> tokens);

    @Insert
    void insertToken(AllTokenEntity token);

    @Query("delete from all_tokens where address = :address")
    void deleteToken(String address);

    @Query("delete from all_tokens")
    void deleteAllToken();

    @Query("select * from all_tokens where id = :tokenId")
    LiveData<AllTokenEntity> loadToken(int tokenId);

    @Query("select * from all_tokens where name like :params or shortName like :params")
    LiveData<List<AllTokenEntity>> queryToken(String params);

    @Query("select * from all_tokens where name like :params or shortName like :params")
    List<AllTokenEntity> queryTokenSync(String params);

    @Query("select * from all_tokens where id = :tokenId")
    AllTokenEntity loadTokenSync(int tokenId);
}
