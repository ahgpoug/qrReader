package ahgpoug.qrreader;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import ahgpoug.qrreader.asyncTasks.DbxSqliteReader;
import ahgpoug.qrreader.objects.Task;
import ahgpoug.qrreader.util.Dialogs;
import ahgpoug.qrreader.util.RealPath;
import ahgpoug.qrreader.util.Util;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ScannerActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 10;

    private SurfaceView cameraView;
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private FloatingActionButton galleryFab;
    private MaterialDialog loadingDialog;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();

        initViews();
        initEvents();
    }

    private void onSqliteTaskComplete(Task task, String token){
        if (loadingDialog != null && loadingDialog.isShowing())
            loadingDialog.dismiss();
        Intent intent = new Intent(ScannerActivity.this, SelectorActivity.class);
        intent.putExtra("token", token);
        intent.putExtra("task", task);
        startActivity(intent);
    }

    private void onSqliteTaskError(){
        if (loadingDialog != null && loadingDialog.isShowing())
            loadingDialog.dismiss();
        Toast.makeText(ScannerActivity.this, "Ошибка", Toast.LENGTH_SHORT).show();
        initViews();
        initEvents();
    }

    private void initViews(){
        setContentView(R.layout.activity_scanner);

        galleryFab = (FloatingActionButton)findViewById(R.id.readFromGalleryFAB);
        cameraView = (SurfaceView)findViewById(R.id.camera_view);

        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();

        if(!barcodeDetector.isOperational()){
            Toast.makeText(ScannerActivity.this, "Ошибка подключения детектора", Toast.LENGTH_SHORT).show();
            return;
        }

        cameraSource = new CameraSource
                .Builder(this, barcodeDetector)
                .setRequestedPreviewSize(640, 480)
                .setAutoFocusEnabled(true)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams attributes = getWindow().getAttributes();
            attributes.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
            getWindow().setAttributes(attributes);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) galleryFab.getLayoutParams();
            params.bottomMargin = Util.getNavigationBarHeight(ScannerActivity.this) + 32;
        }
    }

    private void initEvents(){
        galleryFab.setOnClickListener(v -> {
                if (Build.VERSION.SDK_INT < 19) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
                } else {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                    startActivityForResult(intent, PICK_IMAGE_REQUEST);
                }
        });

        cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    cameraSource.start(cameraView.getHolder());
                } catch (SecurityException | IOException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();

                if (barcodes.size() != 0) {
                    String[] parts = barcodes.valueAt(0).displayValue.split("\\....");

                    if (parts.length > 1)
                        checkQrCode(parts[0], parts[1]);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            Uri mImageCaptureUri = Uri.fromFile(new File(uriToFilename(uri)));

            try {
                Bitmap qrCode = Util.Images.getThumbnail(ScannerActivity.this, mImageCaptureUri);
                Frame myFrame = new Frame.Builder().setBitmap(qrCode).build();

                SparseArray<Barcode> barcodes = barcodeDetector.detect(myFrame);

                if (barcodes.size() != 0) {
                    String[] parts = barcodes.valueAt(0).displayValue.split("\\....");

                    if (parts.length > 1)
                        checkQrCode(parts[0], parts[1]);
                } else {
                    Toast.makeText(ScannerActivity.this, "QR код не распознан", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String uriToFilename(Uri uri) {
        String path;

        if (Build.VERSION.SDK_INT < 19) {
            path = RealPath.getRealPathFromURI_API11to18(ScannerActivity.this, uri);
        } else {
            path = RealPath.getRealPathFromURI_API19(ScannerActivity.this, uri);
        }
        return path;
    }

    private void checkQrCode(final String id, final String token){
        this.runOnUiThread(() -> {
            if (loadingDialog != null && loadingDialog.isShowing())
                loadingDialog.dismiss();
            loadingDialog = Dialogs.getLoadingDialog(ScannerActivity.this);
            loadingDialog.show();

            try {
                cameraSource.release();
            } catch (Exception e) {
                e.printStackTrace();
            }

            Observable.defer(() -> Observable.just(DbxSqliteReader.execute(ScannerActivity.this, id, token)))
                    .filter(result -> result != null)
                    .subscribeOn(Schedulers.io())
                    .timeout(30, TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(result -> onSqliteTaskComplete(result.getTask(), result.getToken()), e -> onSqliteTaskError());
        });
    }
}
