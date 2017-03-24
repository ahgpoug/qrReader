package ahgpoug.qrreader;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import ahgpoug.qrreader.objects.Task;

public class SelectorActivity extends AppCompatActivity{
    private Task task;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        task = (Task) getIntent().getExtras().getSerializable("task");

        //Toast.makeText(SelectorActivity.this, qrCode, Toast.LENGTH_SHORT).show();
    }
}
