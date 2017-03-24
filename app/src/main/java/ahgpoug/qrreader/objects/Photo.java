package ahgpoug.qrreader.objects;

import android.graphics.Bitmap;
import android.net.Uri;

public class Photo {
    private Uri uri;
    private String name;
    private Bitmap bitmap;

    public Photo(Uri uri, String name, Bitmap bitmap) {
        this.uri = uri;
        this.name = name;
        this.bitmap = bitmap;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
