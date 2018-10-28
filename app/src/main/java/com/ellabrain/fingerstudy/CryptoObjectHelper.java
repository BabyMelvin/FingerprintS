package com.ellabrain.fingerstudy;

/*
 * Copyright 2018
 * http://www.aiella.com
 * Author: Melvin(Hang Cao)
 * Email : hang.yasuo@gmail.com
 * Date  : 2018/10/19
 */

import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresApi;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;

import java.security.Key;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;

/**
 * 指纹认证的过程是可能被第三方的中间件恶意攻击的，常见的攻击的手段就是拦截和篡改指纹识别器提供的结果。
 * 这里我们可以提供CryptoObject对象给authenticate方法来避免这种形式的攻击。
 * FingerprintManager.CryptoObject是基于Java加密API的一个包装类，并且被FingerprintManager用来保证认证结果的完整性。
 * 通常来讲，用来加密指纹扫描结果的机制就是一个Javax.Crypto.Cipher对象。Cipher对象本身会使用由应用调用
 * Android keystore的API产生一个key来实现上面说道的保护功能。
 */
public class CryptoObjectHelper {
    // This can be key name you want. Should be unique for the app.
    private static final String KEY_NAME = "com.ella_brain.finger_study.authenticate_key";

    //we always use tis keystore on Android
    static final String KEYSTORE_NAME = "AndroidKeyStore";

    //should be no need to change these values
    static final String KEY_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES;
    static final String BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC;
    static final String ENCRYPTION_PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7;
    static final String TRANSFORMATION = KEY_ALGORITHM + "/" + BLOCK_MODE + "/" + ENCRYPTION_PADDING;
    private KeyStore _keystore;

    public CryptoObjectHelper() throws Exception {
        _keystore = KeyStore.getInstance(KEYSTORE_NAME);
        _keystore.load(null);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public FingerprintManagerCompat.CryptoObject buildCryptoObject() throws Exception {
        Cipher cipher = createCipher(true);
        return new FingerprintManagerCompat.CryptoObject(cipher);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private Cipher createCipher(boolean retry) throws Exception {
        Key key = GetKey();
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        try {
            cipher.init(Cipher.ENCRYPT_MODE | Cipher.DECRYPT_MODE, key);
        } catch (KeyPermanentlyInvalidatedException e) {
            _keystore.deleteEntry(KEY_NAME);
            if (retry) {
                createCipher(false);
            } else {
                throw new Exception("could not create the Cipher");
            }
        }
        return cipher;
    }

    Key GetKey() throws Exception {
        Key secretKey;
        if (!_keystore.isKeyEntry(KEY_NAME)) {
            CreateKey();
        }
        secretKey = _keystore.getKey(KEY_NAME, null);
        return secretKey;
    }

    private void CreateKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(KEY_ALGORITHM, KEYSTORE_NAME);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            KeyGenParameterSpec keyGenSpec = new KeyGenParameterSpec.Builder(KEY_NAME, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(BLOCK_MODE)
                    .setEncryptionPaddings(ENCRYPTION_PADDING)
                    .setUserAuthenticationRequired(true)
                    .build();
            keyGen.init(keyGenSpec);
            keyGen.generateKey();

        }
    }

}
