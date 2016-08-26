package com.rainbow.kam.ble_gatt_manager.permission;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.WindowManager;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;

/**
 * Created by TedPark on 16. 2. 17..
 */
public class PermissionActivity extends AppCompatActivity {

    private static final int REQ_CODE_PERMISSION_REQUEST = 10;
    private static final int REQ_CODE_REQUEST_SETTING = 20;


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        checkPermissions(false);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_CODE_REQUEST_SETTING) {
            checkPermissions(true);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    private void checkPermissions(boolean fromOnActivityResult) {
        ArrayList<String> needPermissions = new ArrayList<>();
        boolean showRationale = false;

        for (String permission : AndroidPermission.permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                needPermissions.add(permission);
                showRationale = true;
            }
        }

        if (needPermissions.isEmpty()) {
            permissionGranted();
        } else if (fromOnActivityResult) {
            permissionDenied(needPermissions);
        } else if (showRationale && !TextUtils.isEmpty(AndroidPermission.explanationMessage)) {
            showRationaleDialog(needPermissions);
        } else {
            requestPermissions(needPermissions);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        ArrayList<String> deniedPermissions = new ArrayList<>();

        for (int i = 0; i < permissions.length; i++) {
            String permission = permissions[i];
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                deniedPermissions.add(permission);
            }
        }

        if (deniedPermissions.isEmpty()) {
            permissionGranted();
        } else {
            showPermissionDenyDialog(deniedPermissions);
        }
    }


    private void permissionGranted() {
        AndroidPermission.permissionSubject.onCompleted();
        finish();
        overridePendingTransition(0, 0);
    }


    private void permissionDenied(ArrayList<String> deniedPermissions) {
        AndroidPermission.permissionSubject.onNext(deniedPermissions);
        finish();
        overridePendingTransition(0, 0);
    }


    private void requestPermissions(ArrayList<String> needPermissions) {
        ActivityCompat.requestPermissions(this, needPermissions.toArray(new String[needPermissions.size()]), REQ_CODE_PERMISSION_REQUEST);
    }


    private void showRationaleDialog(final ArrayList<String> needPermissions) {
        new MaterialDialog.Builder(this)
                .content(AndroidPermission.explanationMessage)
                .cancelable(false)
                .negativeText(AndroidPermission.explanationConfirmText)
                .onNegative((dialog, which) -> requestPermissions(needPermissions))
                .show();
    }


    private void showPermissionDenyDialog(final ArrayList<String> deniedPermissions) {
        new MaterialDialog.Builder(this)
                .content(AndroidPermission.denyMessage)
                .cancelable(false)
                .positiveText(AndroidPermission.settingButtonText)
                .onPositive((dialog, which) -> {
                    Intent intent;
                    try {
                        intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:" + AndroidPermission.packageName));
                    } catch (ActivityNotFoundException e) {
                        intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                    }
                    startActivityForResult(intent, REQ_CODE_REQUEST_SETTING);
                })
                .negativeText(AndroidPermission.deniedCloseButtonText)
                .onNegative((dialog, which) -> permissionDenied(deniedPermissions)).show();
    }
}
