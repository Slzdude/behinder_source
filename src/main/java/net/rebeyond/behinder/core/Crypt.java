package net.rebeyond.behinder.core;


import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public class Crypt {
    public static byte[] Encrypt(byte[] bs, String key) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(1, skeySpec);
        return cipher.doFinal(bs);
    }

    public static byte[] Decrypt(byte[] bs, String key, int encryptType, String type) throws Exception {
        if (type.equals("jsp")) {
            return DecryptForJava(bs, key);
        }
        if (type.equals("php")) {
            return DecryptForPhp(bs, key, encryptType);
        }
        if (type.equals("aspx")) {
            return DecryptForCSharp(bs, key);
        }
        if (type.equals("asp")) {
            return DecryptForAsp(bs, key);
        }
        return null;
    }

    public static byte[] DecryptForJava(byte[] bs, String key) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
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
        if (encryptType == Constants.ENCRYPT_TYPE_AES) {
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(1, skeySpec, new IvParameterSpec(new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
            return cipher.doFinal(bs);
        } else if (encryptType == Constants.ENCRYPT_TYPE_XOR) {
            return DecryptForAsp(bs, key);
        } else {
            return null;
        }
    }

    public static byte[] EncryptForAsp(byte[] bs, String key) throws Exception {
        for (int i = 0; i < bs.length; i++) {
            bs[i] = (byte) (bs[i] ^ key.getBytes()[(i + 1) & 15]);
        }
        return bs;
    }

    public static byte[] DecryptForPhp(byte[] bs, String key, int encryptType) throws Exception {
        if (encryptType == Constants.ENCRYPT_TYPE_AES) {
            byte[] raw = key.getBytes(StandardCharsets.UTF_8);
            byte[] bs2 = Base64.decode(new String(bs));
            SecretKeySpec keySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(2, keySpec, new IvParameterSpec(new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
            return cipher.doFinal(bs2);
        } else if (encryptType == Constants.ENCRYPT_TYPE_XOR) {
            return DecryptForAsp(bs, key);
        } else {
            return null;
        }
    }

    public static byte[] DecryptForAsp(byte[] bs, String key) throws Exception {
        for (int i = 0; i < bs.length; i++) {
            bs[i] = (byte) (bs[i] ^ key.getBytes()[(i + 1) & 15]);
        }
        return bs;
    }
}
