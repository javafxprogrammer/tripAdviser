package com.lengwemushimba.intercityticketbooking.helper;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

/**
 * Created by lengwe on 7/26/18.
 */

public class ImageUploadHelper {

//   private static Context context;
//   private static ImageUploadHelper imageUploadHelper;
//    private static final int CAMERA_PERMISSINS_REQUEST_CODE = 1;
//    private Integer REQUEST_CAMERA = 2, SELECT_FILE = 3;
//
//    private ImageUploadHelper(Context context) {
//        this.context = context;
//    }
//
//    public static synchronized ImageUploadHelper getInstance(Context context){
//        if (imageUploadHelper == null){
//            imageUploadHelper = new ImageUploadHelper(context);
//            return imageUploadHelper;
//        }
//        ImageUploadHelper.context = context;
//        return imageUploadHelper;
//    }
//
//    public void selectImage() {
//
//        final CharSequence[] items = {"Camera", "Gallery", "Cancel"};
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        builder.setTitle("Add Image");
//        builder.setItems(items, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int i) {
//
//                if (items[i].equals("Camera")) {
//                    if (context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
//                        invokeCamera();
//                    } else {
//                        String[] permissinRequest = {Manifest.permission.CAMERA};
//                        ((Activity)context).requestPermissions(permissinRequest, CAMERA_PERMISSINS_REQUEST_CODE);
//                    }
//
//                } else if (items[i].equals("Gallery")) {
//                    Intent intent = new Intent();
//                    intent.setType("image/*");
//                    intent.setAction(Intent.ACTION_GET_CONTENT);
//                    ((Activity)context).startActivityForResult(intent, SELECT_FILE);
//
//                } else if (items[i].equals("Cancel")) {
//                    dialog.dismiss();
//                }
//            }
//        });
//        builder.show();
//    }
//
//    private void invokeCamera() {
//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        ((Activity)context).startActivityForResult(intent, REQUEST_CAMERA);
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == CAMERA_PERMISSINS_REQUEST_CODE) {
//            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                invokeCamera();
//            } else {
//                Toast.makeText(this, "Cannot take photo without permisions", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (resultCode == Activity.RESULT_OK && data != null) {
//
//            if (requestCode == REQUEST_CAMERA) {
//                Bundle bundle = data.getExtras();
//                imageBitmap = (Bitmap) bundle.get("data");
//                Uri uri = utility.getUriFromBitmap(imageBitmap);
//                Log.d("imageURI_cam: ", uri.toString());
//                Log.d("imageBitmap_cam: ", utility.bitmapToString(imageBitmap));
//
//            } else if (requestCode == SELECT_FILE) {
//                Uri path = data.getData();
//                try {
//                    imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), path);
//                    Log.d("imageURI_file: ", path.toString());
//                    Log.d("imageBitmap_file: ", utility.bitmapToString(imageBitmap));
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
}
