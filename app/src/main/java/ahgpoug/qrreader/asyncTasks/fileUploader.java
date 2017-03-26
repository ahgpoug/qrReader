package ahgpoug.qrreader.asyncTasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import ahgpoug.qrreader.interfaces.responses.UploaderResponse;
import ahgpoug.qrreader.objects.Photo;
import ahgpoug.qrreader.objects.Task;
import ahgpoug.qrreader.util.Util;

public class FileUploader extends AsyncTask<Void, Integer, Integer> {
    private static final String ACCESS_TOKEN = "Gtb6zMf7DEIAAAAAAAAA-ZN-27BeAujsyRFO1b7RrDfVa_RJ5kNLADfZnt--Bz46";
    public UploaderResponse delegate = null;

    private ArrayList<Photo> photos = new ArrayList<>();
    private Task task;
    private Context context;
    private String userName;

    private MaterialDialog progressDialog;

    public FileUploader(Context context, ArrayList<Photo> photos, Task task){
        this.photos = photos;
        this.task = task;
        this.context = context;

        userName = Util.getCurrentUsername(context);

        Log.e("MyTAG", userName);
        progressDialog = new MaterialDialog.Builder(context)
                .content("Загрузка...")
                .progress(false, photos.size(), true)
                .cancelable(false)
                .show();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Integer doInBackground(Void... params){
        DbxRequestConfig config = new DbxRequestConfig("dropbox/androidClient1");
        DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);
        int counter = 1;
        for (Photo photo : photos) {
            File imageFile = new File(photo.getUri().getPath());
            try {
                InputStream in = new FileInputStream(imageFile);
                String path = String.format("/%s/%s/%s/%s.%s", task.getGroupName(), task.getTaskName(), userName, String.valueOf(counter), photo.getName().substring(photo.getName().lastIndexOf(".") + 1, photo.getName().length()));
                Log.e("MyTAG", path);
                FileMetadata metadata = client.files().uploadBuilder(path).uploadAndFinish(in);
            } catch (IOException e) {
                e.printStackTrace();
                return 0;
            } catch (DbxException e) {
                e.printStackTrace();
                return 0;
            }
            publishProgress(1);
            counter++;
        }

        return 1;
    }

    @Override
    protected void onProgressUpdate(Integer... result) {
        super.onProgressUpdate(result);
        progressDialog.incrementProgress(result[0]);
    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        progressDialog.dismiss();
        if (result == 0)
            Toast.makeText(context, "Ошибка загрузки", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(context, "Успешно загружено " + String.valueOf(photos.size()) + " файлов", Toast.LENGTH_SHORT).show();


        delegate.processFinish(result);
    }

}
