package com.example.lap10715.myandroidkeystore;

import android.security.KeyPairGeneratorSpec;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.security.auth.x500.X500Principal;


public class MainActivity extends AppCompatActivity {

    private KeyStore mKeyStore;
    private List<String> mKeyAliases = new ArrayList<>();
    private MyAdapter mMyAdapter;
    private EditText mEdtKeyAlias, mEdtInitText, mEdtEncrypt, mEdtDecrypt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView lvContainer = findViewById(R.id.lv_container);
        View listHeader = View.inflate(this, R.layout.activity_main_header, null);
        lvContainer.addHeaderView(listHeader);
        mEdtKeyAlias = listHeader.findViewById(R.id.edt_key_alias);
        mEdtInitText = listHeader.findViewById(R.id.edt_init_text);
        mEdtEncrypt = listHeader.findViewById(R.id.edt_encrypt_text);
        mEdtDecrypt = listHeader.findViewById(R.id.edt_decrypted_text);

        mMyAdapter = new MyAdapter(this, R.layout.list_item, mKeyAliases);
        lvContainer.setAdapter(mMyAdapter);


        try {
            mKeyStore = KeyStore.getInstance("AndroidKeyStore");
            mKeyStore.load(null);
            refreshKeys();


        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void refreshKeys(){
        mKeyAliases.clear();
        try {
            Enumeration<String> aliases = mKeyStore.aliases();

            while (aliases.hasMoreElements()){
                mKeyAliases.add(aliases.nextElement());
            }

        } catch (KeyStoreException e) {
            e.printStackTrace();
        }

        if(mMyAdapter != null){
            mMyAdapter.setNotifyOnChange(true);
            mMyAdapter.notifyDataSetChanged();
        }
    }

    public void createNewKeys(View view){
        String alias = mEdtKeyAlias.getText().toString();
        view.requestFocus();
        view.setEnabled(false);

        try {
            if(!mKeyStore.containsAlias(alias)){
                Calendar start = Calendar.getInstance();
                Calendar end = Calendar.getInstance();
                end.add(Calendar.YEAR, 1);

                KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(this)
                        .setAlias(alias)
                        .setSubject(new X500Principal("CN=Sample Name, O=Android Authority"))
                        .setSerialNumber(BigInteger.ONE)
                        .setStartDate(start.getTime())
                        .setEndDate(end.getTime())
                        .build();

                KeyPairGenerator keyPairGenerator = KeyPairGenerator
                        .getInstance("RSA", "AndroidKeyStore");
                keyPairGenerator.initialize(spec);

                KeyPair keyPair = keyPairGenerator.generateKeyPair();
            }

        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }

        view.setEnabled(true);
        refreshKeys();
    }

    public void deleteKey(final String alias){
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Delete key")
                .setMessage("Do yout want to delete key \"" + alias + "\" from the keystore?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    try {
                        mKeyStore.deleteEntry(alias);
                        refreshKeys();

                    } catch (KeyStoreException e) {
                        e.printStackTrace();
                    }
                    dialog.dismiss();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .create();

        alertDialog.show();

    }

    public void encryptString(String alias){
        try {
            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) mKeyStore.getEntry(alias, null);
            RSAPublicKey publicKey = (RSAPublicKey) privateKeyEntry.getCertificate().getPublicKey();

            String initialText =  mEdtInitText.getText().toString();
            if(initialText.isEmpty()){
                Toast.makeText(MainActivity.this, "Enter text into 'Initial Text' widget", Toast.LENGTH_LONG);
                return;
            }
            Cipher input = Cipher.getInstance("RSA/ECB/PKCS1Padding", "AndroidOpenSSL");
            input.init(Cipher.ENCRYPT_MODE, publicKey);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, input);

            cipherOutputStream.write(initialText.getBytes("UTF-8"));
            cipherOutputStream.close();

            byte[] vals = outputStream.toByteArray();
            mEdtEncrypt.setText(Base64.encodeToString(vals, Base64.DEFAULT));

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnrecoverableEntryException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void decryptString(String alias){
        try {
            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry)mKeyStore.getEntry(alias, null);
            RSAPrivateKey privateKey = (RSAPrivateKey) privateKeyEntry.getPrivateKey();

            Cipher output = Cipher.getInstance("RSA/ECB/PKCS1Padding", "AndroidOpenSSL");
            output.init(Cipher.DECRYPT_MODE, privateKey);

            String cipherText = mEdtEncrypt.getText().toString();
            CipherInputStream cipherInputStream = new CipherInputStream(
                    new ByteArrayInputStream(Base64.decode(cipherText, Base64.DEFAULT)), output);

            List<Byte> values = new ArrayList<>();
            int nextByte ;
            while ((nextByte = cipherInputStream.read()) != -1){
                values.add((byte)nextByte);
            }

            byte[] bytes = new byte[values.size()];
            for(int i = 0; i< values.size(); i++){
                bytes[i] = values.get(i).byteValue();
            }

            String finalText = new String(bytes, 0, bytes.length, "UTF-8");
            mEdtDecrypt.setText(finalText);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnrecoverableEntryException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
