package ahgpoug.qrreader.asyncTasks;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import ahgpoug.qrreader.interfaces.responses.DownloaderResponse;
import ahgpoug.qrreader.objects.Task;

public class FileDownloader extends AsyncTask<Void, Integer, Integer> {
    private static final String ACCESS_TOKEN = "Gtb6zMf7DEIAAAAAAAAA-ZN-27BeAujsyRFO1b7RrDfVa_RJ5kNLADfZnt--Bz46";
    public DownloaderResponse delegate = null;

    private Task task;
    private Context context;
    private File pdfFile;
    private boolean isSwiped;

    private MaterialDialog loadingDialog;

    public FileDownloader(Context context, Task task, boolean isSwiped){
        this.task = task;
        this.context = context;
        this.isSwiped = isSwiped;

        if (!isSwiped)
            loadingDialog = new MaterialDialog.Builder(context)
                    .content("Загрузка...")
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
        String directory = "/Задания/";

        File path = new File(Environment.getExternalStorageDirectory().getPath(), "qrreader/downloads");

        if (!path.exists()) {
            if (!path.mkdirs()) {
                return 0;
            }
        } else if (!path.isDirectory()) {
            return 0;
        }

        File file = new File(path, task.getTaskName() + ".pdf");
        removeExistingFile(file);

        try {
            OutputStream out = new FileOutputStream(file);
            directory += String.format("%s.%s", task.getId(), "pdf");
            client.files().download(directory).download(out);
        } catch (IOException | DbxException e) {
            e.printStackTrace();
            return 0;
        }

        pdfFile = file;
        return 1;
    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        if (!isSwiped)
            loadingDialog.dismiss();
        if (result == 0)
            Toast.makeText(context, "Ошибка загрузки", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(context, "Успешно загружено", Toast.LENGTH_SHORT).show();


        delegate.onDownloadFinish(pdfFile);
    }

    private void removeExistingFile(File file){
        if (file.exists())
            file.delete();
    }
}
