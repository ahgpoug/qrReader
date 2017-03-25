package ahgpoug.qrreader;

import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.plus.model.people.Person;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ahgpoug.qrreader.objects.Photo;
import ahgpoug.qrreader.objects.Task;

public class UploadActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private GoogleApiClient mGoogleApiClient;
    private static final String TAG = "drive-quickstart";
    private static final int REQUEST_CODE_CAPTURE_IMAGE = 1;
    private static final int REQUEST_CODE_CREATOR = 2;
    private static final int REQUEST_CODE_RESOLUTION = 3;

    private static ArrayList<Photo> photos;
    private static Task task;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String jsonPhotos = getIntent().getExtras().getString("photos");
        task = (Task) getIntent().getExtras().getSerializable("task");

        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Photo>>(){}.getType();
        photos = gson.fromJson(jsonPhotos, type);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onPause();
    }

    private void saveFileToDrive() {
        // Start by creating a new contents, and setting a callback.
        Log.i(TAG, "Creating new contents.");

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mGoogleApiClient == null) {
            // Create the API client and bind it to an instance variable.
            // We use this instance as the callback for connection and connection
            // failures.
            // Since no account name is passed, the user is prompted to choose.
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        // Connect the client. Once connected, the camera is launched.
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "API client connected.");
        /*if (mBitmapToSave == null) {
            // This activity has no UI of its own. Just start the camera.
            startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), REQUEST_CODE_CAPTURE_IMAGE);
            return;
        }*/
        saveFileToDrive();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
        if (!result.hasResolution()) {
            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(this, result.getErrorCode(), 0).show();
            return;
        }

        try {
            result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
        }
    }

    class uploadImage extends AsyncTask<Photo, Void, Integer> {
        private MaterialDialog loadingDialog;
        private int code = 0;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingDialog = new MaterialDialog.Builder(UploadActivity.this)
                    .content("Загрузка...")
                    .progress(true, 0)
                    .progressIndeterminateStyle(false)
                    .cancelable(false)
                    .show();
        }

        @Override
        protected Integer doInBackground(Photo... params) {
            final Photo photo = params[0];
            try {
                final Bitmap image = MediaStore.Images.Media.getBitmap(UploadActivity.this.getContentResolver(), photo.getUri());
                Drive.DriveApi.newDriveContents(mGoogleApiClient)
                        .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {

                            @Override
                            public void onResult(DriveApi.DriveContentsResult result) {
                                // If the operation was not successful, we cannot do anything
                                // and must
                                // fail.
                                if (!result.getStatus().isSuccess()) {
                                    Log.i(TAG, "Failed to create new contents.");
                                    code = 0;
                                    return;
                                }
                                // Otherwise, we can write our data to the new contents.
                                Log.i(TAG, "New contents created.");
                                code = 1;
                                // Get an output stream for the contents.
                                OutputStream outputStream = result.getDriveContents().getOutputStream();
                                // Write the bitmap data from it.
                                ByteArrayOutputStream bitmapStream = new ByteArrayOutputStream();
                                image.compress(Bitmap.CompressFormat.PNG, 100, bitmapStream);
                                try {
                                    outputStream.write(bitmapStream.toByteArray());
                                } catch (IOException e1) {
                                    code = 0;
                                    Log.i(TAG, "Unable to write file contents.");
                                }
                                // Create the initial metadata - MIME type and title.
                                // Note that the user will be able to change the title later.
                                MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder().setMimeType("image/jpeg").setTitle("Android Photo.png").build();
                                // Create an intent for the file chooser, and start it.
                                IntentSender intentSender = Drive.DriveApi
                                        .newCreateFileActivityBuilder()
                                        .setInitialMetadata(metadataChangeSet)
                                        .setInitialDriveContents(result.getDriveContents())
                                        .build(mGoogleApiClient);
                                try {
                                    startIntentSenderForResult(intentSender, REQUEST_CODE_CREATOR, null, 0, 0, 0);
                                } catch (IntentSender.SendIntentException e) {
                                    Log.i(TAG, "Failed to launch file chooser.");
                                }
                            }
                        });
            }catch (IOException e){
                e.printStackTrace();
                code = 0;
            }
            return code;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (code == 0)
                Toast.makeText(UploadActivity.this, "Ошибка", Toast.LENGTH_SHORT).show();
            loadingDialog.dismiss();
            UploadActivity.this.finish();
        }
    }
}
