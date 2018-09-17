package io.brahmaos.wallet.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;


public class ImageUtil {

    public static Bitmap getCircleBitmap(Bitmap bitmap) {

        int bmpWidth = bitmap.getWidth();
        int bmpHeight = bitmap.getHeight();
        int radius = (bmpHeight > bmpWidth ? bmpWidth : bmpHeight)/2;

        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        Bitmap roundBitmap = Bitmap.createBitmap(radius * 2,
                radius * 2, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(roundBitmap);
        canvas.drawARGB(0, 0, 0, 0);

        canvas.drawCircle(radius, radius, radius, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return roundBitmap;
    }
}
