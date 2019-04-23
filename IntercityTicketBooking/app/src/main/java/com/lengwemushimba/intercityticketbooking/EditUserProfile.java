package com.lengwemushimba.intercityticketbooking;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.lengwemushimba.intercityticketbooking.helper.BitmaptHelper;
import com.lengwemushimba.intercityticketbooking.helper.BottomNavigationHelper;
import com.lengwemushimba.intercityticketbooking.helper.ImageHttpHelper;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditUserProfile extends AppCompatActivity {
    // TODO: 6/14/18 create viewProfile activity with edit fab at bottom

    @BindView(R.id.viewA)
    ImageView profilePic;
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

    @BindView(R.id.job)
    TextView job;

    @BindView(R.id.bnve)
    BottomNavigationViewEx bnve;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private static final String TAG = EditUserProfile.class.getSimpleName();
    private static final int CAMERA_PERMISSINS_REQUEST_CODE = 1;
    private static final Integer REQUEST_CAMERA = 2, SELECT_FILE = 3;
    private static Bitmap imageBitmap;
    private ProgressDialog progressDialog;
    private String userID;
    private Utility utility;
    private BottomNavigationHelper bottomNavigationHelper;

    private SharedPreferenceManager userPref;
    private String image = "";

    private Boolean imageToggle = false;
    private int xDim, yDim;

    //Get the size of the Image view after the Activity has completely loaded
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        xDim = profilePic.getWidth();
        yDim = profilePic.getHeight();

        Log.d(TAG+"xy-", "xDim = "+xDim+", yDim = "+yDim);

        if (this.profilePic.getDrawable() == null){
            new BitmaptHelper(profilePic).execute( userPref.getUserPicture(),
                    String.valueOf(xDim),
                    String.valueOf(yDim));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_user_profile);
        ButterKnife.bind(this);

        this.userPref = SharedPreferenceManager.getInstance(EditUserProfile.this);

        if (!userPref.isLoggedIn()) {
            Intent intent = new Intent(getApplicationContext(), LogIn.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return;
        }

        utility = Utility.getInstance(EditUserProfile.this);
        setSupportActionBar(toolbar);

        bottomNavigationHelper = BottomNavigationHelper.getInstance(EditUserProfile.this);
        bottomNavigationHelper.bottonNavSettings(bnve, R.id.profile);


        String[] names = userPref.getUserName().split(" ");

        this.firstNameChild.setText(names[0]);
        this.lastNameChild.setText(names[1]);
        this.emailChild.setText(userPref.getUserEmail());
        this.phoneChild.setText(userPref.getUserPhone());
        this.job.setText(userPref.getUserType());
//        userPref.get

//        if (profilePic.getDrawable() != null) {
//            ((BitmapDrawable) profilePic.getDrawable()).getBitmap().recycle();
//        }

//        xDim = profilePic.getWidth();
//        yDim = profilePic.getHeight();
//
//        Log.d(TAG+"xy-", "xDim = "+xDim+", yDim = "+yDim);

//        if (this.profilePic.getDrawable() == null){
//            new BitmaptHelper(this.profilePic).execute(
//                    userPref.getUserPicture(),
//                    String.valueOf(xDim),
//                    String.valueOf(yDim));
//        }



        Log.d(TAG + " profilePic", userPref.getUserPicture());

        this.userID = String.valueOf(userPref.getUserId());
        progressDialog = new ProgressDialog(EditUserProfile.this);
//        utility = Utility.ImageUploadHelper(EditUserProfile.this);
    }

    public void editProfile(View view) {
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
            passwordParent.setError("Invalid Password");
        } else if (validPassword) {
            passwordParent.setErrorEnabled(false);
        }

        if (!firstName.isEmpty() && !lastName.isEmpty() && !email.isEmpty() && !phone.isEmpty()
                && !password.isEmpty() && validPhone && validEmail && validPassword) {

            Log.d(TAG + " datax", userID + ", " + firstName + ", " + lastName + ", " + email + ", " + phone + ", " + password + ", " + image);

            progressDialog.setMessage("Editing profile...");
            progressDialog.show();
            StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.SIGNUP_URL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            progressDialog.dismiss();
                            Log.d(TAG + " onResponse", response);

                            try {
                                JSONObject jsonObject = new JSONObject(response);

                                if (!jsonObject.getBoolean("error")) {
                                    if (userPref.logOut()) {
                                        Toast.makeText(EditUserProfile.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(EditUserProfile.this, LogIn.class);
                                        intent.putExtra("emailtmp", email);
                                        intent.putExtra("passwordtmp", password);
                                        startActivity(intent);

                                    }

                                } else {
                                    Toast.makeText(EditUserProfile.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            progressDialog.hide();
                            Log.d(TAG + " onErrorResponse", error.getMessage());
                            Toast.makeText(EditUserProfile.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("userID_edit", userID);
                    params.put("firstName_edit", firstName);
                    params.put("lastName_edit", lastName);
                    params.put("email_edit", email);
                    params.put("phone_edit", phone);
                    params.put("password_edit", password);
                    params.put("profilePicture_edit", image);
                    return params;
                }
            };
            RequestHandler.getInstance(EditUserProfile.this).addToRequestQueue(stringRequest);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // TODO: 5/18/18 NOT HERE -> check userType and companyID, then inflate the correct menu, all menus must have same search
        getMenuInflater().inflate(R.menu.company_options_menu_toolbar, menu);
        menu.getItem(0).setVisible(false);
        menu.getItem(1).setVisible(false);
        menu.getItem(2).setVisible(false);
        menu.getItem(3).getSubMenu().getItem(1).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.logout:
                userPref.logOut();
                if (!userPref.isLoggedIn()) {
                    Intent intent = new Intent(getApplicationContext(), LogIn.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                break;
            case R.id.sort:
                // Actions here, launch dialog with options
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        profilePic.setImageDrawable(null);
    }

//    private class MyBitmaptHelper extends AsyncTask<String, Void, Bitmap> {
//
//
//        @Override
//        protected Bitmap doInBackground(String... strings) {
//            return ImageHttpHelper.compressBitmap(strings[0], Integer.parseInt(strings[1]), Integer.parseInt(strings[2]));
//        }
//
//        @Override
//        protected void onPostExecute(Bitmap bitmap) {
//            super.onPostExecute(bitmap);
//            profilePic.setImageBitmap(bitmap);
//        }}
}
