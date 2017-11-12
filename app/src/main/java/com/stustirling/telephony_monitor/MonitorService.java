package com.stustirling.telephony_monitor;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.PermissionChecker;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

/**
 * Created by Stu Stirling on 10/11/2017.
 */

public class MonitorService extends Service {
    private static final String TAG = "MonitorService";

    private IBinder binder = new MonitorBinder();

    private BehaviorSubject<SignalStrength> signalStrengthBehaviorSubject =
            BehaviorSubject.create();
    private BehaviorSubject<CellLocation> cellLocationBehaviorSubject =
            BehaviorSubject.create();
    private BehaviorSubject<ServiceState> stateBehaviourSubject =
            BehaviorSubject.create();

    @Override
    public void onCreate() {
        super.onCreate();
        beginListening();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int startMode =  super.onStartCommand(intent, flags, startId);
        beginListening();
        return startMode;
    }

    private void beginListening() {
        if (!hasAllRequiredPermissions(this)) {
            Log.e(TAG,"Must grant required permissions");
            return;
        }

        Log.d(TAG,"Beginning to listen");

        final TelephonyManager telephonyManager =
                (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            int events = PhoneStateListener.LISTEN_CELL_LOCATION |
                    PhoneStateListener.LISTEN_SIGNAL_STRENGTHS |
                    PhoneStateListener.LISTEN_SERVICE_STATE;
            telephonyManager.listen(monitoringListener, events);
        }
    }

    private MonitoringPhoneStateListener monitoringListener = new MonitoringPhoneStateListener();

    static boolean hasAllRequiredPermissions( Context context ) {
        boolean hasLocationPermission =
                PermissionChecker.checkSelfPermission(
                        context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        boolean readPhoneStatePermission =
                PermissionChecker.checkSelfPermission(
                        context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;


        return hasLocationPermission && readPhoneStatePermission;
    }

    private class MonitoringPhoneStateListener extends PhoneStateListener {
        @Override
        public void onCellLocationChanged(CellLocation location) {
            super.onCellLocationChanged(location);
            Log.d(TAG,"Cell location changed: "+location);
            cellLocationBehaviorSubject.onNext(location);
        }

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            Log.d(TAG,"Signal strengths changed: "+signalStrength);
            signalStrengthBehaviorSubject.onNext(signalStrength);
        }

        @Override
        public void onServiceStateChanged(ServiceState serviceState) {
            super.onServiceStateChanged(serviceState);
            Log.d(TAG,"Service state changed: "+serviceState);
            stateBehaviourSubject.onNext(serviceState);
        }
    }

    class MonitorBinder extends Binder {
        void permissionsGranted() {
            beginListening();
        }
        Observable<SignalStrength> getSignalStrengthObservable() {
            return signalStrengthBehaviorSubject;
        }

        Observable<CellLocation> getCellLocationObservable() {
            return cellLocationBehaviorSubject;
        }

        Observable<ServiceState> getServiceStateObservable() {
            return stateBehaviourSubject;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
