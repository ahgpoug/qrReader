package ahgpoug.qrreader.tasks;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import ahgpoug.qrreader.objects.CombinedTask;
import ahgpoug.qrreader.objects.Task;
import ahgpoug.qrreader.util.Crypto;

public class DbxSQLiteReader {
    public static CombinedTask execute(Context context, String id, String token){
        Task task;

        try {
            token = Crypto.decrypt(token);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        DbxRequestConfig config = new DbxRequestConfig("dropbox/androidClient1");
        DbxClientV2 client = new DbxClientV2(config, token);

        File path = new File(context.getFilesDir(), "temp");

        if (!path.exists()) {
            if (!path.mkdirs()) {
                return null;
            }
        } else if (!path.isDirectory()) {
            return null;
        }

        File file = new File(path, "sqlite.db");
        removeExistingFile(file);

        SQLiteDatabase db = null;
        try {
            OutputStream out = new FileOutputStream(file);
            client.files().download("/sqlite.db").download(out);

            db = SQLiteDatabase.openDatabase(file.getPath(), null, SQLiteDatabase.OPEN_READONLY);

            String query = String.format("SELECT * FROM assoc WHERE id = '%s'", id);
            Cursor cursor = db.rawQuery(query, null);
            if (cursor != null && cursor.getCount() == 1)
                cursor.moveToFirst();

            task = new Task(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3));

            cursor.close();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (db != null)
                db.close();
        }

        if (file.exists())
            file.delete();

        return new CombinedTask(task, token);
    }

    private static void removeExistingFile(File file) {
        if (file.exists())
            file.delete();
    }
}