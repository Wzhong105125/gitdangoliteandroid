package com.example.dangolite;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.provider.Settings;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.Bundle;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import androidx.core.content.ContextCompat;
import android.system.Os;



public class MainActivity extends AppCompatActivity {
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.Scanner:
            {
                Intent goScanner = new Intent();
                goScanner.setClass(MainActivity.this , Main2Activity.class);
                startActivity(goScanner);
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private TextView mHeadingLabel;
    private ImageView mFingerprintImage;
    public TextView mParaLabel;
    public  static String  ip,port;
    public static String Information,Read,publickey;
    static ObservableString newread = new ObservableString();

    private FingerprintManager fingerprintManager;
    private KeyguardManager keyguardManager;

    private KeyStore keyStore;
    private Cipher cipher;
    private String KEY_NAME = "AndroidKey";


    private String encryptedread,decryptedread;

    private static final String File_Name = "Example.txt";

 //   public String   android_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == Activity.RESULT_OK) {
            Information = data.getStringExtra("Information");
            ip = data.getStringExtra("ip");
            port = data.getStringExtra("port");

        }

        for (int toasttime=0; toasttime < 3; toasttime++)
        {
            Toast.makeText(this, "Information"+Information+"ip:"+ip+" port:"+port, Toast.LENGTH_LONG).show();
        }



        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHeadingLabel = findViewById(R.id.headingLabel);
        mFingerprintImage =  findViewById(R.id.fingerprintimage);
        mParaLabel = findViewById(R.id.paraLabel);
        Intent goScanner = new Intent();
        goScanner.setClass(MainActivity.this , Main2Activity.class);
        startActivity(goScanner);
        newread.set(null);
        newread.setOnStringChangeListener(new OnStringChangeListener() {
            @Override
            public void onStringChanged(String newStringvalue) {
                Log.v("ONStringChanged","EZ"+newStringvalue);
                File file = new File(MainActivity.this.getFilesDir(),"StoreKey");
                if(!file.exists())
                    file.mkdir();
                if(Information == "C"){
                    try {
                        File gpxfile = new File(file,"publickey");
                        FileWriter fw = new FileWriter(gpxfile);
                        fw.append(Read);
                        fw.flush();
                        fw.close();
                        Log.v("file writer","write file"+Read);
                    }catch (IOException e) {
                        e.printStackTrace();
                    }
                }else{
                    try {
                        File gpxfile = new File(file,"publickey");
                        StringBuilder text = new StringBuilder();
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        String line;
                        while ((line = br.readLine()) != null) {
                            text.append(line);
                            text.append('\n');
                        }
                        publickey = text.toString();
                        Log.v("read file","read"+publickey);
                        EncryptRead();
                        br.close();
                    }catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


            }
        });


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);
            keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);

            if(!fingerprintManager.isHardwareDetected()){

                mParaLabel.setText("Fingerprint Scanner not detected in Device");

            } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED){

                mParaLabel.setText("Permission not granted to use Fingerprint Scanner");

            } else if (!keyguardManager.isKeyguardSecure()){

                mParaLabel.setText("Add Lock to your Phone in Settings");

            } else if (!fingerprintManager.hasEnrolledFingerprints()){

                mParaLabel.setText("You should add atleast 1 Fingerprint to use this Feature");

            } else {

                mParaLabel.setText("Place your Finger on Scanner to Access the App");

                generateKey();

                if (cipherInit()){

                    FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(cipher);
                    FingerprintHandler fingerprintHandler = new FingerprintHandler(this);
                    fingerprintHandler.startAuth(fingerprintManager, cryptoObject);

                }
            }
        }


    }

    private void EncryptRead() {

    }


    @TargetApi(Build.VERSION_CODES.M)
    private void generateKey() {

        try {

            keyStore = KeyStore.getInstance("AndroidKeyStore");
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

            keyStore.load(null);
            keyGenerator.init(new
                    KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                            KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            keyGenerator.generateKey();

        } catch (KeyStoreException | IOException | CertificateException
                | NoSuchAlgorithmException | InvalidAlgorithmParameterException
                | NoSuchProviderException e) {

            e.printStackTrace();

        }

    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean cipherInit() {
        try {
            cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get Cipher", e);
        }


        try {

            keyStore.load(null);

            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME,
                    null);

            cipher.init(Cipher.ENCRYPT_MODE, key);

            return true;

        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }

    }

    public interface OnStringChangeListener{
        public void onStringChanged(String stringvalue);
    }

    public static class ObservableString{
        private OnStringChangeListener listener;
        private String value;
        public void setOnStringChangeListener(OnStringChangeListener listener)
        {
            this.listener = listener;
        }

        public String get()
        {
            return value;
        }

        public void set(String value)
        {
            this.value = value;

            if(listener != null)
            {
                listener.onStringChanged(value);
            }
        }

    }




}
