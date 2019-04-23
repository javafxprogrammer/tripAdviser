package com.lengwemushimba.intercityticketbooking.helper;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

/**
 * Created by lengwe on 7/19/18.
 */

public class BitmaptHelper extends AsyncTask<String, Void, Bitmap> {

    private ImageView imageView;

    public BitmaptHelper(ImageView imageView) {
        this.imageView = imageView;
    }

    @Override
    protected Bitmap doInBackground(String... strings) {
        return ImageHttpHelper.compressBitmap(strings[0], Integer.parseInt(strings[1]), Integer.parseInt(strings[2]));
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        imageView.setImageBitmap(bitmap);
    }
}