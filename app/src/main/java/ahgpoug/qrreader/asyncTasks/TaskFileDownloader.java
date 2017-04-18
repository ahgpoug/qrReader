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

import ahgpoug.qrreader.interfaces.responses.TaskFileDownloaderResponse;
import ahgpoug.qrreader.objects.Task;

public class TaskFileDownloader extends AsyncTask<Void, Integer, Integer> {
    public TaskFileDownloaderResponse delegate = null;

    private Task task;
    private Context context;
    private File pdfFile;
    private boolean isSwiped;
    private String token;

    private MaterialDialog loadingDialog;

    public TaskFileDownloader(Context context, Task task, String token, boolean isSwiped){
        this.task = task;
        this.context = context;
        this.isSwiped = isSwiped;
        this.token = token;

        if (!isSwiped)
            loadingDialog = new MaterialDialog.Builder(context)
                    .content("Загрузка...")
                    .progress(true, 0)
                    .progressIndeterminateStyle(false)
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
        DbxClientV2 client = new DbxClientV2(config, token);
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


        delegate.onTaskFileDownloadFinish(pdfFile);
    }

    private void removeExistingFile(File file){
        if (file.exists())
            file.delete();
    }
}
