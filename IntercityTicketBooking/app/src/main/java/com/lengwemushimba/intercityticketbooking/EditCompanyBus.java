package com.lengwemushimba.intercityticketbooking;

import android.app.ProgressDialog;
import android.content.Intent;
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

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditCompanyBus extends AppCompatActivity {


    @BindView(R.id.busNameParent)
    TextInputLayout busNameParent;
    @BindView(R.id.busSeatsParent)
    TextInputLayout busSeatsParent;
    @BindView(R.id.busDecriptionParent)
    TextInputLayout busDecriptionParent;

    @BindView(R.id.busNameChild)
    TextInputEditText busNameChild;
    @BindView(R.id.busSeatsChild)
    TextInputEditText busSeatsChild;
    @BindView(R.id.busDecriptionChild)
    TextInputEditText busDecriptionChild;

    private static final String TAG = EditCompanyBus.class.getSimpleName();
    private String busID;
    private String companyID;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_company_bus);
        ButterKnife.bind(this);

        progressDialog = new ProgressDialog(this);

        if (getIntent().hasExtra("busID") && getIntent().hasExtra("companyID")) {
            this.busID = getIntent().getExtras().getString("busID");
            this.companyID = getIntent().getExtras().getString("companyID");
        }
    }

    public void editCompanyBus(View view) {

        final String busName = busNameChild.getText().toString().trim();
        final String busSeatCount = busSeatsChild.getText().toString().trim();
        final String busDesc = busDecriptionChild.getText().toString().trim();

        if (busName.isEmpty()) {
            busNameParent.setErrorEnabled(true);
            busNameParent.setError("Please enter bus name");
        } else {
            busNameParent.setErrorEnabled(false);
        }
        if (busSeatCount.isEmpty()) {
            busSeatsParent.setErrorEnabled(true);
            busSeatsParent.setError("Please enter number of seats");
        } else {
            busSeatsParent.setErrorEnabled(false);
        }
        if (busDesc.isEmpty()) {
            busDecriptionParent.setErrorEnabled(true);
            busDecriptionParent.setError("Please enter bus description");
        } else {
            busDecriptionParent.setErrorEnabled(false);
        }

        if (!busName.isEmpty() && !busSeatCount.isEmpty() && !busDesc.isEmpty()) {
            progressDialog.setMessage("Editing bus details...");
            progressDialog.show();

            StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.BUS_URL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            progressDialog.dismiss();
                            Log.d(TAG + " onResponse", response);
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                if (!jsonObject.getBoolean("error")) {
                                    Intent intent = new Intent(EditCompanyBus.this, CompanyBus.class);
                                    intent.putExtra("companyID", StaticVar.map.get("companyID"));
                                    intent.putExtra("companyName", StaticVar.map.get("companyName"));
                                    intent.putExtra("companyImage", StaticVar.map.get("companyImage"));
                                    startActivity(intent);
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
                            Log.d(TAG + " onErrorResponse", volleyError.getMessage());
                            Toast.makeText(EditCompanyBus.this, volleyError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("busID_editBus", busID);
                    params.put("busName_editBus", busName);
                    params.put("busSeatCount_editBus", busSeatCount);
                    params.put("busDescription_editBus", busDesc);
                    return params;
                }
            };
            RequestHandler.getInstance(this).addToRequestQueue(stringRequest);

        }

    }
}
