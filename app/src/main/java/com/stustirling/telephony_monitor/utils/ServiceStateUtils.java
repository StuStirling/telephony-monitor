package com.stustirling.telephony_monitor.utils;

import android.support.annotation.ColorRes;
import android.support.annotation.StringRes;
import android.telephony.ServiceState;

import com.stustirling.telephony_monitor.R;

/**
 * Created by Stu Stirling on 12/11/2017.
 */

public class ServiceStateUtils {

    private ServiceStateUtils() {
    }

    public static @ColorRes int getColorResForServiceState(int state) {
        switch (state) {
            case ServiceState.STATE_IN_SERVICE:
                return R.color.in_service_state;
            case ServiceState.STATE_OUT_OF_SERVICE:
                return R.color.out_service_state;
            case ServiceState.STATE_EMERGENCY_ONLY:
                return R.color.emergency_service_state;
            case ServiceState.STATE_POWER_OFF:
                return R.color.power_off_service_state;
            default:
                return R.color.unknown_service_state;
        }
    }

    public static @StringRes int getStringResForServiceState(int state) {
        switch (state) {
            case ServiceState.STATE_IN_SERVICE:
                return R.string.service_state_in;
            case ServiceState.STATE_OUT_OF_SERVICE:
                return R.string.service_state_out;
            case ServiceState.STATE_EMERGENCY_ONLY:
                return R.string.service_state_emergency;
            case ServiceState.STATE_POWER_OFF:
                return R.string.service_state_off;
            default:
                return R.string.service_state_unknown;
        }
    }


}
