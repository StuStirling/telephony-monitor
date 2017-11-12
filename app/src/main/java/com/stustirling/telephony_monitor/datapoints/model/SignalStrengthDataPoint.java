package com.stustirling.telephony_monitor.datapoints.model;

import android.telephony.SignalStrength;

/**
 * Created by Stu Stirling on 11/11/2017.
 */

public class SignalStrengthDataPoint extends DataPoint {

    private final SignalStrength signalStrength;

    public SignalStrengthDataPoint(int dataPoint,
                                   long timestampMillis,
                                   SignalStrength signalStrength) {
        super(dataPoint,timestampMillis);
        this.signalStrength = signalStrength;
    }

    public SignalStrength getSignalStrength() {
        return signalStrength;
    }
}
