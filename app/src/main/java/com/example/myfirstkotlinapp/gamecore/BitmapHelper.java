package com.example.myfirstkotlinapp.gamecore;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;

public class BitmapHelper {

    /**
     * Resize bitmap to given width and height.
     * @return The resized bitmap
     */
    public static Bitmap resizeBitmap(Resources resources, int id, int width, int height) {
        Bitmap bitmap = BitmapFactory.decodeResource(resources, id);

        RectF src = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
        RectF dst = new RectF(0, 0, width, height);
        Matrix matrix = new Matrix();
        matrix.setRectToRect(src, dst, Matrix.ScaleToFit.FILL);

        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0 , bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        bitmap.recycle();
        return resizedBitmap;
    }
}
