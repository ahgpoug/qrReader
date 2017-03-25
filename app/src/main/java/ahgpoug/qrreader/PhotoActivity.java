package ahgpoug.qrreader;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.karumi.dexter.Dexter;

import java.io.File;
import java.io.IOException;

import ahgpoug.qrreader.interfaces.MySQLresponse;
import ahgpoug.qrreader.objects.Task;
import ahgpoug.qrreader.permissions.PermissionsListener;
import ahgpoug.qrreader.asyncTasks.MySQLreader;
import ahgpoug.qrreader.util.RealPathUtil;

public class PhotoActivity extends AppCompatActivity implements MySQLresponse{
    private SurfaceView cameraView;
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private FloatingActionButton galleryFab;
    private static final int PICK_IMAGE_REQUEST = 10;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();

        initViews();
        initEvents();
    }

    @Override
    public void processFinish(Task task) {
        if (task == null){
            Toast.makeText(PhotoActivity.this, "Ошибка", Toast.LENGTH_SHORT).show();
            initViews();
            initEvents();
        } else {
            Intent intent = new Intent(PhotoActivity.this, SelectorActivity.class);
            intent.putExtra("task", task);
            startActivity(intent);
        }

    }

    private void initViews(){
        setContentView(R.layout.activity_photo);

        galleryFab = (FloatingActionButton)findViewById(R.id.readFromGalleryFAB);
        cameraView = (SurfaceView)findViewById(R.id.camera_view);

        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();

        cameraSource = new CameraSource
                .Builder(this, barcodeDetector)
                .setRequestedPreviewSize(640, 480)
                .build();
    }

    private void initEvents(){
        galleryFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
            }
        });

        cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    cameraSource.start(cameraView.getHolder());
                } catch (SecurityException e){
                    e.printStackTrace();
                } catch (IOException e) {
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
                    Log.e("My QR Code's Data", barcodes.valueAt(0).displayValue);
                    checkQrCode(barcodes.valueAt(0).displayValue);
                }
            }
        });
    }

    private void initPermissions() {

        PermissionsListener permissionsListener = new PermissionsListener(this);

        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET)
                .withListener(permissionsListener)
                .check();
    }

    public void onPermissionsGranted() {
        initViews();
        initEvents();
    }

    public void onPermissionsDenied(String permission, boolean isPermanentlyDenied) {
        new MaterialDialog.Builder(this)
                .title("Ошибка")
                .content("Для работы приложения необходимо принять все разрешения")
                .positiveText(android.R.string.ok)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        PhotoActivity.this.finishAffinity();
                    }
                })
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            Uri mImageCaptureUri = Uri.fromFile(new File(uriToFilename(uri)));

            try {
                Bitmap qrCode = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mImageCaptureUri);
                Frame myFrame = new Frame.Builder().setBitmap(qrCode).build();

                SparseArray<Barcode> barcodes = barcodeDetector.detect(myFrame);

                if(barcodes.size() != 0) {
                    Log.e("My QR Code's Data", barcodes.valueAt(0).displayValue);
                    checkQrCode(barcodes.valueAt(0).displayValue);
                }
            } catch (IOException e){
                e.printStackTrace();
            }


        }
    }

    private String uriToFilename(Uri uri) {
        String path = null;

        if (Build.VERSION.SDK_INT < 19) {
            path = RealPathUtil.getRealPathFromURI_API11to18(PhotoActivity.this, uri);
        } else {
            path = RealPathUtil.getRealPathFromURI_API19(PhotoActivity.this, uri);
        }
        return path;
    }

    private void checkQrCode(final String qrCode){
        this.runOnUiThread(new Runnable() {
            public void run() {
                cameraSource.release();
                MySQLreader reader = new MySQLreader(PhotoActivity.this);
                reader.delegate = PhotoActivity.this;
                reader.execute(qrCode);
            }
        });
    }
}
