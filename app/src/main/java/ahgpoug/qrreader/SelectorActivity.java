package ahgpoug.qrreader;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import ahgpoug.qrreader.adapters.PhotoRecyclerAdapter;
import ahgpoug.qrreader.interfaces.OnStartDragListener;
import ahgpoug.qrreader.interfaces.SimpleItemTouchHelperCallback;
import ahgpoug.qrreader.objects.Photo;
import ahgpoug.qrreader.objects.Task;
import ahgpoug.qrreader.util.RealPathUtil;
import ahgpoug.qrreader.util.Util;

public class SelectorActivity extends AppCompatActivity implements OnStartDragListener{
    private static final int PICK_IMAGE_REQUEST = 10;
    private static final int CAMERA_REQUEST = 11;
    private static final String CAPTURE_IMAGE_FILE_PROVIDER = "ahgpoug.qrreader.fileprovider";

    private Task task;
    private RecyclerView recyclerView;
    private PhotoRecyclerAdapter adapter;
    private static ArrayList<Photo> photoArrayList = new ArrayList<>();
    private ItemTouchHelper mItemTouchHelper;
    private static String id;
    private MaterialDialog loadingDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_selector);
        task = (Task) getIntent().getExtras().getSerializable("task");
        initViews();
        initEvents();
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    private void initViews(){
        setTitle("Загрузка изображений");
        recyclerView = (RecyclerView)findViewById(R.id.recycler);

        recyclerView.setHasFixedSize(true);
        int spanCount = 2;

        if (SelectorActivity.this.getResources().getConfiguration().orientation == 2)
            spanCount = 4;

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), spanCount);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new PhotoRecyclerAdapter(getApplicationContext(), photoArrayList, this);
        recyclerView.setAdapter(adapter);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void initEvents(){
        final com.getbase.floatingactionbutton.FloatingActionButton action_camera = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.action_camera);
        action_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File path = new File(SelectorActivity.this.getFilesDir(), "qrreader/photos");
                if (!path.exists())
                    path.mkdirs();
                id = new SimpleDateFormat("HHmmss").format(new Date());
                File image = new File(path, "image_" + id + ".jpg");
                Uri imageUri = FileProvider.getUriForFile(SelectorActivity.this, CAPTURE_IMAGE_FILE_PROVIDER, image);
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, CAMERA_REQUEST);
            }
        });

        final com.getbase.floatingactionbutton.FloatingActionButton action_gallery = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.action_gallery);
        action_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            loadingDialog = new MaterialDialog.Builder(SelectorActivity.this)
                    .content("Загрузка...")
                    .progress(true, 0)
                    .progressIndeterminateStyle(false)
                    .cancelable(false)
                    .show();

            Uri uri = data.getData();
            File file = new File(uriToFilename(uri));
            Uri mImageCaptureUri = Uri.fromFile(file);

            photoArrayList.add(new Photo(mImageCaptureUri, file.getName(), new Date(file.lastModified())));
            adapter.notifyItemInserted(photoArrayList.size());
            loadingDialog.dismiss();
        } else if (requestCode == CAMERA_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                loadingDialog = new MaterialDialog.Builder(SelectorActivity.this)
                        .content("Загрузка...")
                        .progress(true, 0)
                        .progressIndeterminateStyle(false)
                        .cancelable(false)
                        .show();

                File path = new File(getFilesDir(), "qrreader/photos");
                if (!path.exists())
                    path.mkdirs();
                File imageFile = new File(path, "image_" + id + ".jpg");

                photoArrayList.add(new Photo(Uri.fromFile(imageFile), imageFile.getName(), new Date(path.lastModified())));
                adapter.notifyItemInserted(photoArrayList.size());
                loadingDialog.dismiss();
            }
        }
    }

    private String uriToFilename(Uri uri) {
        String path = null;

        if (Build.VERSION.SDK_INT < 19) {
            path = RealPathUtil.getRealPathFromURI_API11to18(SelectorActivity.this, uri);
        } else {
            path = RealPathUtil.getRealPathFromURI_API19(SelectorActivity.this, uri);
        }
        return path;
    }
}
