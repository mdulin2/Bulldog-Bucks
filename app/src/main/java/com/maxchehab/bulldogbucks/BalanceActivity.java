package com.maxchehab.bulldogbucks;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

public class BalanceActivity extends AppCompatActivity {

    SwipeRefreshLayout swipeContainer;
    @Bind(R.id.balanceText) TextView _balanceText;
    @Bind(R.id.textLogout) TextView _logoutText;
    @Bind(R.id.imageLogout) ImageView _logoutImage;


    Boolean retry = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance);
        ButterKnife.bind(this);


        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                updateBalance();
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(R.color.swipe);

        _logoutImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
        _logoutText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        updateBalance();
    }

    @Override
    protected void onResume() {
        // Runtime.getRuntime().gc();

        updateBalance();
        super.onResume();
    }


    void logout(){

        new AlertDialog.Builder(this,R.style.AlertDialogCustom)
                .setTitle("Logout")

                .setMessage("Do you really want to logout?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("yes", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        getBaseContext().getSharedPreferences("data", 0).edit().clear().commit();
                        Intent intent = new Intent(getBaseContext(), LoginActivity.class);
                        startActivity(intent);
                        finish();
                        Toast.makeText(BalanceActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
                    }})
                .setNegativeButton("no", null).show();

    }

    void updateBalance(){
        SharedPreferences sharedPref = getSharedPreferences("data", MODE_PRIVATE);

        final String pin = sharedPref.getString("pin", null);
        final String userID = sharedPref.getString("userID", null);

        if(!sharedPref.contains("pin") || !sharedPref.contains("userID")){
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://67.204.152.242/bulldogbucks/balance.php";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Response", response);
                        try{
                            JsonParser jp = new JsonParser(); //from gson
                            JsonElement root = jp.parse(response); //Convert the input stream to a json element

                            JsonObject rootobj = root.getAsJsonObject(); //May be an array, may be an object.

                            if(rootobj.get("success").getAsBoolean() == true) {
                                _balanceText.setText(rootobj.get("balance").getAsString());
                            }else{
                                Toast.makeText(getBaseContext(), "Retrieving balance failed", Toast.LENGTH_LONG).show();

                            }
                        }catch(Exception e){
                            if(retry){
                                logout();
                            }else{
                                retry = true;
                                updateBalance();
                            }
                        }

                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response", error.toString());
                        Toast.makeText(getBaseContext(), "Retrieving balance failed", Toast.LENGTH_LONG).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();



                params.put("pin", pin);
                params.put("userID", userID);

                return params;
            }
        };
        queue.add(postRequest);

        swipeContainer.setRefreshing(false);
    }
}
