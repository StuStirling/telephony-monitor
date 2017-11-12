package com.stustirling.telephony_monitor.datapoints.model;

/**
 * Created by Stu Stirling on 11/11/2017.
 */

public abstract class DataPoint {

    private final int dataPoint;
    private long timestampMillis;

    DataPoint(int dataPoint,
              long timestampMillis) {
        this.dataPoint = dataPoint;
        this.timestampMillis = timestampMillis;
    }

    public int getDataPoint() {
        return dataPoint;
    }

    public long getTimestampMillis() {
        return timestampMillis;
    }
}
