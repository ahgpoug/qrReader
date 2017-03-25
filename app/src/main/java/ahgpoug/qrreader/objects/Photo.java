package ahgpoug.qrreader.objects;

import android.graphics.Bitmap;
import android.net.Uri;

import java.util.Date;

public class Photo {
    private Uri uri;
    private String name;
    private Bitmap bitmap;
    private Date modDate;

    public Photo(Uri uri, String name, Bitmap bitmap, Date modDate) {
        this.uri = uri;
        this.name = name;
        this.bitmap = bitmap;
        this.modDate = modDate;
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

    public Date getModDate() {
        return modDate;
    }

    public void setModDate(Date modDate) {
        this.modDate = modDate;
    }
}
