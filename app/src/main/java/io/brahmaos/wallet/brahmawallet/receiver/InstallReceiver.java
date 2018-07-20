package io.brahmaos.wallet.brahmawallet.receiver;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.widget.Toast;

import java.io.File;

import io.brahmaos.wallet.util.BLog;

public class InstallReceiver extends BroadcastReceiver {
    protected String tag() {
        return InstallReceiver.class.getName();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())) {
            DownloadManager.Query query = new DownloadManager.Query();
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
            query.setFilterById(id);
            Cursor c = downloadManager.query(query);
            if (c.moveToFirst()) {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                    String filename = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
                    if (filename != null) {
                        File apkfile = new File(filename);
                        if (!apkfile.exists()) {
                            Toast.makeText(context, "要安装的文件不存在，请检查路径", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (filename.endsWith(".apk")) {
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            i.setDataAndType(Uri.parse("file://" + apkfile.toString()),
                                    "application/vnd.android.package-archive");
                            context.startActivity(i);
                        }
                    }
                } else {
                    File file = new File(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                            , "download/BrahmaWallet.apk");
                    if (!file.exists()) {
                        Toast.makeText(context, "要安装的文件不存在，请检查路径", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Uri apkUri = FileProvider.getUriForFile(context, "io.brahmaos.wallet.brahmawallet.fileprovider", file);
                    i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    //i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.setDataAndType(apkUri, "application/vnd.android.package-archive");
                    BLog.i("receiver", apkUri.toString());
                    context.startActivity(i);
                }
            }
            c.close();
        } else if (DownloadManager.ACTION_NOTIFICATION_CLICKED.equals(intent.getAction())) {
            long[] ids = intent.getLongArrayExtra(DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS);
            downloadManager.remove(ids);
            Toast.makeText(context, "已经取消下载", Toast.LENGTH_SHORT).show();
        }
    }
}
