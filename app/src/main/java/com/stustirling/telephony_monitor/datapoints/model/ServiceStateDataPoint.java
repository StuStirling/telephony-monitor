package com.stustirling.telephony_monitor.datapoints.model;

import android.telephony.ServiceState;

/**
 * Created by Stu Stirling on 11/11/2017.
 */

public class ServiceStateDataPoint extends DataPoint {

    private final ServiceState serviceState;

    public ServiceStateDataPoint(int dataPoint,
                                 long timestampMillis,
                                 ServiceState serviceState) {
        super(dataPoint,timestampMillis);
        this.serviceState = serviceState;
    }
}
