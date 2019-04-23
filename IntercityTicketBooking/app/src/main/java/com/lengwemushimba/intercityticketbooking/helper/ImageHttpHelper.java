package com.lengwemushimba.intercityticketbooking.helper;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by lengwe on 7/16/18.
 */

public class ImageHttpHelper {

    private static final String TAG = ImageHttpHelper.class.getSimpleName();


    public static Bitmap compressBitmap(String imageUrl, int reqWidth, int reqHeight){
        Log.d(TAG+" imageProperties", "imageUrl = "+imageUrl+", reqWidth = "+reqWidth+", reqHeight = "+reqHeight);

        try {
            URL url = new URL(imageUrl);
            InputStream is = (InputStream) url.getContent();
            return decodeSampledBitmapFromResource(new BufferedInputStream(is), reqWidth, reqHeight);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap decodeSampledBitmapFromResource(InputStream inputStream, int reqWidth,
                                                         int reqHeight) throws IOException {
        // First decode with inJustDecodeBounds=true to check dimensions
        inputStream.mark(inputStream.available());
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
//        inputStream.mark(inputStream.available());
        BitmapFactory.decodeStream(inputStream, null, options);
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        inputStream.reset();
        return BitmapFactory.decodeStream(inputStream, null, options);
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth,
                                                  int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    //Given the bitmap size and View size calculate a subsampling size (powers of 2)
    public static int calculateInSampleSize( BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int inSampleSize = 1;   //Default subsampling size
        // See if image raw height and width is bigger than that of required view
        if (options.outHeight > reqHeight || options.outWidth > reqWidth) {

            Log.d(TAG+" imageProperties2", "outWidth = "+options.outWidth+", outHeight = "+options.outHeight);

            //bigger
            final int halfHeight = options.outHeight / 2;
            final int halfWidth = options.outWidth / 2;
            inSampleSize = 4;
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        Log.d(TAG+" imageProperties3", "inSampleSize = "+inSampleSize+", reqWidth = "+reqWidth+", reqHeight = "+reqHeight);
        return inSampleSize;
    }
}
