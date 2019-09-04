package com.example.dangolite;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

@TargetApi(Build.VERSION_CODES.M)
public class FingerprintHandler extends FingerprintManager.AuthenticationCallback {

    private Context context;

    public FingerprintHandler(Context context){

        this.context = context;

    }

    public void startAuth(FingerprintManager fingerprintManager, FingerprintManager.CryptoObject cryptoObject){

        CancellationSignal cancellationSignal = new CancellationSignal();
        fingerprintManager.authenticate(cryptoObject, cancellationSignal, 0, this, null);

    }

    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {

        this.update("There was an Auth Error. " + errString, false);

    }

    @Override
    public void onAuthenticationFailed() {

        this.update("Auth Failed. ", false);

    }

    @Override
    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {

        this.update("Error: " + helpString, false);

    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        try{
            String information = MainActivity.Information;
            String ip = MainActivity.ip;
            String port = MainActivity.port;
            Log.e(" ip port value","ip:"+ip+"port:"+port+"information"+information);
            this.update("You can now access the app.", true);
            MessageSender messageSender = new MessageSender();
            messageSender.execute(ip,port);
            Log.v("messageSender","messageSender execute~");


        }catch (NumberFormatException e){

            this.update("Error:Please open QRcode Scanner for IPaddress",false);
        }



    }

    private void update(String s, boolean b) {

        TextView paraLabel =  ((Activity)context).findViewById(R.id.paraLabel);
        ImageView imageView =  (ImageView) ((Activity)context).findViewById(R.id.fingerprintimage);

        paraLabel.setText(s);


        if(b == false){

            paraLabel.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));

        } else {

            paraLabel.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
            imageView.setImageResource(R.drawable.action_done);

        }

    }

}


