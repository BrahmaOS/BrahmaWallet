package io.brahmaos.wallet.brahmawallet.service;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.web3j.protocol.ObjectMapperFactory;

import java.io.File;
import java.io.IOException;

import io.brahmaos.wallet.brahmawallet.BuildConfig;
import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.api.ApiConst;
import io.brahmaos.wallet.brahmawallet.api.ApiRespResult;
import io.brahmaos.wallet.brahmawallet.api.Networks;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConfig;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConst;
import io.brahmaos.wallet.brahmawallet.model.VersionInfo;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.util.BLog;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/*
 * Responsible for version detection upgrade and installation
 */

public class VersionUpgradeService {
    protected String tag() {
        return VersionUpgradeService.class.getName();
    }

    private static VersionUpgradeService instance = new VersionUpgradeService();
    public static VersionUpgradeService getInstance() {
        return instance;
    }

    public static VersionInfo CURR_VER = new VersionInfo();
    static {
        CURR_VER.appId = BrahmaConst.APP_ID;
        CURR_VER.os = ApiConst.OSTYPE_ANDROID;
    }

    /**
     * 返回应用当前版本信息
     */
    public static VersionInfo getCurrVer(Context context) {

        PackageInfo packInfo;
        try {
            packInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        CURR_VER.code = packInfo.versionCode;
        CURR_VER.name = packInfo.versionName;
        return CURR_VER;
    }

    /**
     * callback
     */
    public interface INewVerNotify {

        void alreadyLatest();

        void confirmUpdate(VersionInfo newVer);

        void cancelUpdate(VersionInfo newVer);
    }

    /**
     * check version
     *
     * @param dontAlert if true, don't show alert window
     */
    public void checkVersion(final BaseActivity activity,
                             final boolean dontAlert, final INewVerNotify notify) {

        VersionInfo currVer = getCurrVer(activity);
        if (currVer != null) {
            Networks.getInstance().getWalletApi().getLatestVersion(BrahmaConst.APP_ID,
                    ApiConst.OSTYPE_ANDROID)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<ApiRespResult>() {

                        @Override
                        public void onCompleted() {
                            BLog.i(tag(), "onCompleted.");
                        }

                        @Override
                        public void onError(Throwable e) {
                            BLog.e(tag(), "onError - " + e.getMessage());
                        }

                        @Override
                        public void onNext(ApiRespResult apr) {
                            if (apr != null && apr.getResult() == 0) {
                                if (apr.getData() != null
                                        && apr.getData().get(ApiConst.PARAM_VER_INFO) != null) {
                                    ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
                                    try {
                                        VersionInfo newVer = objectMapper.readValue(objectMapper.writeValueAsString(apr.getData().get(ApiConst.PARAM_VER_INFO)), new TypeReference<VersionInfo>() {});
                                        if (newVer.getPkgUrl() != null && newVer.getDesc() != null
                                                && newVer.getCode() > BuildConfig.VERSION_CODE && newVer.getPkgSize() > 0) {
                                            showVersionDlg(activity, newVer, dontAlert, notify);
                                        } else {
                                            BLog.d(tag(), "this is the latest version");
                                            if (notify != null) {
                                                notify.alreadyLatest();
                                            }
                                        }
                                    } catch (IOException e1) {
                                        e1.printStackTrace();
                                        if (notify != null) {
                                            notify.alreadyLatest();
                                        }
                                    }
                                } else {
                                    if (notify != null) {
                                        notify.alreadyLatest();
                                    }
                                }
                            }
                        }
                    });
        }
    }

    private AlertDialog dlgVersion;
    private void showVersionDlg(final BaseActivity activity,
                                final VersionInfo newVer, final boolean dontAlert, final INewVerNotify notify) {

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.dialog_title_upgrade_prompt);
        builder.setMessage(newVer.getDesc());
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dlgVersion.cancel();
                if (notify != null) {
                    notify.cancelUpdate(newVer);
                }
            }
        });
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                notify.confirmUpdate(newVer);
                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    activity.requestExternalStorage();
                } else {
                    downloadApkFile(activity, newVer, notify);
                }
            }
        });
        dlgVersion = builder.create();
        dlgVersion.show();
    }

    public void downloadApkFile(BaseActivity activity, VersionInfo newVer, INewVerNotify notify) {
        if (dlgVersion != null) {
            dlgVersion.cancel();
        }
        if (notify != null) {
            notify.confirmUpdate(newVer);
        }
        File file = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                , "download/BrahmaWallet.apk");
        if (file.exists()) {
            file.delete();
        }

        DownloadManager downloadManager = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(newVer.getPkgUrl()));
        request.setVisibleInDownloadsUi(true);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "download/BrahmaWallet.apk");
        request.setMimeType("application/vnd.android.package-archive");
        downloadManager.enqueue(request);
    }

}
