package ahgpoug.qrreader;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import java.io.File;
import java.io.IOException;
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
    private Task task;
    private RecyclerView recyclerView;
    private PhotoRecyclerAdapter adapter;
    private FloatingActionButton floatingActionButton;
    private static ArrayList<Photo> photoArrayList = new ArrayList<>();
    private ItemTouchHelper mItemTouchHelper;

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
        floatingActionButton = (FloatingActionButton)findViewById(R.id.addPhotoFab);

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
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            try {
                Uri uri = data.getData();
                File file = new File(uriToFilename(uri));
                Uri mImageCaptureUri = Uri.fromFile(file);
                Bitmap photo = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mImageCaptureUri);

                photoArrayList.add(new Photo(mImageCaptureUri, file.getName(), photo, new Date(file.lastModified()), Util.cropBitmapCenter(photo)));
                adapter.notifyItemInserted(photoArrayList.size());
            } catch (IOException e){
                e.printStackTrace();
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
