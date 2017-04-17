package ahgpoug.qrreader;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnPageScrollListener;

import java.io.File;

import ahgpoug.qrreader.asyncTasks.TaskFileDownloader;
import ahgpoug.qrreader.interfaces.responses.TaskFileDownloaderResponse;
import ahgpoug.qrreader.objects.Task;

public class TaskActivity extends AppCompatActivity implements TaskFileDownloaderResponse, SwipeRefreshLayout.OnRefreshListener {
    private PDFView pdfView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Task task;
    private String token;
    private OnPageScrollListener onPageScrollListener;
    private OnErrorListener onErrorListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);
        task = (Task) getIntent().getExtras().getSerializable("task");
        token = getIntent().getExtras().getString("token");

        initViews();
        checkPDF();
    }

    private void initViews(){
        pdfView = (PDFView)findViewById(R.id.pdfView);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.blue, R.color.purple, R.color.green, R.color.orange);

        onPageScrollListener = ((page, positionOffset) -> {
            if (positionOffset == 0.0)
                swipeRefreshLayout.setEnabled(true);
            else
                swipeRefreshLayout.setEnabled(false);
        });

        onErrorListener = (t -> {
            Toast.makeText(TaskActivity.this, "Ошибка загрузки", Toast.LENGTH_SHORT).show();
            File file = new File(Environment.getExternalStorageDirectory().getPath(), "qrreader/downloads/" + task.getTaskName() + ".pdf");
            if (file.exists())
                file.delete();
            downloadPDF(false);
        });

        swipeRefreshLayout.setEnabled(false);
    }

    private void checkPDF(){
        File file = new File(Environment.getExternalStorageDirectory().getPath(), "qrreader/downloads/" + task.getTaskName() + ".pdf");
        if (file.exists()) {
            if (Integer.parseInt(String.valueOf(file.length())) == 0)
                downloadPDF(false);
            else
                loadPDF(file);
        } else {
            downloadPDF(false);
        }
    }

    @Override
    public void onTaskFileDownloadFinish(File output) {
        swipeRefreshLayout.setRefreshing(false);
        pdfView.setEnabled(true);

        if (output != null) {
            loadPDF(output);
        } else {
            finish();
        }
    }

    private void downloadPDF(boolean isSwiped){
        TaskFileDownloader taskFileDownloader = new TaskFileDownloader(TaskActivity.this, task, token, isSwiped);
        taskFileDownloader.delegate = TaskActivity.this;
        taskFileDownloader.execute();
    }


    private void loadPDF(File file){
        pdfView.fromFile(file)
                .enableSwipe(true)
                .swipeHorizontal(false)
                .enableDoubletap(true)
                .defaultPage(0)
                .enableAnnotationRendering(false)
                .onPageScroll(onPageScrollListener)
                .password(null)
                .scrollHandle(null)
                .enableAntialiasing(true)
                .onError(onErrorListener)
                .load();
    }

    @Override
    public void onRefresh() {
        downloadPDF(true);
        swipeRefreshLayout.post(() -> {
                pdfView.setEnabled(false);
                swipeRefreshLayout.setRefreshing(true);
        });
    }
}
