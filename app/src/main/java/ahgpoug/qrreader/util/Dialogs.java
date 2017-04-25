package ahgpoug.qrreader.util;

import android.content.Context;

import com.afollestad.materialdialogs.MaterialDialog;

public class Dialogs {
    public static MaterialDialog getLoadingDialog(Context context){
        return new MaterialDialog.Builder(context)
                .content("Загрузка...")
                .progress(true, 0)
                .progressIndeterminateStyle(false)
                .cancelable(false)
                .build();
    }

    public static MaterialDialog getProgressLoadingDialog(Context context, int size){
        return  new MaterialDialog.Builder(context)
                .content("Загрузка...")
                .progress(false, size, true)
                .cancelable(false)
                .build();
    }
}
