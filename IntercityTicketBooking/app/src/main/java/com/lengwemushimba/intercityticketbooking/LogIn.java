package com.lengwemushimba.intercityticketbooking;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

import java.util.HashMap;
import java.util.Map;

public class LogIn extends AppCompatActivity {

    private static final String TAG = LogIn.class.getSimpleName();
    private TextInputLayout emailParent, passwordParent;
    private TextInputEditText email, password;
    private ProgressDialog progressDialog;
    private Button submit;
    private SharedPreferenceManager userPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        userPref = SharedPreferenceManager.getInstance(LogIn.this);

        if (userPref.isLoggedIn()){
            finish();
            startActivity(new Intent(getApplicationContext(), Company.class));
            return;
        }

        emailParent = (TextInputLayout) findViewById(R.id.userEmailParent);
        passwordParent = (TextInputLayout) findViewById(R.id.userPasswordParent);
        email = (TextInputEditText) findViewById(R.id.userEmailChild);
        password = (TextInputEditText) findViewById(R.id.userPasswordChild);
        submit = (Button) findViewById(R.id.submit);
        progressDialog = new ProgressDialog(this);

        if(getIntent().hasExtra("emailtmp") && getIntent().hasExtra("passwordtmp")){
            this.email.setText(getIntent().getExtras().getString("emailtmp", null));
            this.password.setText(getIntent().getExtras().getString("passwordtmp", null));
            this.submit.performClick();
        }
    }

    public void openSignUpActivity(View view) {
        Intent intent = new Intent(getApplicationContext(), SignUp.class);
        startActivity(intent);
    }

    public void logIn(View view) {
        final String email = this.email.getText().toString().trim();
        final String password = this.password.getText().toString().trim();

        if(email.isEmpty()){
            emailParent.setErrorEnabled(true);
            emailParent.setError("Please enter your email");
        } else if (!email.isEmpty()){
            emailParent.setErrorEnabled(false);
        }
        if (password.isEmpty()){
            passwordParent.setErrorEnabled(true);
            passwordParent.setError("Please enter your password");
        } else if (!password.isEmpty()){
            passwordParent.setErrorEnabled(false);
        }
        if (!email.isEmpty() && !password.isEmpty()){

            progressDialog.setMessage("LogIn processing...");
            progressDialog.show();
            StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.LOGIN_URL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            progressDialog.dismiss();
                            try {

                                Log.d("login_json_response", "["+response+"]");
                                JSONObject jsonObject = new JSONObject(response);

                                if(!jsonObject.getBoolean("error")){

                                    userPref.userLogIn(jsonObject.getInt(
                                            "userID"), jsonObject.getString("userType"), jsonObject.getString("userName"),
                                            jsonObject.getString("email"), jsonObject.getString("phone"),
                                            jsonObject.getString("companyID"), jsonObject.getString("profilePicture"));

                                    Log.d(TAG+" companyIDX", jsonObject.getString("companyID"));

                                    startActivity(new Intent(getApplicationContext(), Company.class));
                                    finish();
                                } else{
                                    Toast.makeText(LogIn.this, jsonObject.getString("message"), Toast.LENGTH_LONG).show();
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
                            Toast.makeText(LogIn.this, volleyError.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }){
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("email", email);
                    params.put("password", password);
                    return params;
                }

//                @Override
//                public Map<String, String> getHeaders() throws AuthFailureError {
//                    Map<String, String>  params = new HashMap<String, String>();
//                    params.put("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.135 Safari/537.36 Edge/12.10240 ");
//                    params.put("Cookie", "__test=21172a400f413127151fd92f61ac6f06; expires=Friday, January 1, 2038 at 1:55:55 AM; path=/");
//                    return params;
//                }
            };
            RequestHandler.getInstance(this).addToRequestQueue(stringRequest);
        }
    }
}
