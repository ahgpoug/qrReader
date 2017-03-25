package ahgpoug.qrreader.objects;

import android.graphics.Bitmap;
import android.net.Uri;

import java.util.Date;

public class Photo {
    private Uri uri;
    private String name;
    private Bitmap bitmap;
    private Date modDate;
    private Bitmap thumbnail;

    public Photo(Uri uri, String name, Bitmap bitmap, Date modDate, Bitmap thumbnail) {
        this.uri = uri;
        this.name = name;
        this.bitmap = bitmap;
        this.modDate = modDate;
        this.thumbnail = thumbnail;
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

    public Bitmap getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(Bitmap thumbnail) {
        this.thumbnail = thumbnail;
    }
}
