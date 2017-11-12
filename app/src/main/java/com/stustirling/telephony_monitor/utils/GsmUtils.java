package com.stustirling.telephony_monitor.utils;

import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.annotation.StringRes;
import android.telephony.SignalStrength;

import com.stustirling.telephony_monitor.R;

/**
 * Created by Stu Stirling on 12/11/2017.
 */

public class GsmUtils {
    private GsmUtils() {

    }

    /**
     * Taken from the AOSP SignalStrength class
     *
     * @param signalStrength
     * @return abstract level rating from 0 to 4, 0 being unknown, 1 poor and great 4. -1 if not gsm
     */
    public static int getGsmLevel(SignalStrength signalStrength) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return signalStrength.getLevel();
        } else if ( signalStrength.isGsm()) {
            int gsmSignalStrength = signalStrength.getGsmSignalStrength();
            if (gsmSignalStrength <= 2 || gsmSignalStrength == 99) return  0;
            else if (gsmSignalStrength >= 12) return  4;
            else if (gsmSignalStrength >= 8)  return  3;
            else if (gsmSignalStrength >= 5)  return  2;
            else return 1;
        } else return -1;
    }

    public static @StringRes int textRepresentationOfGsmLevel(int level) {
        switch (level) {
            case 0: return R.string.gsm_signal_unknown;
            case 1: return R.string.gsm_signal_poor;
            case 2: return R.string.gsm_signal_moderate;
            case 3: return R.string.gsm_signal_good;
            default: return R.string.gsm_signal_great;
        }
    }

    public static @ColorRes int colorRepresentationOfGsmLevel(int level) {
        switch (level) {
            case 0: return R.color.signal_strength_unknown;
            case 1: return R.color.signal_strength_poor;
            case 2: return R.color.signal_strength_moderate;
            case 3: return R.color.signal_strength_good;
            default: return R.color.signal_strength_great;
        }
    }
}
