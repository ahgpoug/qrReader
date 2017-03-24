package ahgpoug.qrreader.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;

import ahgpoug.qrreader.SelectorActivity;
import ahgpoug.qrreader.objects.MySQLresponse;
import ahgpoug.qrreader.objects.Task;

public class MySQLreader extends AsyncTask<String, Void, Object>{
    private Context context;
    private MaterialDialog loadingDialog;
    public MySQLresponse delegate = null;

    public MySQLreader(Context context){
        super();
        this.context = context;
    }

    protected void onPreExecute(){
        super.onPreExecute();

        loadingDialog = new MaterialDialog.Builder(context)
                .content("Загрузка...")
                .progress(true, 0)
                .progressIndeterminateStyle(false)
                .cancelable(false)
                .show();
    }

    @Override
    protected Object doInBackground(String... params) {
        try{
            Task task = new Task();
            String link = "http://ahgpoug.xyz/index.php?id=" + params[0];

            URL url = new URL(link);
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet();
            request.setURI(new URI(link));
            HttpResponse response = client.execute(request);
            BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            ArrayList<String> buffer = new ArrayList<>();
            String line="";

            while ((line = in.readLine()) != null) {
                Log.e("MyTAG", line);
                buffer.add(line);
            }

            in.close();
            if (buffer.size() > 0) {
                task = new Task(buffer.get(0), buffer.get(1), buffer.get(2), buffer.get(3));
            }
            return task;
        } catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(Object result) {
        Task task = (Task) result;
        delegate.processFinish(task);
        loadingDialog.dismiss();
    }
}