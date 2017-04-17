package ahgpoug.qrreader.asyncTasks;

import android.content.Context;
import android.os.AsyncTask;

import com.afollestad.materialdialogs.MaterialDialog;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import java.net.InetAddress;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

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
        Date date = new Date();

        try{
            String timeServer = "0.pool.ntp.org";

            NTPUDPClient timeClient = new NTPUDPClient();
            InetAddress inetAddress = InetAddress.getByName(timeServer);
            TimeInfo timeInfo = timeClient.getTime(inetAddress);
            long time = timeInfo.getMessage().getReceiveTimeStamp().getTime();

            Calendar cal = new GregorianCalendar();
            cal.setTimeInMillis(time);
            date = cal.getTime();
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
