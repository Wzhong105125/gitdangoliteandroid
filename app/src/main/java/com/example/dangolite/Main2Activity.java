package com.example.dangolite;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Calendar;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.security.auth.x500.X500Principal;

public class Main2Activity extends AppCompatActivity {

    String Information,ip,port,privatekey,filename,random;
    private static final String AndroidKeyStore = "AndroidKeyStore";
    private static final String AES_MODE = "AES/GCM/NoPadding";
    private KeyStore ks;
    String alias = "key3";

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        if (result!= null)
        {
            if (result.getContents()==null)
            {
                Toast.makeText(this, "You cancelled the scanning", Toast.LENGTH_SHORT).show();
                if(ip == null || port == null){

                    Toast.makeText(this,"QRcode Scan Failed",Toast.LENGTH_LONG).show();
                    Intent goScanner = new Intent();
                    goScanner.setClass(Main2Activity.this , Main2Activity.class);
                    startActivity(goScanner);

                }
                finish();
            }
            else
            {
           //     Toast.makeText(this,result.getContents(),Toast.LENGTH_SHORT).show();
                String ScanAnswer = result.getContents().toString();
                Information = ScanAnswer.substring(0,1);
                MainActivity.Information = Information;
                if(Information.equals("C") || Information.equals("c")){
                    int keystart = ScanAnswer.indexOf(":")+1;
                    int keyend = ScanAnswer.length();
                    filename = ScanAnswer.substring(1,6);
                    privatekey = ScanAnswer.substring(keystart,keyend);
                    MainActivity.privatekey = privatekey;
                    File file = new File(Main2Activity.this.getFilesDir(),"StoreKey");
                    File gpxfile = new File(file,filename);
                    FileWriter fw = null;
                    try {
                        fw = new FileWriter(gpxfile);
                        fw.append(privatekey);
                        fw.flush();
                        fw.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.v("file writer","write file"+privatekey);
                }else{
                    int ipstart = ScanAnswer.indexOf("ip:")+3;
                    int ipend = ScanAnswer.indexOf("port:");
                    int portstart = ScanAnswer.indexOf("port:")+5;
                    int portend = ScanAnswer.length();
                    filename = ScanAnswer.substring(1,6);
                    random = ScanAnswer.substring(ScanAnswer.indexOf("random:")+7,ScanAnswer.indexOf("random:")+23);
                    ip = ScanAnswer.substring(ipstart,ipend);
                    port = ScanAnswer.substring(portstart,portend);
                    MainActivity.ip = ip;
                    MainActivity.port = port;
                    MainActivity.filename = filename;
                    MainActivity.random = random;
         //           Log.v("test","information:"+Information+"  ip:"+ip+"  port:"+port+"  random:"+random);
                    Intent returnvalue  = getIntent();
                    returnvalue.putExtra("Information",Information);
                    returnvalue.putExtra("ip",ip);
                    returnvalue.putExtra("port",port);
                    setResult(Activity.RESULT_OK,returnvalue);
                    MainActivity.newInformation.set(Information);
                }
                finish();
            }
        }
        else
        {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        IntentIntegrator integrator = new IntentIntegrator(Main2Activity.this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setPrompt("掃描QRcode");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(false);
        integrator.setOrientationLocked(false);
        integrator.initiateScan();
    }


}
