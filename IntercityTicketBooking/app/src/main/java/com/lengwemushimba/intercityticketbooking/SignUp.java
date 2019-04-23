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
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SignUp extends AppCompatActivity {

    private static final String TAG = SignUp.class.getSimpleName();

    private TextInputLayout firstNameParent, lastNameParent, emailParent, phoneParent, passwordParent;
    private TextInputEditText firstNameChild, lastNameChild, emailChild, phoneChild, passwordChild;
    private ProgressDialog progressDialog;
    private static final int CAMERA_PERMISSINS_REQUEST_CODE = 1;
    private Integer REQUEST_CAMERA = 2, SELECT_FILE = 3;
    private static Bitmap imageBitmap;
    private Utility utility;
    private String image = "";

    private SharedPreferenceManager userPref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);

        userPref = SharedPreferenceManager.getInstance(SignUp.this);

        if (userPref.isLoggedIn()) {
            finish();
            startActivity(new Intent(getApplicationContext(), Company.class));
            return;
        }

        firstNameParent = (TextInputLayout) findViewById(R.id.firstNameParent);
        lastNameParent = (TextInputLayout) findViewById(R.id.lastNameParent);
        emailParent = (TextInputLayout) findViewById(R.id.emailParent);
        phoneParent = (TextInputLayout) findViewById(R.id.phoneParent);
        passwordParent = (TextInputLayout) findViewById(R.id.passwordParent);

        firstNameChild = (TextInputEditText) findViewById(R.id.firstNameChild);
        lastNameChild = (TextInputEditText) findViewById(R.id.lastNameChild);
        emailChild = (TextInputEditText) findViewById(R.id.emailChild);
        phoneChild = (TextInputEditText) findViewById(R.id.phoneChild);
        passwordChild = (TextInputEditText) findViewById(R.id.passwordChild);

        progressDialog = new ProgressDialog(this);
        utility = Utility.getInstance(SignUp.this);
    }

    public void openLogInActivity(View view) {
        Intent intent = new Intent(getApplicationContext(), LogIn.class);
        startActivity(intent);
    }


    public void signUp(View view) {
        final String firstName = firstNameChild.getText().toString().trim();
        final String lastName = lastNameChild.getText().toString().trim();
        final String email = emailChild.getText().toString().trim();
        final String phone = phoneChild.getText().toString().trim();
        final String password = passwordChild.getText().toString().trim(); // TODO: 5/14/18 encypt password

        boolean validPhone = Utility.isValidPhone(phone);
        boolean validEmail = Utility.isValidEmail(email);
        boolean validPassword = Utility.isValidPassword(password);

        if (firstName.isEmpty()) {
            firstNameParent.setErrorEnabled(true);
            firstNameParent.setError("Please enter your first name");
        } else if (!firstName.isEmpty()) {
            firstNameParent.setErrorEnabled(false);
        }
        if (lastName.isEmpty()) {
            lastNameParent.setErrorEnabled(true);
            lastNameParent.setError("Please enter your last name");
        } else if (!lastName.isEmpty()) {
            lastNameParent.setErrorEnabled(false);
        }
        if (email.isEmpty()) {
            emailParent.setErrorEnabled(true);
            emailParent.setError("Please enter your email");
        } else if (!email.isEmpty()) {
            emailParent.setErrorEnabled(false);
        }
        if (phone.isEmpty()) {
            phoneParent.setErrorEnabled(true);
            phoneParent.setError("Please enter your phone number");
        } else if (!phone.isEmpty()) {
            phoneParent.setErrorEnabled(false);
        }
        if (password.isEmpty()) {
            passwordParent.setErrorEnabled(true);
            passwordParent.setError("Please enter your password");
        } else if (!password.isEmpty()) {
            passwordParent.setErrorEnabled(false);
        }

        if (!validPhone) {
            phoneParent.setErrorEnabled(true);
            phoneParent.setError("Invalid Phone Number");
        } else if (validPhone) {
            phoneParent.setErrorEnabled(false);
        } if (!validEmail) {
            emailParent.setErrorEnabled(true);
            emailParent.setError("Invalid Email Address");
        } else if (validEmail) {
            emailParent.setErrorEnabled(false);
        } if (!validPassword) {
            passwordParent.setErrorEnabled(true);
            passwordParent.setError("Password must alteast contain 6 characters");
        } else if (validPassword) {
            passwordParent.setErrorEnabled(false);
        }

        if (!firstName.isEmpty() && !lastName.isEmpty() && !email.isEmpty() && !phone.isEmpty()
                && !password.isEmpty() && validPhone && validEmail && validPassword) {

            Log.d(TAG + " datax", " " + firstName + ", " + lastName + ", " + email + ", " + phone + ", " + password + ", " + image);

            progressDialog.setMessage("SignUp processing...");
            progressDialog.show();
            StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.SIGNUP_URL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            progressDialog.dismiss();
                            try {
                                Log.d("Signup_json_response", "[" + response + "]");
                                JSONObject jsonObject = new JSONObject(response);

                                if (jsonObject.getBoolean("error") == false) {
                                    startActivity(new Intent(getApplicationContext(), LogIn.class));
                                } else {
                                    Toast.makeText(SignUp.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(SignUp.this, volleyError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("firstName", firstName);
                    params.put("lastName", lastName);
                    params.put("email", email);
                    params.put("phone", phone);
                    params.put("password", password);
                    params.put("profilePicture", image);
                    return params;
                }
            };
            RequestHandler.getInstance(this).addToRequestQueue(stringRequest);
        }
    }

    public void setProfilePicture(View view) {
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
