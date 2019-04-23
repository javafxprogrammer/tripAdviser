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
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
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
import java.util.HashMap;
import java.util.Map;

public class AddCompany extends AppCompatActivity {

    public static AddCompany addCompany;
    private TextInputLayout companyNameParent, companyDescriptionParent, companyPhoneParent, companyEmailParent, companyWebsiteParent;
    private TextInputEditText companyNameChild, companyDescriptionChild, companyPhoneChild, companyEmailChild, companyWebsiteChild;
    private static Bitmap imageBitmap;
    private ProgressDialog progressDialog;
    private static final int CAMERA_PERMISSINS_REQUEST_CODE = 1;
    private Integer REQUEST_CAMERA = 2, SELECT_FILE = 3;
    private Utility utility;
    private String image = "";
    private SharedPreferenceManager userPref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_company);
        this.addCompany = this;
        userPref = SharedPreferenceManager.getInstance(AddCompany.this);
        if (!userPref.isLoggedIn()){
            finish();
            startActivity(new Intent(getApplicationContext(), Company.class));
            return;
        }

        companyNameParent = (TextInputLayout) findViewById(R.id.companyNameParent);
        companyDescriptionParent = (TextInputLayout) findViewById(R.id.companyDescriptionParent);
        companyPhoneParent = (TextInputLayout) findViewById(R.id.companyPhoneParent);
        companyEmailParent = (TextInputLayout) findViewById(R.id.companyEmailParent);
        companyWebsiteParent = (TextInputLayout) findViewById(R.id.companyWebsiteParent);

        companyNameChild = (TextInputEditText) findViewById(R.id.companyNameChild);
        companyDescriptionChild = (TextInputEditText) findViewById(R.id.companyDescriptionChild);
        companyPhoneChild = (TextInputEditText) findViewById(R.id.companyPhoneChild);
        companyEmailChild = (TextInputEditText) findViewById(R.id.companyEmailChild);
        companyWebsiteChild = (TextInputEditText) findViewById(R.id.companyWebsiteChild);

        progressDialog = new ProgressDialog(this);
        utility = Utility.getInstance(AddCompany.this);
    }



    public void addCompany(View view) {
        final String name = companyNameChild.getText().toString().trim();
        final String description = companyDescriptionChild.getText().toString().trim();
        final String phone = companyPhoneChild.getText().toString().trim();
        final String email = companyEmailChild.getText().toString().trim();
        final String website = companyWebsiteChild.getText().toString().trim();

        boolean validPhone = Utility.isValidPhone(phone);
        boolean validEmail = Utility.isValidEmail(email);
        boolean validUrl = Utility.isValidUrl(website);

        if (name.isEmpty()) {
            companyNameParent.setErrorEnabled(true);
            companyNameParent.setError("Please enter your company name");
        } else if (!name.isEmpty()) {
            companyNameParent.setErrorEnabled(false);
        }
        if (description.isEmpty()) {
            companyDescriptionParent.setErrorEnabled(true);
            companyDescriptionParent.setError("Please enter your company description");
        } else if (!description.isEmpty()) {
            companyDescriptionParent.setErrorEnabled(false);
        }
        if (phone.isEmpty()) {
            companyPhoneParent.setErrorEnabled(true);
            companyPhoneParent.setError("Please enter your company phone number");
        } else if (!phone.isEmpty()) {
            companyPhoneParent.setErrorEnabled(false);
        }

        if (email.isEmpty()) {
            companyEmailParent.setErrorEnabled(true);
            companyEmailParent.setError("Please enter your company email");
        } else if (!email.isEmpty()) {
            companyEmailParent.setErrorEnabled(false);
        }

        if (!website.isEmpty()) {
            companyWebsiteParent.setErrorEnabled(true);
            companyWebsiteParent.setError("Please enter your company website");
        } else if (website.isEmpty()) {
            companyWebsiteParent.setErrorEnabled(false);
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

//        if (imageBitmap != null) {
//            image = utility.bitmapToString(imageBitmap);
//        }

        if (!name.isEmpty() && !description.isEmpty() && !phone.isEmpty() && !email.isEmpty()
                && !website.isEmpty() && validPhone && validEmail && validUrl){
            progressDialog.setMessage("Registering company...");
            progressDialog.show();

            StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.COMPANY_URL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            progressDialog.dismiss();
                            Log.d("AddCompany_json_onRresponse", "["+response+"]");
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                if (jsonObject.getBoolean("error") == false){
//                                    finish();
//                                    startActivity(new Intent(getApplicationContext(), Company.class));
                                    //added
                                    if (userPref.logOut()){
                                        finish();
                                        startActivity(new Intent(getApplicationContext(), LogIn.class));
                                    }

                                } else {
                                    Toast.makeText(AddCompany.this, "failed", Toast.LENGTH_SHORT).show();
//                                    Log.d(TAG+"")
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
                            Log.d("AddCompany_json_onErrorResponse", "["+volleyError.getMessage()+"]");
                            Toast.makeText(AddCompany.this, volleyError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }){
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("userID", String.valueOf(userPref.getUserId()));
                    params.put("name", name);
                    params.put("image", image);
                    params.put("description", description);
                    params.put("phone", phone);
                    params.put("email", email);
                    params.put("website", website);
                    return params;
                }
            };
            RequestHandler.getInstance(this).addToRequestQueue(stringRequest);
        }
    }

    public void openCompanyActivity(View view) {
        finish();
        startActivity(new Intent(getApplicationContext(), Company.class));
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
                image = utility.bitmapToString(imageBitmap);

            } else if (requestCode == SELECT_FILE) {
                Uri path = data.getData();
                try {
                    imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), path);
                    image = utility.bitmapToString(imageBitmap);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
