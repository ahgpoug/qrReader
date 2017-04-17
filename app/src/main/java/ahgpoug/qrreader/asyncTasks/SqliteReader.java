package ahgpoug.qrreader.asyncTasks;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import ahgpoug.qrreader.interfaces.responses.SqliteResponse;
import ahgpoug.qrreader.objects.Task;
import ahgpoug.qrreader.util.Crypto;

public class SqliteReader extends AsyncTask<Void, Integer, Integer> {
    public SqliteResponse delegate = null;

    private Context context;
    private String token;
    private String id;
    private Task task = null;

    private MaterialDialog loadingDialog;

    public SqliteReader(Context context, String id, String token){
        this.context = context;
        this.token = token;
        this.id = id;

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
        try {
            token = Crypto.decrypt(token);
        } catch (Exception e){
            e.printStackTrace();
        }

        DbxRequestConfig config = new DbxRequestConfig("dropbox/androidClient1");
        DbxClientV2 client = new DbxClientV2(config, token);

        File path = new File(Environment.getExternalStorageDirectory().getPath(), "qrreader/temp");

        if (!path.exists()) {
            if (!path.mkdirs()) {
                return 0;
            }
        } else if (!path.isDirectory()) {
            return 0;
        }

        File file = new File(path, "sqlite.db");
        removeExistingFile(file);

        try {
            OutputStream out = new FileOutputStream(file);
            client.files().download("/sqlite.db").download(out);

            SQLiteDatabase db = SQLiteDatabase.openDatabase(file.getPath(), null, SQLiteDatabase.OPEN_READONLY);

            String query = String.format("SELECT * FROM assoc WHERE id = '%s'", id);
            Cursor cursor = db.rawQuery(query, null);
            if (cursor != null && cursor.getCount() == 1)
                cursor.moveToFirst();

            task = new Task(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3));

            cursor.close();

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }

        if (file.exists())
            file.delete();

        return 1;
    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        loadingDialog.dismiss();

        if (result == 0)
            Toast.makeText(context, "Ошибка", Toast.LENGTH_SHORT).show();

        delegate.onSqliteResponseComplete(task, token);
    }

    private void removeExistingFile(File file){
        if (file.exists())
            file.delete();
    }
}
