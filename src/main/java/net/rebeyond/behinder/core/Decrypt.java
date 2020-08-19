package net.rebeyond.behinder.core;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public class Decrypt {
    public Decrypt() {
    }

    public static byte[] Encrypt(byte[] bs, String key) throws Exception {
        byte[] raw = key.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(1, skeySpec);
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

    public static byte[] EncryptForPhp(byte[] bs, String key) throws Exception {
        byte[] raw = key.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(1, skeySpec, new IvParameterSpec(new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}));
        return cipher.doFinal(bs);
    }

    public static byte[] EncryptForAsp(byte[] bs, String key) throws Exception {
        for (int i = 0; i < bs.length; ++i) {
            bs[i] ^= key.getBytes()[i + 1 & 15];
        }

        return bs;
    }

    public static void main(String[] args) throws Exception {
        new String(EncryptForAsp("ffff".getBytes(), "1234567887654321"));
    }
}
