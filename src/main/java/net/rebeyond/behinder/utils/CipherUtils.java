package net.rebeyond.behinder.utils;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

public class CipherUtils {
    public static final String TAG = "CipherUtils";

    static class DecodeHexStrException extends Exception {
        private static final long serialVersionUID = 938776570614030665L;

        DecodeHexStrException(String string) {
            super(string);
        }
    }

    static byte[] RSA_OAEPPaddingPublicKeyEncrpt(byte[] data, PublicKey publicKey) {
        if (data == null || publicKey == null) {
            return new byte[0];
        }
        try {
            Cipher cipher = Cipher.getInstance("RSA/NONE/OAEPWithSHA1AndMGF1Padding");
            cipher.init(1, publicKey);
            return cipher.doFinal(data);
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    static byte[] RSA_OAEPPaddingPrivateKeyDecrpt(byte[] data, PrivateKey privateKey) {
        if (data == null || privateKey == null) {
            return new byte[0];
        }
        try {
            Cipher cipher = Cipher.getInstance("RSA/NONE/OAEPWithSHA1AndMGF1Padding");
            cipher.init(2, privateKey);
            return cipher.doFinal(data);
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    static PublicKey generatePublicKey(BigInteger modulus, BigInteger publicExponent) {
        try {
            return KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(modulus, publicExponent));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    static PrivateKey generatePrivateKey(BigInteger modulus, BigInteger publicExponent) {
        try {
            return KeyFactory.getInstance("RSA").generatePrivate(new RSAPrivateKeySpec(modulus, publicExponent));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    static byte[] AES_CBC_PKCS5PaddingDecrypt(byte[] data, byte[] key, byte[] IV) {
        if (key == null || key.length == 0) {
            return new byte[0];
        }
        try {
            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(IV);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(2, skeySpec, ivParameterSpec);
            return cipher.doFinal(data);
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    static byte[] AES_CBC_PKCS5PaddingEncrypt(byte[] data, byte[] key, byte[] IV) {
        if (key == null || key.length == 0) {
            return new byte[0];
        }
        try {
            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(IV);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(1, skeySpec, ivParameterSpec);
            return cipher.doFinal(data);
        } catch (InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
            return new byte[0];
        }
    }

    static Cipher generateAES_CFB_NoPaddingEncryptCipher(byte[] key, byte[] IV) {
        try {
            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(IV);
            Cipher cipher = Cipher.getInstance("AES/CFB/NoPadding");
            cipher.init(1, skeySpec, ivParameterSpec);
            return cipher;
        } catch (InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
            return new NullCipher();
        }
    }

    static Cipher generateAES_CFB_NoPaddingDecryptCipher(byte[] key, byte[] IV) {
        try {
            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(IV);
            Cipher cipher = Cipher.getInstance("AES/CFB/NoPadding");
            cipher.init(2, skeySpec, ivParameterSpec);
            return cipher;
        } catch (InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
            return new NullCipher();
        }
    }

    static byte[] hmacSha256(byte[] data, byte[] key) {
        if (key == null || key.length == 0) {
            return new byte[0];
        }
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "HmacSHA256");
            Mac mac = Mac.getInstance(secretKeySpec.getAlgorithm());
            mac.init(secretKeySpec);
            return mac.doFinal(data);
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            return new byte[0];
        }
    }

    public static String bytesToHexStr(byte[] data) {
        if (data == null || data.length == 0) {
            return "";
        }
        String hexStr = "0123456789ABCDEF";
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            builder.append(hexStr.charAt((data[i] & 240) >>> 4));
            builder.append(hexStr.charAt(data[i] & 15));
        }
        return builder.toString();
    }

    public static byte[] hexStrToBytes(String hexStr) {
        byte[] result = null;
        if (hexStr != null) {
            try {
                if (hexStr.length() != 0) {
                    char[] hexChars = hexStr.toCharArray();
                    if ((hexChars.length & 1) != 0) {
                        throw new DecodeHexStrException("hexStr is Odd number");
                    }
                    result = new byte[(hexChars.length / 2)];
                    int i = 0;
                    int j = 0;
                    while (i < hexChars.length) {
                        int h = Character.digit(hexChars[i], 16);
                        int i2 = i + 1;
                        int l = Character.digit(hexChars[i2], 16);
                        if (h == -1 || l == -1) {
                            throw new DecodeHexStrException("Illegal hexStr");
                        }
                        result[j] = (byte) ((h << 4) | l);
                        i = i2 + 1;
                        j++;
                    }
                    byte[] bArr = result;
                    return result;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new byte[0];
    }

    static byte[] intToByte(int i) {
        byte[] result = new byte[4];
        result[3] = (byte) ((i >>> 24) & 255);
        result[2] = (byte) ((i >>> 16) & 255);
        result[1] = (byte) ((i >>> 8) & 255);
        result[0] = (byte) ((i >>> 0) & 255);
        return result;
    }

    public static byte[] mergeByteArray(byte[]... byteArray) {
        int totalLength = 0;
        for (int i = 0; i < byteArray.length; i++) {
            if (byteArray[i] != null) {
                totalLength += byteArray[i].length;
            }
        }
        byte[] result = new byte[totalLength];
        int cur = 0;
        for (int i2 = 0; i2 < byteArray.length; i2++) {
            if (byteArray[i2] != null) {
                System.arraycopy(byteArray[i2], 0, result, cur, byteArray[i2].length);
                cur += byteArray[i2].length;
            }
        }
        return result;
    }

    public static String sha256Hex(InputStream is) {
        byte[] buffer = new byte[1024];
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            while (true) {
                int read = is.read(buffer);
                if (read <= -1) {
                    return bytesToHexStr(digest.digest());
                }
                digest.update(buffer, 0, read);
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            return "";
        }
    }

    public static String sha256Hex(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(data);
            return bytesToHexStr(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }

    public static byte[] bytesXor(byte[] b1, byte[] b2) {
        byte[] longbytes;
        byte[] shortbytes;
        if (b1.length >= b2.length) {
            longbytes = b1;
            shortbytes = b2;
        } else {
            longbytes = b2;
            shortbytes = b1;
        }
        byte[] xorstr = new byte[longbytes.length];
        int i = 0;
        while (i < shortbytes.length) {
            xorstr[i] = (byte) (shortbytes[i] ^ longbytes[i]);
            i++;
        }
        while (i < longbytes.length) {
            xorstr[i] = longbytes[i];
            i++;
        }
        return xorstr;
    }
}
