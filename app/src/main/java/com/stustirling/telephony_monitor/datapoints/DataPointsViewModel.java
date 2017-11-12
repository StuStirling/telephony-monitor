package com.stustirling.telephony_monitor.datapoints;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.Nullable;
import android.telephony.CellLocation;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import com.stustirling.telephony_monitor.datapoints.model.DataPoint;
import com.stustirling.telephony_monitor.datapoints.model.GsmCellLocationDataPoint;
import com.stustirling.telephony_monitor.datapoints.model.ServiceStateDataPoint;
import com.stustirling.telephony_monitor.datapoints.model.SignalStrengthDataPoint;
import com.stustirling.telephony_monitor.utils.GsmUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.ReplaySubject;

/**
 * Created by Stu Stirling on 11/11/2017.
 */

class DataPointsViewModel extends ViewModel {

    private static final String TAG = "DataPointsVm";

    private MutableLiveData<List<DataPoint>> dataPointsLiveData = new MutableLiveData<>();

    private ReplaySubject<DataPoint> dataPointSubject = ReplaySubject.create();

    private Observer<SignalStrength> signalStrengthObserver = new Observer<SignalStrength>() {
        @Override
        public void onChanged(@Nullable SignalStrength signalStrength) {
            if ( signalStrength != null ) {
                dataPointSubject.onNext(
                        new SignalStrengthDataPoint(GsmUtils.getGsmLevel(signalStrength),
                                System.currentTimeMillis(), signalStrength));
            }
        }
    };

    private Observer<ServiceState> serviceStateObserver = new Observer<ServiceState>() {
        @Override
        public void onChanged(@Nullable ServiceState serviceState) {
            if ( serviceState != null ) {
                dataPointSubject.onNext(
                        new ServiceStateDataPoint(serviceState.getState(),
                                System.currentTimeMillis(), serviceState));
            }
        }
    };

    private Observer<CellLocation> cellLocationObserver = new Observer<CellLocation>() {
        @Override
        public void onChanged(@Nullable CellLocation cellLocation) {
            if ( cellLocation != null && cellLocation instanceof GsmCellLocation ) {
                GsmCellLocation gsmCellLocation = (GsmCellLocation) cellLocation;
                dataPointSubject.onNext(
                        new GsmCellLocationDataPoint(gsmCellLocation.getLac(),
                                System.currentTimeMillis(), gsmCellLocation));
            }
        }
    };

    private LiveData<SignalStrength> signalStrengthLiveData;
    private LiveData<ServiceState> serviceStateLiveData;
    private LiveData<CellLocation> cellLocationLiveData;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public DataPointsViewModel() {
        dataPointSubject.buffer(2,TimeUnit.SECONDS)
                .subscribe(new Consumer<List<DataPoint>>() {
                    @Override
                    public void accept(List<DataPoint> dataPoints) throws Exception {
                        List<DataPoint> existingDataPoints = dataPointsLiveData.getValue();
                        if ( existingDataPoints == null ) existingDataPoints = new ArrayList<>();
                        existingDataPoints.addAll(dataPoints);
                        dataPointsLiveData.postValue(existingDataPoints);
                        Log.d(TAG, "Collected new datapoints: "+dataPoints.size());
                    }
                });
    }

    LiveData<List<DataPoint>> getDataPointsLiveData() {
        return dataPointsLiveData;
    }

    void observeDataChanges(LiveData<SignalStrength> signalStrengthLiveData,
                            LiveData<ServiceState> serviceStateLiveData,
                            LiveData<CellLocation> cellLocationLiveData) {
        this.signalStrengthLiveData = signalStrengthLiveData;
        this.signalStrengthLiveData.observeForever(signalStrengthObserver);
        this.serviceStateLiveData = serviceStateLiveData;
        this.serviceStateLiveData.observeForever(serviceStateObserver);
        this.cellLocationLiveData = cellLocationLiveData;
        this.cellLocationLiveData.observeForever(cellLocationObserver);
    }

    void stopObservingDataChanges() {
        signalStrengthLiveData.removeObserver(signalStrengthObserver);
        serviceStateLiveData.removeObserver(serviceStateObserver);
        cellLocationLiveData.removeObserver(cellLocationObserver);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
    }
}
