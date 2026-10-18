package com.ludomaster.reference;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import java.security.KeyStore;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/** Store the website access token encrypted by the Android Keystore, never in plaintext. */
public final class SecureLogin {
    private static final String ALIAS="LudoMasters.WebSession.v1";
    private static final String STORE="LudoMasters.Auth";
    private SecureLogin() {}
    private static SecretKey key() throws Exception {
        KeyStore ks=KeyStore.getInstance("AndroidKeyStore");
        ks.load(null);
        if(ks.containsAlias(ALIAS))return (SecretKey)ks.getKey(ALIAS,null);
        KeyGenerator generator=KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore");
        generator.init(new KeyGenParameterSpec.Builder(ALIAS,
             KeyProperties.PURPOSE_ENCRYPT|KeyProperties.PURPOSE_DECRYPT)
             .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
             .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
             .setRandomizedEncryptionRequired(true)
             .build());
        return generator.generateKey();
    }
    public static void save(Context context,String token,String name) throws Exception {
        Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE,key());
        byte[] encrypted=cipher.doFinal(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        SharedPreferences.Editor edit=context.getSharedPreferences(STORE,Context.MODE_PRIVATE).edit();
        edit.putString("iv",Base64.encodeToString(cipher.getIV(),Base64.NO_WRAP));
        edit.putString("payload",Base64.encodeToString(encrypted,Base64.NO_WRAP));
        edit.putString("name",name);
        if(!edit.commit())throw new IllegalStateException("Could not save secure login.");
    }
    public static String token(Context context) {
        try {
            SharedPreferences prefs=context.getSharedPreferences(STORE,Context.MODE_PRIVATE);
            String iv=prefs.getString("iv",null),payload=prefs.getString("payload",null);
            if(iv==null||payload==null)return null;
            Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE,key(),new GCMParameterSpec(128,Base64.decode(iv,Base64.DEFAULT)));
            return new String(cipher.doFinal(Base64.decode(payload,Base64.DEFAULT)),java.nio.charset.StandardCharsets.UTF_8);
        } catch(Exception ignored){return null;}
    }
    public static void clear(Context context) {
        context.getSharedPreferences(STORE,Context.MODE_PRIVATE).edit().clear().apply();
        context.getSharedPreferences("LudoModUser",Context.MODE_PRIVATE).edit().putBoolean("logged",false).apply();
    }
}
