package com.lengwemushimba.intercityticketbooking;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditCompany extends AppCompatActivity {

    @BindView(R.id.companyNameParent)
    TextInputLayout companyNameParent;
    @BindView(R.id.companyDescriptionParent)
    TextInputLayout companyDescriptionParent;
    @BindView(R.id.companyPhoneParent)
    TextInputLayout companyPhoneParent;
    @BindView(R.id.companyEmailParent)
    TextInputLayout companyEmailParent;
    @BindView(R.id.companyWebsiteParent)
    TextInputLayout companyWebsiteParent;

    @BindView(R.id.companyNameChild)
    TextInputEditText companyNameChild;
    @BindView(R.id.companyDescriptionChild)
    TextInputEditText companyDescriptionChild;
    @BindView(R.id.companyPhoneChild)
    TextInputEditText companyPhoneChild;
    @BindView(R.id.companyEmailChild)
    TextInputEditText companyEmailChild;
    @BindView(R.id.companyWebsiteChild)
    TextInputEditText companyWebsiteChild;

    private static final String TAG = EditCompany.class.getSimpleName();

    private static Bitmap imageBitmap;
    private ProgressDialog progressDialog;
    private static final int CAMERA_PERMISSINS_REQUEST_CODE = 1;
    private Integer REQUEST_CAMERA = 2, SELECT_FILE = 3;
    private Utility utility;
    private String companyID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_company);
        ButterKnife.bind(this);

        if (getIntent().hasExtra("CompanyID")){
            this.companyID = getIntent().getExtras().getString("CompanyID");
        }

        progressDialog = new ProgressDialog(this);
        utility = Utility.getInstance(this);

    }

    public void editCompany(View view) {

        final String companyName = companyNameChild.getText().toString().trim();
        final String companyDesc = companyDescriptionChild.getText().toString().trim();
        final String companyPhone = companyPhoneChild.getText().toString().trim();
        final String companyEmail = companyEmailChild.getText().toString().trim();
        final String companyWebsite = companyWebsiteChild.getText().toString().trim();

        boolean validPhone = Utility.isValidPhone(companyPhone);
        boolean validEmail = Utility.isValidEmail(companyEmail);
        boolean validUrl = Utility.isValidUrl(companyWebsite);

        if (companyName.isEmpty()){
            companyNameParent.setErrorEnabled(true);
            companyNameParent.setError("Please enter company name");
        }else {
            companyNameParent.setErrorEnabled(false);
        }
        if (companyDesc.isEmpty()){
            companyDescriptionParent.setErrorEnabled(true);
            companyDescriptionParent.setError("Please enter company description");
        }else {
            companyDescriptionParent.setErrorEnabled(false);
        }
        if (companyPhone.isEmpty()){
            companyPhoneParent.setErrorEnabled(true);
            companyPhoneParent.setError("Please enter company phone");
        }else {
            companyPhoneParent.setErrorEnabled(false);
        }
        if (companyEmail.isEmpty()){
            companyEmailParent.setErrorEnabled(true);
            companyEmailParent.setError("Please enter company email");
        }else {
            companyEmailParent.setErrorEnabled(false);
        }

        if (!validPhone) {
            companyPhoneParent.setErrorEnabled(true);
            companyPhoneParent.setError("Invalid Phone Number");
        } else if (validPhone) {
            companyPhoneParent.setErrorEnabled(false);
        }
        if (!validEmail) {
            companyEmailParent.setErrorEnabled(true);
            companyEmailParent.setError("Invalid Email Address");
        } else if (validEmail) {
            companyEmailParent.setErrorEnabled(false);
        }
        if (!validUrl) {
            companyWebsiteParent.setErrorEnabled(true);
            companyWebsiteParent.setError("Invalid Website Name");
        } else if (validUrl) {
            companyWebsiteParent.setErrorEnabled(false);
        }


        if (!companyName.isEmpty() && !companyDesc.isEmpty() && !companyPhone.isEmpty()
                && !companyEmail.isEmpty() && validPhone && validEmail && validUrl) {

            progressDialog.setMessage("Editing company details...");
            progressDialog.show();

            StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.COMPANY_URL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            progressDialog.dismiss();
                            Log.d(TAG+" onResponse", response);
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                if (jsonObject.getBoolean("error") == false){
                                    finish();
                                    startActivity(new Intent(getApplicationContext(), Company.class));
                                } else {
                                    Toast.makeText(EditCompany.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            progressDialog.hide();
                            Log.d(TAG+" onErrorResponse", volleyError.getMessage());
                            Toast.makeText(EditCompany.this, volleyError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }){
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("companyID", companyID);
                    params.put("image", utility.bitmapToString(imageBitmap));
                    params.put("name", companyName);
                    params.put("desc", companyDesc);
                    params.put("phone", companyPhone);
                    params.put("email", companyEmail);
                    params.put("website", companyWebsite);
                    return params;
                }
            };

            RequestHandler.getInstance(this).addToRequestQueue(stringRequest);
        }

        }

    public void setCompanyProfilePicture(View view) {
        selectImage();
    }

    public void selectImage() {

        final CharSequence[] items = {"Camera", "Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Image");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {

                if (items[i].equals("Camera")) {
                    if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        invokeCamera();
                    } else {
                        String[] permissinRequest = {Manifest.permission.CAMERA};
                        requestPermissions(permissinRequest, CAMERA_PERMISSINS_REQUEST_CODE);
                    }

                } else if (items[i].equals("Gallery")) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(intent, SELECT_FILE);

                } else if (items[i].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void invokeCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSINS_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                invokeCamera();
            } else {
                Toast.makeText(this, "Cannot take photo without permisions", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {

            if (requestCode == REQUEST_CAMERA) {
                Bundle bundle = data.getExtras();
                imageBitmap = (Bitmap) bundle.get("data");
                Uri uri = utility.getUriFromBitmap(imageBitmap);
                Log.d("imageURI_cam: ", uri.toString());
                Log.d("imageBitmap_cam: ", utility.bitmapToString(imageBitmap));

            } else if (requestCode == SELECT_FILE) {
                Uri path = data.getData();
                try {
                    imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), path);
                    Log.d("imageURI_file: ", path.toString());
                    Log.d("imageBitmap_file: ", utility.bitmapToString(imageBitmap));

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
