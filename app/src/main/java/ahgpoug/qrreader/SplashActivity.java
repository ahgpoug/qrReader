package ahgpoug.qrreader;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.karumi.dexter.Dexter;

import ahgpoug.qrreader.permissions.PermissionsListener;
import ahgpoug.qrreader.util.Util;

public class SplashActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{
    private static final int SIGN_IN_REQUEST = 11;
    private GoogleSignInOptions gso;
    private GoogleApiClient mGoogleApiClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (hasConnection())
            initPermissions();
        else {
            Toast.makeText(SplashActivity.this, "Ошибка подключения к сети", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void startNextActivity(){
        startActivity(new Intent(SplashActivity.this, ScannerActivity.class));
        SplashActivity.this.finish();
    }

    private void initPermissions() {
        PermissionsListener permissionsListener = new PermissionsListener(this);

        Dexter.withActivity(this)
                .withPermissions(android.Manifest.permission.CAMERA,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        android.Manifest.permission.INTERNET,
                        android.Manifest.permission.ACCESS_NETWORK_STATE)
                .withListener(permissionsListener)
                .check();
    }

    public void onPermissionsGranted() {
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            signIn();
        }
    }

    public void onPermissionsDenied() {
        new MaterialDialog.Builder(this)
                .title("Ошибка")
                .content("Для работы приложения необходимо принять все разрешения")
                .positiveText(android.R.string.ok)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        SplashActivity.this.finishAffinity();
                    }
                })
                .show();
    }

    private void signIn(){
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, SIGN_IN_REQUEST);
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            Util.Account.setGoogleAccount(acct);
            startNextActivity();
        } else {
            Toast.makeText(SplashActivity.this, "Ошибка подключения", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SIGN_IN_REQUEST) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(SplashActivity.this, "Ошибка подключения", Toast.LENGTH_SHORT).show();
        finish();
    }

    private boolean hasConnection() {
        ConnectivityManager cm = (ConnectivityManager)SplashActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}