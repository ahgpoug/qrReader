package ahgpoug.qrreader.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.util.LinkedList;
import java.util.List;

public class Util {
    private static GoogleSignInAccount account;

    public static Bitmap cropBitmapCenter(Bitmap srcBmp){
        Bitmap dstBmp;
        if (srcBmp.getWidth() >= srcBmp.getHeight()){

            dstBmp = Bitmap.createBitmap(
                    srcBmp,
                    srcBmp.getWidth()/2 - srcBmp.getHeight()/2,
                    0,
                    srcBmp.getHeight(),
                    srcBmp.getHeight()
            );

        }else{

            dstBmp = Bitmap.createBitmap(
                    srcBmp,
                    0,
                    srcBmp.getHeight()/2 - srcBmp.getWidth()/2,
                    srcBmp.getWidth(),
                    srcBmp.getWidth()
            );
        }

        return dstBmp;
    }

    public static String getCurrentUsername() {
        return account.getDisplayName();
    }

    public static void setGoogleAccount(GoogleSignInAccount acc){
        account = acc;
    }
}
