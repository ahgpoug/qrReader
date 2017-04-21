package ahgpoug.qrreader.tasks;

import android.os.Environment;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import ahgpoug.qrreader.objects.Task;

public class DbxPDFDownloader {
    public static File execute(Task task, String token){
        DbxRequestConfig config = new DbxRequestConfig("dropbox/androidClient1");
        DbxClientV2 client = new DbxClientV2(config, token);
        String directory = "/Задания/";

        File path = new File(Environment.getExternalStorageDirectory().getPath(), "qrreader/downloads");

        if (!path.exists()) {
            if (!path.mkdirs()) {
                return null;
            }
        } else if (!path.isDirectory()) {
            return null;
        }

        File file = new File(path, task.getTaskName() + ".pdf");
        removeExistingFile(file);

        try {
            OutputStream out = new FileOutputStream(file);
            directory += String.format("%s.%s", task.getId(), "pdf");
            client.files().download(directory).download(out);
        } catch (IOException | DbxException e) {
            e.printStackTrace();
            return null;
        }

        return file;
    }

    private static void removeExistingFile(File file) {
        if (file.exists())
            file.delete();
    }
}