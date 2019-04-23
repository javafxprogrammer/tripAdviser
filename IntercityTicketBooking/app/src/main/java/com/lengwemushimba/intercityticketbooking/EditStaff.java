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
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditStaff extends AppCompatActivity {

    @BindView(R.id.firstNameParent)
    TextInputLayout firstNameParent;
    @BindView(R.id.lastNameParent)
    TextInputLayout lastNameParent;
    @BindView(R.id.emailParent)
    TextInputLayout emailParent;
    @BindView(R.id.phoneParent)
    TextInputLayout phoneParent;
    @BindView(R.id.passwordParent)
    TextInputLayout passwordParent;
    @BindView(R.id.jobParent)
    TextInputLayout jobParent;

    @BindView(R.id.firstNameChild)
    TextInputEditText firstNameChild;
    @BindView(R.id.lastNameChild)
    TextInputEditText lastNameChild;
    @BindView(R.id.emailChild)
    TextInputEditText emailChild;
    @BindView(R.id.phoneChild)
    TextInputEditText phoneChild;
    @BindView(R.id.passwordChild)
    TextInputEditText passwordChild;
    @BindView(R.id.jobChild)
    TextInputEditText jobChild;

    private static final String TAG = EditStaff.class.getSimpleName();
    private SharedPreferenceManager userPref;
    private Utility utility;
    private ProgressDialog progressDialog;
    private static final int CAMERA_PERMISSINS_REQUEST_CODE = 1;
    private Integer REQUEST_CAMERA = 2, SELECT_FILE = 3;
    private static Bitmap imageBitmap;
    private String staffID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_staff);
        ButterKnife.bind(this);

        if (getIntent().getExtras() != null) {
            Bundle bundle = getIntent().getExtras();
            this.staffID = bundle.getString("staffID");
        }

        userPref = SharedPreferenceManager.getInstance(EditStaff.this);

        if (!userPref.isLoggedIn()) {
            finish();
            startActivity(new Intent(getApplicationContext(), SignUp.class));
            return;
        }

        utility = Utility.getInstance(this);
        progressDialog = new ProgressDialog(this);
    }

    public void signUp(View view) {
        final String firstName = firstNameChild.getText().toString().trim();
        final String lastName = lastNameChild.getText().toString().trim();
        final String email = emailChild.getText().toString().trim();
        final String phone = phoneChild.getText().toString().trim();
        final String password = passwordChild.getText().toString().trim(); // TODO: 5/14/18 encypt password
        final String job = jobChild.getText().toString().trim();

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
        if (job.isEmpty()) {
            jobParent.setErrorEnabled(true);
            jobParent.setError("Please enter staff job");
        } else if (!password.isEmpty()) {
            jobParent.setErrorEnabled(false);
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
                && !password.isEmpty() && !job.isEmpty() && validPhone && validEmail && validPassword) {
            progressDialog.setMessage("Editing Staff...");
            progressDialog.show();
            StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.STAFF_URL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            progressDialog.dismiss();
                            try {
                                Log.d(TAG + " onResponse", response);
                                JSONObject jsonObject = new JSONObject(response);

                                if (!jsonObject.getBoolean("error")) {
                                    Intent intent = new Intent(EditStaff.this, ViewStaff.class);
                                    intent.putExtra("companyID", StaticVar.map.get("companyID"));
                                    intent.putExtra("companyName", StaticVar.map.get("companyName"));
                                    intent.putExtra("companyImage", StaticVar.map.get("companyImage"));
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(EditStaff.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(EditStaff.this, volleyError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("userID_edit", staffID);
                    params.put("job_edit", job);
                    params.put("fname_edit", firstName);
                    params.put("lname_edit", lastName);
                    params.put("email_edit", email);
                    params.put("phone_edit", phone);
                    params.put("password_edit", password);
                    params.put("image_edit", utility.bitmapToString(imageBitmap));
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
