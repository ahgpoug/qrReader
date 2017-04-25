package ahgpoug.qrreader.tasks;

import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import ahgpoug.qrreader.objects.Photo;
import ahgpoug.qrreader.util.RealPath;

public class ImagesLoader {
    public static ArrayList<Photo> loadFromCamera(Context context, String id, ArrayList<Photo> list){
        File path = new File(Environment.getExternalStorageDirectory().getPath(), "qrreader/qrReader Photos");
        if (!path.exists())
            path.mkdirs();
        File imageFile = new File(path, "photo_" + id + ".jpg");

        MediaScannerConnection.scanFile(context, new String[]{imageFile.getPath()}, null, null);

        list.add(new Photo(Uri.fromFile(imageFile), imageFile.getName(), new Date(path.lastModified())));
        return list;
    }

    public static ArrayList<Photo> loadFromGallery(Context context, Intent data, ArrayList<Photo> list){
        Uri uri = data.getData();
        File file = new File(uriToFilename(context, uri));
        Uri mImageCaptureUri = Uri.fromFile(file);

        list.add(new Photo(mImageCaptureUri, file.getName(), new Date(file.lastModified())));
        return list;
    }

    private static String uriToFilename(Context context, Uri uri) {
        String path = null;

        if (Build.VERSION.SDK_INT < 19) {
            path = RealPath.getRealPathFromURI_API11to18(context, uri);
        } else {
            path = RealPath.getRealPathFromURI_API19(context, uri);
        }
        return path;
    }
}