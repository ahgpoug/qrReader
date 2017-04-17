package ahgpoug.qrreader.util;

import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Crypto {
    private static final String key = "WhenLocustNests1";
    private static final String vector = "AbsolutelyRandom";

    public static String decrypt(String line) throws Exception{
        IvParameterSpec iv = new IvParameterSpec(vector.getBytes("UTF-8"));
        SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

        byte[] original = cipher.doFinal(Base64.decode(line, Base64.NO_WRAP));

        return new String(original);
    }
}
