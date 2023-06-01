package it.italiandudes.mymcserver.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;
import java.util.logging.Logger;

import it.italiandudes.mymcserver.ConnectivitySingleton;
import it.italiandudes.mymcserver.Constants;
import it.italiandudes.mymcserver.R;
import it.italiandudes.mymcserver.inputfilters.MinMaxFilter;
import it.italiandudes.mymcserver.inputfilters.MinMaxStringLengthFilter;

public class LoginActivity extends Activity {

    private EditText ipEdTxt;
    private EditText portEdTxt;
    private EditText userEdTxt;
    private EditText pwdEdTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);  //This is a login form, so I can accept this solution
        setContentView(R.layout.activity_login);

        ipEdTxt = findViewById(R.id.login_form_ip);
        portEdTxt = findViewById(R.id.login_form_port);
        portEdTxt.setFilters(new InputFilter[]{new MinMaxFilter(0, Constants.Connectivity.MAX_PORT_NUM)});
        userEdTxt = findViewById(R.id.login_form_user);
        userEdTxt.setFilters(new InputFilter[]{new MinMaxStringLengthFilter(0, Constants.Protocol.General.MAX_USERNAME_MINECRAFT_LENGTH)});
        pwdEdTxt = findViewById(R.id.login_form_pwd);
    }

    /**
     * logIn handles the log in phase, the connection to the server and the activity swap.
     *
     * @param view identifies the android view that called the method.
     * */
    public void logIn(View view){

        new Thread(()->{

            String ipAddress=ipEdTxt.toString();
            int portNum = Integer.getInteger(portEdTxt.toString());
            String user = userEdTxt.toString();
            String pwd = pwdEdTxt.toString();

            ConnectivitySingleton.getInstance().setURL(ipAddress,portNum);
            ConnectivitySingleton.getInstance().setPath(Constants.Connectivity.LOGIN);
            ConnectivitySingleton.getInstance().setHTTPHeader(Constants.Protocol.HTTP_Headers.MMCS_USERNAME,user);
            ConnectivitySingleton.getInstance().setHTTPHeader(Constants.Protocol.HTTP_Headers.MMCS_PWD,pwd);

            try {
                ConnectivitySingleton.getInstance().executeQueryHTTP();
            } catch (IOException | JSONException e) {
                Log.d(Constants.Log.TAG,"IOException or JSONException thrown after calling Connectivity.getInstance().executeQueryHTTP() inside LoginActivity#logIn()");
                runOnUiThread(()->{
                    Toast.makeText(this,getString(R.string.string_error_login),Toast.LENGTH_LONG).show();
                });
            }

            try {
                if(ConnectivitySingleton.getInstance().getInteger(Constants.Protocol.HTTP_Headers.RETURN_CODE)==Constants.Protocol.ReturnCodes.HTTP_OK){
                    ConnectivitySingleton.getInstance().setToken(ConnectivitySingleton.getInstance().getString(Constants.Protocol.HTTP_Headers.TOKEN));
                    Log.d(Constants.Log.TAG,"HTTP code returned: "+ConnectivitySingleton.getInstance().getInteger(Constants.Protocol.HTTP_Headers.RETURN_CODE));
                    runOnUiThread(()->{
                        //TODO: lanciare tutte le operazioni necessarie per caricare il collegamento al server, e poi fare il login
                        Intent switchActivity = new Intent(this,TerminalActivity.class);
                        startActivity(switchActivity);
                    });
                }else{
                    //Other codes returned
                    Log.d(Constants.Log.TAG,"HTTP code returned: "+ConnectivitySingleton.getInstance().getInteger(Constants.Protocol.HTTP_Headers.RETURN_CODE));
                    runOnUiThread(()->{
                        Toast.makeText(this,getString(R.string.string_error_login),Toast.LENGTH_LONG).show();
                    });
                }
            } catch (JSONException e) {
                Log.d(Constants.Log.TAG,"JSONException thrown after calling ConnectivitySingleton.getInstance().getInteger() inside LoginActivity#logIn()");
                runOnUiThread(()->{
                    Toast.makeText(this,getString(R.string.string_error_login),Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
}