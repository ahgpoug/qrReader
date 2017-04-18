package ahgpoug.qrreader;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionsMenu;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import ahgpoug.qrreader.adapters.PhotoRecyclerAdapter;
import ahgpoug.qrreader.asyncTasks.DateGetter;
import ahgpoug.qrreader.asyncTasks.ImagesUploader;
import ahgpoug.qrreader.interfaces.OnStartDragListener;
import ahgpoug.qrreader.interfaces.SimpleItemTouchHelperCallback;
import ahgpoug.qrreader.interfaces.responses.DateResponse;
import ahgpoug.qrreader.interfaces.responses.ImagesUploaderResponse;
import ahgpoug.qrreader.objects.Photo;
import ahgpoug.qrreader.objects.Task;
import ahgpoug.qrreader.util.RealPathUtil;

public class SelectorActivity extends AppCompatActivity implements OnStartDragListener, ImagesUploaderResponse, DateResponse{
    private static final int PICK_IMAGE_REQUEST = 10;
    private static final int CAMERA_REQUEST = 11;
    private static final String CAPTURE_IMAGE_FILE_PROVIDER = "ahgpoug.qrreader.fileprovider";

    private static Task task;
    private static ArrayList<Photo> photoArrayList = new ArrayList<>();
    private static String id;

    private String token;

    private PhotoRecyclerAdapter adapter;
    private ItemTouchHelper mItemTouchHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_selector);
        task = (Task) getIntent().getExtras().getSerializable("task");
        token = getIntent().getExtras().getString("token");
        initViews();
        initEvents();
    }

    @Override
    public void onBackPressed() {
        id = "";
        task = null;
        photoArrayList.clear();
        super.onBackPressed();
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public void onImageUploadFinish(Integer output) {
        if (output == 1) {
            photoArrayList.clear();
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDateResponseComplete(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date taskDate = dateFormat.parse(task.getExpDate());
            if (date.after(taskDate)){
                Toast.makeText(SelectorActivity.this, "Срок выполнения задания истек", Toast.LENGTH_SHORT).show();
            } else {
                ImagesUploader uploader = new ImagesUploader(SelectorActivity.this, photoArrayList, task, token);
                uploader.delegate = SelectorActivity.this;
                uploader.execute();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void initViews(){
        setTitle("Загрузка изображений");
        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.recycler);

        recyclerView.setHasFixedSize(true);
        int spanCount = 2;

        if (SelectorActivity.this.getResources().getConfiguration().orientation == 2)
            spanCount = 4;

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(SelectorActivity.this, spanCount);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new PhotoRecyclerAdapter(SelectorActivity.this, photoArrayList, this);
        recyclerView.setAdapter(adapter);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void initEvents(){
        final com.getbase.floatingactionbutton.FloatingActionButton action_camera = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.action_camera);
        action_camera.setOnClickListener(v -> {
                File path = new File(Environment.getExternalStorageDirectory().getPath(), "qrreader/qrReader Photos");
                if (!path.exists())
                    path.mkdirs();
                id = new SimpleDateFormat("MMddHHmmssSSS").format(new Date());
                File image = new File(path, "photo_" + id + ".jpg");
                Uri imageUri = FileProvider.getUriForFile(SelectorActivity.this, CAPTURE_IMAGE_FILE_PROVIDER, image);
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, CAMERA_REQUEST);
        });

        final com.getbase.floatingactionbutton.FloatingActionButton action_gallery = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.action_gallery);
        action_gallery.setOnClickListener(v -> {
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
    }

    private void checkDate(){
        DateGetter dateGetter = new DateGetter(SelectorActivity.this);
        dateGetter.delegate = SelectorActivity.this;
        dateGetter.execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            new loadFromGallery().execute(data);
        } else if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            new loadFromCamera().execute();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_selector, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_send) {
            if (photoArrayList.size() > 0) {
                checkDate();
            }
        } else if (id == R.id.action_info) {
            Intent intent = new Intent(SelectorActivity.this, TaskActivity.class);
            intent.putExtra("task", task);
            intent.putExtra("token", token);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
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

    private void hideFAM(){
        FloatingActionsMenu floatingActionsMenu = (FloatingActionsMenu)findViewById(R.id.multiple_actions);
        floatingActionsMenu.collapse();
    }

    private class loadFromCamera extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            File path = new File(Environment.getExternalStorageDirectory().getPath(), "qrreader/qrReader Photos");
            if (!path.exists())
                path.mkdirs();
            File imageFile = new File(path, "photo_" + id + ".jpg");

            MediaScannerConnection.scanFile(SelectorActivity.this, new String[]{imageFile.getPath()}, null, null);

            photoArrayList.add(new Photo(Uri.fromFile(imageFile), imageFile.getName(), new Date(path.lastModified())));
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            adapter.notifyItemInserted(photoArrayList.size());
            hideFAM();
        }
    }

    private class loadFromGallery extends AsyncTask<Intent, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Intent... params) {
            Intent data = params[0];
            Uri uri = data.getData();
            File file = new File(uriToFilename(uri));
            Uri mImageCaptureUri = Uri.fromFile(file);

            photoArrayList.add(new Photo(mImageCaptureUri, file.getName(), new Date(file.lastModified())));
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            adapter.notifyItemInserted(photoArrayList.size());
            hideFAM();
        }
    }
}
