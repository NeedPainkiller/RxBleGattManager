package com.rainbow.kam.ble_gatt_manager.permission;

import android.content.ActivityNotFoundException;
import android.content.Intent;
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

import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.rainbow.kam.ble_gatt_manager.permission.AndroidPermission.KEY_DENIED_CLOSE;
import static com.rainbow.kam.ble_gatt_manager.permission.AndroidPermission.KEY_DENIED_MSG;
import static com.rainbow.kam.ble_gatt_manager.permission.AndroidPermission.KEY_EXPLANATION_CONFIRM;
import static com.rainbow.kam.ble_gatt_manager.permission.AndroidPermission.KEY_EXPLANATION_MSG;
import static com.rainbow.kam.ble_gatt_manager.permission.AndroidPermission.KEY_PACKAGE_NAME;
import static com.rainbow.kam.ble_gatt_manager.permission.AndroidPermission.KEY_PERMISSIONS;

public class PermissionActivity extends AppCompatActivity {

    private static final int REQ_CODE_PERMISSION_REQUEST = 10;
    private static final int REQ_CODE_REQUEST_SETTING = 20;

    private Bundle permissionBundle;

    private String packageName;
    private String[] permissions;

    private String explanationMessage;
    private String explanationConfirmText;

    private String deniedMessage;
    private String deniedCloseButtonText;
    private String showSettingButtonText;


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityWindow();
        setPermissionBundle();
        setPermissionResources();
        checkPermissions(false);
    }


    private void setActivityWindow() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }


    private void setPermissionBundle() {
        permissionBundle = getIntent().getExtras();
    }


    private void setPermissionResources() {
        packageName = permissionBundle.getString(KEY_PACKAGE_NAME);
        permissions = permissionBundle.getStringArray(KEY_PERMISSIONS);
        explanationMessage = permissionBundle.getString(KEY_EXPLANATION_MSG);
        explanationConfirmText = permissionBundle.getString(KEY_EXPLANATION_CONFIRM);
        deniedMessage = permissionBundle.getString(KEY_DENIED_MSG);
        deniedCloseButtonText = permissionBundle.getString(KEY_DENIED_CLOSE);
        showSettingButtonText = permissionBundle.getString(KEY_DENIED_MSG);
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

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PERMISSION_GRANTED) {
                needPermissions.add(permission);
                showRationale = true;
            }
        }

        if (needPermissions.isEmpty()) {
            permissionGranted();
        } else if (fromOnActivityResult) {
            permissionDenied(needPermissions);
        } else if (showRationale && !TextUtils.isEmpty(explanationMessage)) {
            showRationaleDialog(needPermissions);
        } else {
            requestPermissions(needPermissions);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        ArrayList<String> deniedPermissions = new ArrayList<>();

        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] == PERMISSION_DENIED) {
                deniedPermissions.add(permissions[i]);
            }
        }

        if (deniedPermissions.isEmpty()) {
            permissionGranted();
        } else {
            showPermissionDenyDialog(deniedPermissions);
        }
    }


    private void permissionGranted() {
        AndroidPermission.listener.permissionGranted();
        finish();
        overridePendingTransition(0, 0);
    }


    private void permissionDenied(ArrayList<String> deniedPermissions) {
        AndroidPermission.listener.permissionDenied(deniedPermissions);
        finish();
        overridePendingTransition(0, 0);
    }


    private void requestPermissions(ArrayList<String> needPermissions) {
        ActivityCompat.requestPermissions(this, needPermissions.toArray(new String[needPermissions.size()]), REQ_CODE_PERMISSION_REQUEST);
    }


    private void showRationaleDialog(final ArrayList<String> needPermissions) {
        new MaterialDialog.Builder(this)
                .content(explanationMessage)
                .cancelable(false)
                .negativeText(explanationConfirmText)
                .onNegative((dialog, which) -> requestPermissions(needPermissions))
                .show();
    }


    private void showPermissionDenyDialog(final ArrayList<String> deniedPermissions) {
        new MaterialDialog.Builder(this)
                .content(deniedMessage)
                .cancelable(false)
                .positiveText(showSettingButtonText)
                .onPositive((dialog, which) -> {
                    Intent intent;
                    try {
                        intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:" + packageName));
                    } catch (ActivityNotFoundException e) {
                        intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                    }
                    startActivityForResult(intent, REQ_CODE_REQUEST_SETTING);
                })
                .negativeText(deniedCloseButtonText)
                .onNegative((dialog, which) -> permissionDenied(deniedPermissions)).show();
    }
}
