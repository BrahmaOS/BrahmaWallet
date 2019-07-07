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

package io.brahmaos.wallet.brahmawallet.db.database;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;

import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConst;
import io.brahmaos.wallet.brahmawallet.db.converter.DateConverter;
import io.brahmaos.wallet.brahmawallet.db.dao.AccountDao;
import io.brahmaos.wallet.brahmawallet.db.dao.AllTokenDao;
import io.brahmaos.wallet.brahmawallet.db.dao.ContactDao;
import io.brahmaos.wallet.brahmawallet.db.dao.TokenDao;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.db.entity.AllTokenEntity;
import io.brahmaos.wallet.brahmawallet.db.entity.ContactEntity;
import io.brahmaos.wallet.brahmawallet.db.entity.TokenEntity;


@Database(entities = {AccountEntity.class, TokenEntity.class, AllTokenEntity.class, ContactEntity.class}, version = 7, exportSchema = false)
@TypeConverters(DateConverter.class)
public abstract class WalletDatabase extends RoomDatabase {

    private static WalletDatabase sInstance;

    private static final String DATABASE_NAME = "wallet-db";

    public abstract AccountDao accountDao();
    public abstract TokenDao tokenDao();
    public abstract AllTokenDao allTokenDao();
    public abstract ContactDao contactDao();

    private final MutableLiveData<Boolean> mIsDatabaseCreated = new MutableLiveData<>();

    public static WalletDatabase getInstance(final Context context) {
        if (sInstance == null) {
            synchronized (WalletDatabase.class) {
                if (sInstance == null) {
                    sInstance = buildDatabase(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    /**
     * Build the database. {@link Builder#build()} only sets up the database configuration and
     * creates a new instance of the database.
     * The SQLite database is only created when it's accessed for the first time.
     */
    private static WalletDatabase buildDatabase(final Context appContext) {
        return Room.databaseBuilder(appContext, WalletDatabase.class, DATABASE_NAME)
                .addCallback(new Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                        super.onCreate(db);
                        // Check whether the database already exists after first create database
                        sInstance.updateDatabaseCreated(appContext);
                        db.execSQL("INSERT INTO tokens (name, address, shortName, avatar, code) " +
                                "values (\"BrahmaOS\", \"0xd7732e3783b0047aa251928960063f863ad022d8\", \"BRM\", "
                                + String.valueOf(R.drawable.icon_brm) + ", \"" + BrahmaConst.COIN_CODE_BRM + "\")");
                        db.execSQL("INSERT INTO tokens (name, address, shortName, avatar, code) " +
                                "values (\"Ethereum\", \"\", \"ETH\", "
                                + String.valueOf(R.drawable.icon_eth)  + ", \"" + BrahmaConst.COIN_CODE_ETH + "\")");
                        db.execSQL("INSERT INTO tokens (name, address, shortName, avatar, code) " +
                                "values (\"Bitcoin\", \"btc\", \"BTC\", \"\", \"" + BrahmaConst.COIN_CODE_BTC + "\")");
                    }

                    @Override
                    public void onOpen(@NonNull SupportSQLiteDatabase db) {
                        super.onOpen(db);
                        // Check whether the database already exists after access database
                        sInstance.updateDatabaseCreated(appContext);
                    }
                })
                .addMigrations(MIGRATION_1_2)
                .addMigrations(MIGRATION_2_3)
                .addMigrations(MIGRATION_3_4)
                .addMigrations(MIGRATION_4_5)
                .addMigrations(MIGRATION_5_6)
                .addMigrations(MIGRATION_6_7)
                .build();
    }

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE `tokens` (`id` INTEGER not null, "
                    + "`name` TEXT, `address` TEXT unique, `shortName` TEXT, `icon` INTEGER, " +
                    "PRIMARY KEY(`id`))");
            database.execSQL("INSERT INTO tokens (name, address, shortName, icon) " +
                    "values (\"BrahmaOS\", \"0xd7732e3783b0047aa251928960063f863ad022d8\", \"BRM\", "
                    + R.drawable.icon_brm + ")");
            database.execSQL("INSERT INTO tokens (name, address, shortName, icon) " +
                    "values (\"Ethereum\", \"\", \"ETH\", "
                    + R.drawable.icon_eth + ")");
        }
    };

    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE tokens ADD COLUMN avatar TEXT");
            database.execSQL("UPDATE tokens SET avatar = icon");
            database.execSQL("CREATE TABLE `all_tokens` (`id` INTEGER not null, "
                    + "`name` TEXT, `address` TEXT unique, `shortName` TEXT, `avatar` TEXT, `showFlag` INTEGER not null," +
                    "PRIMARY KEY(`id`))");
            database.execSQL("CREATE UNIQUE INDEX `index_all_tokens_address` on `all_tokens` (`address`);");
        }
    };

    private static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE `contacts` (`id` INTEGER not null, "
                    + "`familyName` TEXT, `name` TEXT, `address` TEXT, `avatar` TEXT, `remark` TEXT," +
                    "PRIMARY KEY(`id`))");
        }
    };

    private static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE contacts ADD COLUMN btcAddress TEXT");
            database.execSQL("ALTER TABLE accounts ADD COLUMN type INTEGER default 1 not null");
            database.execSQL("ALTER TABLE accounts ADD COLUMN cryptoMnemonics TEXT");
            database.execSQL("UPDATE accounts SET type = 1");
            database.execSQL("INSERT INTO tokens (name, address, shortName, avatar) " +
                    "values (\"Bitcoin\", \"btc\", \"BTC\", \"\")");
        }
    };

    private static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE tokens ADD COLUMN code INTEGER default 0 not null");
            database.execSQL("ALTER TABLE all_tokens ADD COLUMN code INTEGER default 0 not null");
        }
    };

    private static final Migration MIGRATION_6_7 = new Migration(5, 6) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("INSERT INTO tokens (name, address, shortName, avatar, code) " +
                    "values (\"Tether\", \"usdt\", \"USDT\", \"\", \"" + BrahmaConst.COIN_CODE_BTC + "\")");
        }
    };

    /**
     * Check whether the database already exists and expose it via {@link #getDatabaseCreated()}
     */
    private void updateDatabaseCreated(final Context context) {
        if (context.getDatabasePath(DATABASE_NAME).exists()) {
            setDatabaseCreated();
        }
    }

    private void setDatabaseCreated(){
        mIsDatabaseCreated.postValue(true);
    }

    public LiveData<Boolean> getDatabaseCreated() {
        return mIsDatabaseCreated;
    }
}
