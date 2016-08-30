package com.rainbow.kam.ble_gatt_manager.permission;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.rainbow.kam.ble_gatt_manager.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import rx.Observable;


public class AndroidPermission {

    public static final String KEY_PACKAGE_NAME = "KEY_PACKAGE_NAME";
    public static final String KEY_PERMISSIONS = "KEY_PERMISSIONS";

    public static final String KEY_EXPLANATION_MSG = "KEY_EXPLANATION_MSG";
    public static final String KEY_EXPLANATION_CONFIRM = "KEY_EXPLANATION_CONFIRM";

    public static final String KEY_DENIED_MSG = "KEY_DENIED_MSG";
    public static final String KEY_DENIED_CLOSE = "KEY_DENIED_CLOSE";
    public static final String KEY_DENIED_SETTING = "KEY_DENIED_SETTING";

    private String packageName;
    private String[] permissions;

    private String explanationMessage;
    private String explanationConfirmText;

    private String deniedMessage;
    private String deniedCloseButtonText;
    private String showSettingButtonText;

    private final Activity activity;

    public static PermissionListener listener;


    public AndroidPermission(Activity activity) {
        this.packageName = activity.getPackageName();

        this.explanationMessage = activity.getString(R.string.permission_default_message_explanation);
        this.explanationConfirmText = activity.getString(R.string.permission_default_confirm);

        this.deniedMessage = activity.getString(R.string.permission_default_message_denied);
        this.deniedCloseButtonText = activity.getString(R.string.permission_default_denied_close);
        this.showSettingButtonText = activity.getString(R.string.permission_default_setting);

        this.activity = activity;
    }


    public AndroidPermission setPermissions(String... permissions) {
        this.permissions = permissions;
        return this;
    }


    public AndroidPermission setExplanationMessage(String explanationMessage) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(explanationMessage), "Invalid text for explanationMessage");
        this.explanationMessage = explanationMessage;
        return this;
    }


    public AndroidPermission setExplanationMessage(@StringRes int explanationMessageResID) {
        Preconditions.checkArgument(explanationMessageResID > 0, "Invalid value for explanationMessage");
        this.explanationMessage = activity.getString(explanationMessageResID);
        return this;
    }


    public AndroidPermission setExplanationConfirmText(String explanationConfirmText) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(explanationConfirmText), "Invalid text for explanationConfirmText");
        this.explanationConfirmText = explanationConfirmText;
        return this;
    }


    public AndroidPermission setExplanationConfirmText(@StringRes int explanationConfirmTextResID) {
        Preconditions.checkArgument(explanationConfirmTextResID > 0, "Invalid value for explanationConfirmText");
        this.explanationConfirmText = activity.getString(explanationConfirmTextResID);
        return this;
    }


    public AndroidPermission setDeniedMessage(String deniedMessage) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(deniedMessage), "Invalid text for DeniedMessage");
        this.deniedMessage = deniedMessage;
        return this;
    }


    public AndroidPermission setDeniedMessage(@StringRes int deniedMessageResID) {
        Preconditions.checkArgument(deniedMessageResID > 0, "Invalid value for DeniedMessage");
        this.deniedMessage = activity.getString(deniedMessageResID);
        return this;
    }


    public AndroidPermission setDeniedCloseButtonText(String deniedCloseButtonText) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(deniedCloseButtonText), "Invalid text for DeniedCloseButtonText");
        this.deniedCloseButtonText = deniedCloseButtonText;
        return this;
    }


    public AndroidPermission setDeniedCloseButtonText(@StringRes int deniedCloseButtonTextResID) {
        Preconditions.checkArgument(deniedCloseButtonTextResID > 0, "Invalid value for DeniedCloseButtonText");
        this.deniedCloseButtonText = activity.getString(deniedCloseButtonTextResID);
        return this;
    }


    public AndroidPermission setShowSettingButtonText(String showSettingButtonText) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(showSettingButtonText), "Invalid text for ShowSettingButtonText");
        this.showSettingButtonText = showSettingButtonText;
        return this;
    }


    public AndroidPermission setShowSettingButtonText(@StringRes int showSettingButtonTextResID) {
        Preconditions.checkArgument(showSettingButtonTextResID > 0, "Invalid value for ShowSettingButtonText");
        this.showSettingButtonText = activity.getString(showSettingButtonTextResID);
        return this;
    }


    public Observable<ArrayList<String>> check() {
        return Observable.create(subscriber -> {
            if (isEmpty(permissions)) {
                subscriber.onError(new NullPointerException("You must setPermissions() on AndroidPermission"));
            } else {
                boolean isGrantedAll = true;
                for (String permission : this.permissions) {
                    if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                        isGrantedAll = false;
                    }
                }
                if (isGrantedAll) {
                    subscriber.onCompleted();
                } else {
                    listener = new PermissionListener() {
                        @Override public void permissionGranted() {
                            subscriber.onCompleted();
                        }


                        @Override public void permissionDenied(ArrayList<String> deniedPermissions) {
                            subscriber.onNext(deniedPermissions);
                        }
                    };
                    startPermissionActivity();
                }
            }
        });
    }


    private boolean isEmpty(Object s) {
        if (s == null) {
            return true;
        }
        if ((s instanceof String) && (((String) s).trim().length() == 0)) {
            return true;
        }
        if (s instanceof Map) {
            return ((Map<?, ?>) s).isEmpty();
        }
        if (s instanceof List) {
            return ((List<?>) s).isEmpty();
        }
        return s instanceof Object[] && (((Object[]) s).length == 0);
    }


    private void startPermissionActivity() {
        Bundle permissionBundle = new Bundle();
        permissionBundle.putString(KEY_PACKAGE_NAME, packageName);
        permissionBundle.putStringArray(KEY_PERMISSIONS, permissions);
        permissionBundle.putString(KEY_EXPLANATION_MSG, explanationMessage);
        permissionBundle.putString(KEY_EXPLANATION_CONFIRM, explanationConfirmText);
        permissionBundle.putString(KEY_DENIED_MSG, deniedMessage);
        permissionBundle.putString(KEY_DENIED_CLOSE, deniedCloseButtonText);
        permissionBundle.putString(KEY_DENIED_SETTING, showSettingButtonText);

        Intent intent = new Intent(activity, PermissionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtras(permissionBundle);
        activity.startActivity(intent);
    }
}