package ahgpoug.qrreader.tasks;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.Metadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import ahgpoug.qrreader.objects.Photo;
import ahgpoug.qrreader.objects.Task;
import ahgpoug.qrreader.util.Util;

public class DbxImagesUploader {
    public static void execute(Task task, Photo photo, String token){
        String userName = Util.Account.getCurrentUsername();
        DbxRequestConfig config = new DbxRequestConfig("dropbox/androidClient1");
        DbxClientV2 client = new DbxClientV2(config, token);
        String directory = String.format("/%s/%s/%s/", task.getGroupName(), task.getTaskName(), userName);
        File imageFile = new File(photo.getUri().getPath());
        try {
            InputStream in = new FileInputStream(imageFile);
            String path = String.format("%s%s.%s", directory,  String.valueOf(getDirectoryFilesCount(client, directory) + 1), photo.getName().substring(photo.getName().lastIndexOf(".") + 1, photo.getName().length()));
            client.files().uploadBuilder(path).uploadAndFinish(in);
        } catch (IOException | DbxException e) {
            e.printStackTrace();
        }
    }

    private static int getDirectoryFilesCount(DbxClientV2 client, String path){
        try {
            List<Metadata> result = client.files().listFolder(path).getEntries();
            return result.size();
        } catch (DbxException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static void clearActiveDirectory(Task task, String token){
        String userName = Util.Account.getCurrentUsername();
        DbxRequestConfig config = new DbxRequestConfig("dropbox/androidClient1");
        DbxClientV2 client = new DbxClientV2(config, token);
        String directory = String.format("/%s/%s/%s/", task.getGroupName(), task.getTaskName(), userName);
        try {
            List<Metadata> result = client.files().listFolder(directory).getEntries();
            for (Metadata entry : result)
                client.files().delete(entry.getPathDisplay());
        } catch (DbxException e) {
            e.printStackTrace();
        }
    }
}