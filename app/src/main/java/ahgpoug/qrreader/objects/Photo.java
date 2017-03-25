package ahgpoug.qrreader.objects;

import android.net.Uri;

import java.io.Serializable;
import java.util.Date;

public class Photo{
    private Uri uri;
    private String name;
    private Date modDate;

    public Photo(Uri uri, String name, Date modDate) {
        this.uri = uri;
        this.name = name;
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

    public Date getModDate() {
        return modDate;
    }

    public void setModDate(Date modDate) {
        this.modDate = modDate;
    }
}
