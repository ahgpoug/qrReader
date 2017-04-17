package ahgpoug.qrreader.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public class Util {
    public static class Account{
        private static GoogleSignInAccount account;

        public static String getCurrentUsername() {
            return account.getDisplayName();
        }

        public static void setGoogleAccount(GoogleSignInAccount acc){
            account = acc;
        }
    }

    public static class Images {
        public static Bitmap getThumbnail(Context context, Uri uri) throws IOException {
            int THUMBNAIL_SIZE = 300;
            InputStream input = context.getContentResolver().openInputStream(uri);

            BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
            onlyBoundsOptions.inJustDecodeBounds = true;
            onlyBoundsOptions.inDither=true;
            onlyBoundsOptions.inPreferredConfig=Bitmap.Config.ARGB_8888;
            BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
            input.close();

            if ((onlyBoundsOptions.outWidth == -1) || (onlyBoundsOptions.outHeight == -1)) {
                return null;
            }

            int originalSize = (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) ? onlyBoundsOptions.outHeight : onlyBoundsOptions.outWidth;

            double ratio = (originalSize > THUMBNAIL_SIZE) ? (originalSize / THUMBNAIL_SIZE) : 1.0;

            BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
            bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
            bitmapOptions.inDither = true;
            bitmapOptions.inPreferredConfig=Bitmap.Config.ARGB_8888;
            input = context.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
            input.close();
            return bitmap;
        }

        private static int getPowerOfTwoForSampleRatio(double ratio) {
            int k = Integer.highestOneBit((int) Math.floor(ratio));
            if (k == 0) return 1;
            else return k;
        }
    }
}
