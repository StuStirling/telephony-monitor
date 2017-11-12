package com.stustirling.telephony_monitor;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.telephony.CellLocation;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

/**
 * Created by Stu Stirling on 10/11/2017.
 */

public class MainViewModel extends ViewModel {

    private MutableLiveData<SignalStrength> signalStrengthMutableLiveData =
            new MutableLiveData<>();
    private MutableLiveData<CellLocation> cellLocationMutableLiveData =
            new MutableLiveData<>();
    private MutableLiveData<ServiceState> serviceStateMutableLiveData =
            new MutableLiveData<>();


    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    void observe(MonitorService.MonitorBinder binder) {
        compositeDisposable.addAll(
                binder.getCellLocationObservable()
                    .subscribe(new Consumer<CellLocation>() {
                        @Override
                        public void accept(CellLocation cellLocation) throws Exception {
                            cellLocationMutableLiveData.postValue(cellLocation);
                        }
                    }),
                binder.getSignalStrengthObservable()
                    .subscribe(new Consumer<SignalStrength>() {
                        @Override
                        public void accept(SignalStrength signalStrength) throws Exception {
                            signalStrengthMutableLiveData.postValue(signalStrength);
                        }
                    }),
                binder.getServiceStateObservable()
                    .subscribe(new Consumer<ServiceState>() {
                        @Override
                        public void accept(ServiceState serviceState) throws Exception {
                            serviceStateMutableLiveData.postValue(serviceState);
                        }
                    })
        );
    }

    public LiveData<SignalStrength> getSignalStrengthLiveData() {
        return signalStrengthMutableLiveData;
    }

    public LiveData<ServiceState> getServiceStateLivedata() {
        return serviceStateMutableLiveData;
    }

    public LiveData<CellLocation> getCellLocationLiveData() {
        return cellLocationMutableLiveData;
    }

    void stopObserving() {
        compositeDisposable.clear();
    }
}
