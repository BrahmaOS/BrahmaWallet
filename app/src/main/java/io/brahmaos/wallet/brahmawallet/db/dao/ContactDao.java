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

import io.brahmaos.wallet.brahmawallet.db.entity.ContactEntity;

@Dao
public interface ContactDao {
    @Query("SELECT * FROM contacts")
    LiveData<List<ContactEntity>> loadAllContacts();

    @Insert
    void insertAllContact(List<ContactEntity> contacts);

    @Insert
    void insertContact(ContactEntity contact);

    @Query("delete from contacts where id = :contactId")
    void deleteContact(int contactId);

    @Query("select * from contacts where id = :contactId")
    LiveData<ContactEntity> loadContact(int contactId);

    @Query("select * from contacts where id = :contactId")
    ContactEntity loadContactSync(int contactId);

    @Query("update contacts set familyName = :familyName, name = :name, address = :address, avatar = :avatar, remark = :remark where id = :contactId")
    void changeContact(int contactId, String familyName, String name, String address, String avatar, String remark);
}
