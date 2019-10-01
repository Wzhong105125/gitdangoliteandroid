package com.example.dangolite;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.icu.util.RangeValueIterator;
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
import java.io.ByteArrayInputStream;
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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import androidx.core.content.ContextCompat;
import android.system.Os;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;


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
    public  static String  ip,port,privatekey,testprivatekey;
    public static String Information,Read,publickey,filename,random,hashrandom,encryptedrandom,sendmessage,signrandom;
    static ObservableString newread = new ObservableString();
    static ObservableString newInformation = new ObservableString();

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
        newInformation.set(null);
        newInformation.setOnStringChangeListener(new OnStringChangeListener() {
            @Override
            public void onStringChanged(String stringvalue) {
                Log.v("new information",stringvalue);
                if(stringvalue.equals("L")){
                    privatekey = getKeyStoreString(MainActivity.this);
                    Log.v("privatekey",privatekey);
                    Log.v("main test","information:"+Information+"  ip:"+ip+"  port:"+port+"  random:"+random);
     //               hashrandom = hash(random);
     //               encryptedrandom = encrypt(hashrandom);
     //               sendmessage = combine(random,encryptedrandom);
                    signrandom = sign(random);
                }
            }
        });
        Intent goScanner = new Intent();
        goScanner.setClass(MainActivity.this , Main2Activity.class);
        startActivity(goScanner);

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

    private String getKeyStoreString(Context context) {
        KeyStore keyStore;
        String recoveredSecret = "";
        String filesDirectory = context.getFilesDir().getAbsolutePath();
        String encryptedDataFilePath = filesDirectory +File.separator + filename;
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            SecretKey secretKey = (SecretKey)
                    keyStore.getKey("phrase", null);
            if (secretKey == null) throw new RuntimeException("secretKey is null");

            Cipher outCipher;
            outCipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            outCipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(
                    new byte[outCipher.getBlockSize()]));

            CipherInputStream cipherInputStream = new CipherInputStream(
                    new FileInputStream(encryptedDataFilePath), outCipher);
            byte[] roundTrippedBytes = new byte[1000]; //TODO: dynamically resize as we get more data
            int index = 0;
            int nextByte;
            while ((nextByte = cipherInputStream.read()) != -1) {
                roundTrippedBytes[index] = (byte) nextByte;
                index++;
            }
            recoveredSecret = new String(roundTrippedBytes, 0, index, "UTF-8");
            Log.e("tag", "round tripped string = " + recoveredSecret);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String cover = "<RSAKeyValue><Modulus>";
        recoveredSecret = cover+recoveredSecret.substring(21,recoveredSecret.length());
        Log.e("tah", "recovered: " + recoveredSecret);
        return recoveredSecret;
    }



    private String sign(String random) {
        byte[] cipherdata;
        String encryptedtmp;
        DocumentBuilder db = null;
        try {
            db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Element el = db.parse(new ByteArrayInputStream(privatekey.getBytes())).getDocumentElement();
            String[] names = {"Modulus", "Exponent", "D", "P", "Q", "DP", "DQ", "InverseQ"};
            BigInteger[] vals = new BigInteger [names.length];
            for( int i = 0; i < names.length; i++ ){
                String v = el.getElementsByTagName(names[i]).item(0).getTextContent();
                vals[i] = new BigInteger(1, Base64.decode(v,Base64.DEFAULT));
            }
            PrivateKey prikey = KeyFactory.getInstance("RSA").generatePrivate(new RSAPrivateCrtKeySpec(vals[0], vals[1], vals[2], vals[3], vals[4], vals[5], vals[6], vals[7]) );
            Signature s = Signature.getInstance("SHA256withRSA");
            s.initSign(prikey);
            s.update(random.getBytes());
            byte[] signature = s.sign();
            String result = Base64.encodeToString(signature, Base64.DEFAULT);
            Log.v("signdata",result);
            return result;
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }

        return "";
    }

    private String combine(String random, String encryptedrandom) {
        String concattmp = random+encryptedrandom;
        return concattmp;
    }

    private String encrypt(String hashrandom) {
        try {
            byte[] cipherdata;
            String encryptedtmp;
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Element el = db.parse(new ByteArrayInputStream(privatekey.getBytes())).getDocumentElement();
            String[] names = {"Modulus", "Exponent", "D", "P", "Q", "DP", "DQ", "InverseQ"};
            BigInteger[] vals = new BigInteger [names.length];
            for( int i = 0; i < names.length; i++ ){
                String v = el.getElementsByTagName(names[i]).item(0).getTextContent();
                vals[i] = new BigInteger(1, Base64.decode(v,Base64.DEFAULT));
            }
            PrivateKey prikey = KeyFactory.getInstance("RSA").generatePrivate(new RSAPrivateCrtKeySpec(vals[0], vals[1], vals[2], vals[3], vals[4], vals[5], vals[6], vals[7]) );
            Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            rsaCipher.init(Cipher.ENCRYPT_MODE, prikey);
            cipherdata = rsaCipher.doFinal(hashrandom.getBytes("UTF-8"));
            encryptedtmp =  new String(Base64.encode(cipherdata,Base64.DEFAULT ));
            Log.v("encrypted hashrandom:",encryptedtmp);
            return encryptedtmp;

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return "";
    }

    private String hash(String random) {
        try {
            MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            digest.update(random.getBytes(Charset.forName("US-ASCII")),0,random.length());
            byte[] magnitude = digest.digest();
            BigInteger bi = new BigInteger(1, magnitude);
            String hash = String.format("%0" + (magnitude.length << 1) + "x", bi);
            Log.v("hash random",hash);
            return hash;
        }catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        return "";
    }


    private String readprivateket() {
        File file = new File(MainActivity.this.getFilesDir(),"StoreKey");
        File gpxfile = new File(file,filename);
        StringBuilder text = new StringBuilder();
        BufferedReader br = null;
        String pp = null;
        try {
            br = new BufferedReader(new FileReader(gpxfile));
            String line;
            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            pp = text.toString();
            Log.v("read file","read"+pp);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return pp;
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
