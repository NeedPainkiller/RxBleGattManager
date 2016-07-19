package com.rainbow.kam.ble_gatt_manager.permission;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;

import com.rainbow.kam.ble_gatt_manager.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.subjects.PublishSubject;


public class AndroidPermission {

    private Activity activity;

    protected static String packageName;
    protected static String[] permissions;
    protected static String explanationMessage;
    protected static String explanationConfirmText;
    protected static String denyMessage;
    protected static String deniedCloseButtonText;
    protected static String settingButtonText;

    protected static PublishSubject<ArrayList<String>> permissionSubject = PublishSubject.create();


    public AndroidPermission(Activity activity) {
        packageName = activity.getPackageName();
        explanationMessage = activity.getString(R.string.permission_default_message_explanation);
        denyMessage = activity.getString(R.string.permission_default_message_denied);
        settingButtonText = activity.getString(R.string.permission_default_setting);
        deniedCloseButtonText = activity.getString(R.string.permission_default_denied_close);
        explanationConfirmText = activity.getString(R.string.permission_default_confirm);
        this.activity = activity;
    }


    public AndroidPermission setPermissions(String... permissions) {
        AndroidPermission.permissions = permissions;
        return this;
    }


    public AndroidPermission setExplanationMessage(String explanationMessage) {
        AndroidPermission.explanationMessage = explanationMessage;
        return this;
    }


    public AndroidPermission setExplanationMessage(@StringRes int stringRes) {
        if (stringRes <= 0) {
            throw new IllegalArgumentException("Invalid value for explanationMessage");
        }
        explanationMessage = this.activity.getString(stringRes);
        return this;
    }


    public AndroidPermission setDeniedMessage(String denyMessage) {
        AndroidPermission.denyMessage = denyMessage;
        return this;
    }


    public AndroidPermission setDeniedMessage(@StringRes int stringRes) {
        if (stringRes <= 0) {
            throw new IllegalArgumentException("Invalid value for DeniedMessage");
        }
        denyMessage = activity.getString(stringRes);
        return this;
    }


    public AndroidPermission setGotoSettingButtonText(String explanationConfirmText) {
        settingButtonText = explanationConfirmText;
        return this;
    }


    public AndroidPermission setGotoSettingButtonText(@StringRes int stringRes) {
        if (stringRes <= 0) {
            throw new IllegalArgumentException("Invalid value for setGotoSettingButtonText");
        }
        settingButtonText = activity.getString(stringRes);
        return this;
    }


    public AndroidPermission setExplanationConfirmText(String explanationConfirmText) {
        AndroidPermission.explanationConfirmText = explanationConfirmText;
        return this;
    }


    public AndroidPermission setExplanationConfirmText(@StringRes int stringRes) {
        if (stringRes <= 0) {
            throw new IllegalArgumentException("Invalid value for explanationConfirmText");
        }
        explanationConfirmText = activity.getString(stringRes);
        return this;
    }


    public AndroidPermission setDeniedCloseButtonText(String deniedCloseButtonText) {
        AndroidPermission.deniedCloseButtonText = deniedCloseButtonText;
        return this;
    }


    public AndroidPermission setDeniedCloseButtonText(@StringRes int stringRes) {
        if (stringRes <= 0) {
            throw new IllegalArgumentException("Invalid value for DeniedCloseButtonText");
        }
        deniedCloseButtonText = activity.getString(stringRes);
        return this;
    }


    public Observable<ArrayList<String>> check() {
        return Observable.merge(permissionSubject, Observable.create((Observable.OnSubscribe<ArrayList<String>>) subscriber -> {
            if (isEmpty(permissions)) {
                subscriber.onError(new NullPointerException("You must setPermissions() on AndroidPermission"));
                return;
            } else {
                boolean isGrantedAll = true;
                for (String permission : AndroidPermission.permissions) {
                    if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                        isGrantedAll = false;
                    }
                }
                if (isGrantedAll) {
                    subscriber.onCompleted();
                    return;
                }
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                subscriber.onCompleted();
            } else {
                checkPermissions();
            }
        }));
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


    private void checkPermissions() {
        Intent intent = new Intent(activity, PermissionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }
}