package com.stustirling.telephony_monitor;

import android.Manifest;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.CellLocation;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.stustirling.telephony_monitor.utils.GsmUtils;
import com.stustirling.telephony_monitor.utils.ServiceStateUtils;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST = 134;

    private boolean isBound = false;
    private MonitorService.MonitorBinder binder;
    private MainViewModel viewModel;

    private TextView signalStrengthText;
    private TextView gsmArea;
    private TextView serviceStateText;
    private TextView gsmCell;
    private TextView gsmPsc;
    private LinearLayout signalStrengthContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        signalStrengthText = findViewById(R.id.tv_ma_signal_strength);
        serviceStateText = findViewById(R.id.tv_ma_service_state);

        gsmArea = findViewById(R.id.tv_ma_gsm_area);
        gsmCell = findViewById(R.id.tv_ma_gsm_cell);
        gsmPsc = findViewById(R.id.tv_ma_gsm_psc);

        signalStrengthContainer = findViewById(R.id.ll_ma_signal_strength_container);

        viewModel = ViewModelProviders.of(this)
                .get(MainViewModel.class);

        if ( !MonitorService.hasAllRequiredPermissions(this) ) {
            requestPermissions();
        }

        observeSignalStrength();
        observeCellLocation();
        observeServiceState();
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.READ_PHONE_STATE},
                134);
    }

    private void observeSignalStrength() {
        viewModel.getSignalStrengthLiveData().observe(this, new Observer<SignalStrength>() {
            @Override
            public void onChanged(@Nullable SignalStrength signalStrength) {
                if ( signalStrength != null ) {
                    int level = GsmUtils.getGsmLevel(signalStrength);
                    if ( level == -1 ) {
                        Toast.makeText(MainActivity.this,
                                R.string.signal_strength_unknown_format, Toast.LENGTH_SHORT).show();
                    } else {
                        signalStrengthText.setText(GsmUtils.textRepresentationOfGsmLevel(level));
                        signalStrengthContainer.setBackgroundColor(
                                ContextCompat.getColor(MainActivity.this,
                                        GsmUtils.colorRepresentationOfGsmLevel(level)));
                    }
                }
            }
        });
    }



    private void observeCellLocation() {
        viewModel.getCellLocationLiveData().observe(this, new Observer<CellLocation>() {
            @Override
            public void onChanged(@Nullable CellLocation cellLocation) {
                if ( cellLocation != null ) {
                    if ( cellLocation instanceof GsmCellLocation) {
                        GsmCellLocation gsmCellLocation = (GsmCellLocation) cellLocation;
                        gsmArea.setText(String.format(
                                Locale.getDefault(),"%d",gsmCellLocation.getLac()));
                        gsmCell.setText(String.format(
                                Locale.getDefault(),"%d",gsmCellLocation.getCid()));
                        gsmPsc.setText(String.format(
                                Locale.getDefault(),"%d",gsmCellLocation.getPsc()));
                    } else if ( cellLocation instanceof CdmaCellLocation ) {
                        Toast.makeText(MainActivity.this,
                                R.string.cdma_error,
                                Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    private void observeServiceState() {
        viewModel.getServiceStateLivedata().observe(this, new Observer<ServiceState>() {
            @Override
            public void onChanged(@Nullable ServiceState serviceState) {
                if ( serviceState != null ) {
                    String state = getString(
                            ServiceStateUtils.getStringResForServiceState(serviceState.getState()));
                    @ColorRes int background =
                            ServiceStateUtils.getColorResForServiceState(serviceState.getState());
                    serviceStateText.setBackgroundColor(
                            ContextCompat.getColor(MainActivity.this,background));
                    serviceStateText.setText(state);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(this, MonitorService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(mConnection);
        isBound = false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean allAccepted = true;
        for ( int result : grantResults ) {
            if ( result != PackageManager.PERMISSION_GRANTED ) allAccepted = false;
        }
        if (!allAccepted) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.error_title)
                    .setMessage(R.string.permission_error_content)
                    .setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestPermissions();
                        }
                    })
                    .setNegativeButton(R.string.later,null)
                    .show();
        } else {
            binder.permissionsGranted();
        }
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            binder = (MonitorService.MonitorBinder) service;
            viewModel.observe(binder);
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            viewModel.stopObserving();
            isBound = false;
        }
    };
}
