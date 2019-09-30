/*
 * Copyright (C) 2019 xuqiqiang. All rights reserved.
 * Libutils
 */
package com.snailstudio2010.libutils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;

/**
 * Created by xuqiqiang on 2019/09/24.
 */
public final class ImageUtils {

    private ImageUtils() {
    }

    public static Bitmap centerCrop(Bitmap bm) {
        int min = Math.min(bm.getWidth(), bm.getHeight());
        return zoomImg(bm, min, min, true);
    }

    public static Bitmap zoomImg(Bitmap bm, int newWidth, int newHeight, boolean isCircle) {
        int w = bm.getWidth();
        int h = bm.getHeight();
        int retX;
        int retY;
        double wh = (double) w / (double) h;
        double nwh = (double) newWidth / (double) newHeight;
        if (wh > nwh) {
            retX = h * newWidth / newHeight;
            retY = h;
        } else {
            retX = w;
            retY = w * newHeight / newWidth;
        }
        int startX = w > retX ? (w - retX) / 2 : 0;
        int startY = h > retY ? (h - retY) / 2 : 0;

        Bitmap bit = bm;
        try {
            if (!isCircle) {
                bit = Bitmap.createBitmap(bm, startX, startY, retX, retY, null, false);
            } else {
                bit = Bitmap.createBitmap(retX, retY, Bitmap.Config.ARGB_4444);
                Canvas canvas = new Canvas(bit);
                Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                paint.setColor(Color.WHITE);
                canvas.drawARGB(0, 0, 0, 0);
                float radius = Math.min(retX / 2, retY / 2);
                canvas.drawCircle(retX / 2, retY / 2, radius, paint);
                paint.reset();
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
                canvas.drawBitmap(bm, -startX, -startY, paint);
            }
        } catch (OutOfMemoryError error) {
            error.printStackTrace();
            return bit;
        }
//        bm.recycle();
        return bit;
    }
}
