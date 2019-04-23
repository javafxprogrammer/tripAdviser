package com.lengwemushimba.intercityticketbooking.utility;

import android.net.Uri;

import com.lengwemushimba.intercityticketbooking.R;

/**
 * Created by lengwe on 7/21/18.
 */

public class ImageUtil {

    public static String getURLForResource(int resourcesid){
        return Uri.parse("android.resource://"+R.class.getPackage().getName()+"/"+resourcesid).toString();
    }
}
