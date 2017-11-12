package com.stustirling.telephony_monitor.datapoints.model;

import android.telephony.gsm.GsmCellLocation;

/**
 * Created by Stu Stirling on 11/11/2017.
 */

public class GsmCellLocationDataPoint extends DataPoint {
    private final GsmCellLocation cellLocation;

    public GsmCellLocationDataPoint(int dataPoint,
                                    long timestampMillis,
                                    GsmCellLocation cellLocation) {
        super(dataPoint,timestampMillis);
        this.cellLocation = cellLocation;
    }

    public GsmCellLocation getCellLocation() {
        return cellLocation;
    }
}
