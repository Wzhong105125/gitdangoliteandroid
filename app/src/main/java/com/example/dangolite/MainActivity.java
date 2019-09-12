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
import android.os.SystemClock;
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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
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
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;

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


    public static String  encryptedread = null;
    public byte[] result;
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
                Log.v("information",Information);
                File file = new File(MainActivity.this.getFilesDir(),"StoreKey");
                if(!file.exists())
                    file.mkdir();
                if(Information.equals("C")){
                    try {
                        Log.v("get public",newStringvalue);
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
                        BufferedReader br = new BufferedReader(new FileReader(gpxfile));
                        String line;
                        while ((line = br.readLine()) != null) {
                            text.append(line);
                            text.append('\n');
                        }
                        publickey = text.toString();
                        Log.v("read file","read"+publickey);
                        EncryptRead();
                        br.close();
                        Information = "K";
                        SystemClock.sleep(1000);
                        MessageSender messageSender2 = new MessageSender();
                        messageSender2.execute(ip,port);
                        Log.v("message sender","execute twice");
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
        int modstart= publickey.indexOf("<Modulus>")+9;
        int modend = publickey.indexOf("</Modulus>");
        int exstart = publickey.indexOf("<Exponent>")+10;
        int exend = publickey.indexOf("</Exponent>");
        String mod = publickey.substring(modstart,modend);
        String ex = publickey.substring(exstart,exend);
        Log.v("mod",mod);
        Log.v("exponent",ex);
        BigInteger modulus = new BigInteger(1,Base64.decode(mod,Base64.DEFAULT));
        BigInteger exponent = new BigInteger(1,Base64.decode(ex,Base64.DEFAULT));
        PublicKey pubKey;
        byte[] cipherdata;
        try{
            pubKey = KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(modulus, exponent));
            Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            rsaCipher.init(Cipher.ENCRYPT_MODE, pubKey);
            cipherdata = rsaCipher.doFinal(Read.getBytes("UTF-8"));
            encryptedread =  new String(Base64.encode(cipherdata,Base64.DEFAULT ));
            result = cipherdata;
            Log.v("encrypted:",encryptedread);
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

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
