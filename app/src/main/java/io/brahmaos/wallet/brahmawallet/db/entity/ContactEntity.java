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

package io.brahmaos.wallet.brahmawallet.db.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import java.io.Serializable;

import me.yokeyword.indexablerv.IndexableEntity;

@Entity(tableName = "contacts")
public class ContactEntity implements Serializable, IndexableEntity{
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String familyName;
    private String name;
    private String address;
    private String avatar;
    private String remark;

    public ContactEntity() {
    }

    @Ignore
    public ContactEntity(int id, String familyName, String name, String address, String avatar, String remark) {
        this.id = id;
        this.familyName = familyName;
        this.name = name;
        this.address = address;
        this.avatar = avatar;
        this.remark = remark;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String getFieldIndexBy() {
        return name;
    }

    @Override
    public void setFieldIndexBy(String indexField) {
        this.name = indexField;
    }

    @Override
    public void setFieldPinyinIndexBy(String pinyin) {

    }

    @Override
    public String toString() {
        return "ContactEntity{" +
                "id=" + id +
                ", familyName='" + familyName + '\'' +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", avatar='" + avatar + '\'' +
                ", remark='" + remark + '\'' +
                '}';
    }
}
