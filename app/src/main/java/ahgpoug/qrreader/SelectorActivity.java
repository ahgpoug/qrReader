package ahgpoug.qrreader;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

public class SelectorActivity extends AppCompatActivity{
    private String qrCode;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        qrCode = getIntent().getExtras().getString("qrCode");

        Toast.makeText(SelectorActivity.this, qrCode, Toast.LENGTH_SHORT).show();
    }
}
