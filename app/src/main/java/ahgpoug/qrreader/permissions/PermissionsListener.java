package ahgpoug.qrreader.permissions;

import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;

import ahgpoug.qrreader.SplashActivity;

public class PermissionsListener implements MultiplePermissionsListener {
    private final SplashActivity activity;

    public PermissionsListener(SplashActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onPermissionsChecked(MultiplePermissionsReport report) {
        if (report.areAllPermissionsGranted())
            activity.onPermissionsGranted();
        else
            activity.onPermissionsDenied();
    }

    @Override
    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
        token.continuePermissionRequest();
    }
}
