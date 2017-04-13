package ahgpoug.qrreader.asyncTasks;

import android.content.Context;
import android.os.AsyncTask;

import com.afollestad.materialdialogs.MaterialDialog;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import ahgpoug.qrreader.interfaces.responses.DateResponse;

public class DateGetter extends AsyncTask<String, Void, Object> {
    private Context context;
    private MaterialDialog loadingDialog;
    public DateResponse delegate = null;

    public DateGetter(Context context){
        this.context = context;
    }

    protected void onPreExecute(){
        super.onPreExecute();

        loadingDialog = new MaterialDialog.Builder(context)
                .content("Проверка данных...")
                .progress(true, 0)
                .progressIndeterminateStyle(false)
                .cancelable(false)
                .show();
    }

    @Override
    protected Object doInBackground(String... params) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();

        try{
            String link = "http://ahgpoug.xyz/qrreader/getCurrentDate.php";

            URL url = new URL(link);
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet();
            request.setURI(new URI(link));
            HttpResponse response = client.execute(request);
            BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            String line="";

            while ((line = in.readLine()) != null) {
                date = dateFormat.parse(line);
            }

            in.close();

        } catch(Exception e){
            e.printStackTrace();
        }
        return date;
    }

    @Override
    protected void onPostExecute(Object result) {
        Date date = (Date) result;
        delegate.onDateResponseComplete(date);
        loadingDialog.dismiss();
    }
}
