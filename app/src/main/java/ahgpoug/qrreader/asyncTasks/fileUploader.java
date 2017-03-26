package ahgpoug.qrreader.asyncTasks;

import android.content.Context;
import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import ahgpoug.qrreader.objects.Photo;

public class fileUploader extends AsyncTask<Void, Void, Void> {
    private static final String ACCESS_TOKEN = "Gtb6zMf7DEIAAAAAAAAA-ZN-27BeAujsyRFO1b7RrDfVa_RJ5kNLADfZnt--Bz46";
    private ArrayList<Photo> photos = new ArrayList<Photo>();

    public fileUploader(ArrayList<Photo> photos){
        this.photos = photos;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... params){
        DbxRequestConfig config = new DbxRequestConfig("dropbox/androidClient1");
        DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);
        File imageFile = new File(photos.get(0).getUri().getPath());
        try{
            InputStream in = new FileInputStream(imageFile);
            FileMetadata metadata = client.files().uploadBuilder("/" + photos.get(0).getName()).uploadAndFinish(in);
        } catch (IOException e){
            e.printStackTrace();
        } catch (DbxException e){
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }

}
