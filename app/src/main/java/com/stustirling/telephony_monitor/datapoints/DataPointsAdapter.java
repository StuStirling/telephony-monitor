package com.stustirling.telephony_monitor.datapoints;

import android.arch.lifecycle.Observer;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.stustirling.telephony_monitor.R;
import com.stustirling.telephony_monitor.datapoints.model.DataPoint;
import com.stustirling.telephony_monitor.datapoints.model.GsmCellLocationDataPoint;
import com.stustirling.telephony_monitor.datapoints.model.ServiceStateDataPoint;
import com.stustirling.telephony_monitor.datapoints.model.SignalStrengthDataPoint;
import com.stustirling.telephony_monitor.utils.GsmUtils;
import com.stustirling.telephony_monitor.utils.ServiceStateUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Stu Stirling on 12/11/2017.
 */

public class DataPointsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "DataPointsAdapter";

    private static final int VIEW_TYPE_CELL_LOCATION = 0;
    private static final int VIEW_TYPE_SIGNAL_STRENGTH = 1;
    private static final int VIEW_TYPE_SERVICE_STATE = 2;

    DataPointsAdapter() {
        super();
        setHasStableIds(true);
    }

    Observer<List<DataPoint>> dataPointObserver = new Observer<List<DataPoint>>() {
        @Override
        public void onChanged(@Nullable List<DataPoint> dataPoints) {
            if ( dataPoints != null ) {
                Collections.sort(dataPoints, new Comparator<DataPoint>() {
                    @Override
                    public int compare(DataPoint o1, DataPoint o2) {
                        return (int) (o2.getTimestampMillis() - o1.getTimestampMillis());
                    }
                });
                DataPointsAdapter.this.dataPoints = dataPoints;
                notifyDataSetChanged();
            }

        }
    };

    private List<DataPoint> dataPoints = new ArrayList<>();

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView;
        switch (viewType) {
            case VIEW_TYPE_CELL_LOCATION:
                itemView = inflater.inflate(R.layout.item_cell_location,parent,false);
                return new CellLocationViewHolder(itemView);
            case VIEW_TYPE_SIGNAL_STRENGTH:
                itemView = inflater.inflate(R.layout.item_signal_strength,parent,false);
                return new SignalStrengthViewHolder(itemView);
            case VIEW_TYPE_SERVICE_STATE:
                itemView = inflater.inflate(R.layout.item_service_state,parent,false);
                return new ServiceStateViewHolder(itemView);
                default: throw new IllegalStateException("Unknown view type");
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if ( holder instanceof CellLocationViewHolder ) {
            ((CellLocationViewHolder)holder).bind((GsmCellLocationDataPoint) dataPoints.get(position));
        } else if ( holder instanceof SignalStrengthViewHolder) {
            ((SignalStrengthViewHolder)holder).bind((SignalStrengthDataPoint) dataPoints.get(position));
        } else if ( holder instanceof ServiceStateViewHolder) {
            ((ServiceStateViewHolder)holder).bind((ServiceStateDataPoint) dataPoints.get(position));
        }
    }

    @Override
    public int getItemViewType(int position) {
        DataPoint dataPoint = dataPoints.get(position);
        if ( dataPoint instanceof GsmCellLocationDataPoint) return VIEW_TYPE_CELL_LOCATION;
        else if ( dataPoint instanceof ServiceStateDataPoint ) return VIEW_TYPE_SERVICE_STATE;
        else if ( dataPoint instanceof SignalStrengthDataPoint ) return VIEW_TYPE_SIGNAL_STRENGTH;
        else throw new IllegalStateException("Unknown data point");
    }

    @Override
    public long getItemId(int position) {
        return dataPoints.get(position).hashCode();
    }

    @Override
    public int getItemCount() {
        return dataPoints.size();
    }

    private class CellLocationViewHolder extends DataPointViewHolder<GsmCellLocationDataPoint>{
        private final TextView areaCode;
        private final TextView cellId;
        private final TextView psc;

        CellLocationViewHolder(View itemView) {
            super(itemView,R.id.tv_cli_timestamp);
            areaCode = itemView.findViewById(R.id.tv_cli_gsm_area);
            cellId = itemView.findViewById(R.id.tv_cli_gsm_cell);
            psc = itemView.findViewById(R.id.tv_cli_gsm_psc);
        }

        void bind(GsmCellLocationDataPoint dataPoint) {
            super.bind(dataPoint);
            areaCode.setText(String.format(
                    Locale.getDefault(),"%d",dataPoint.getCellLocation().getLac()));
            cellId.setText(String.format(
                    Locale.getDefault(),"%d",dataPoint.getCellLocation().getCid()));
            psc.setText(String.format(
                    Locale.getDefault(),"%d",dataPoint.getCellLocation().getPsc()));
        }
    }

    private class ServiceStateViewHolder extends DataPointViewHolder<ServiceStateDataPoint> {
        TextView serviceState;
        ServiceStateViewHolder(View itemView) {
            super(itemView,R.id.tv_sstri_timestamp);
            serviceState = itemView.findViewById(R.id.tv_sstri_state);
        }

        void bind(ServiceStateDataPoint dataPoint) {
            super.bind(dataPoint);
            serviceState.setTextColor(
                    ContextCompat.getColor(serviceState.getContext(),
                            ServiceStateUtils.getColorResForServiceState(dataPoint.getDataPoint())));
            serviceState.setText(
                    ServiceStateUtils.getStringResForServiceState(dataPoint.getDataPoint()));
        }
    }

    private class SignalStrengthViewHolder extends DataPointViewHolder<SignalStrengthDataPoint> {
        private TextView rating;
        SignalStrengthViewHolder(View itemView) {
            super(itemView,R.id.tv_ssi_timestamp);
            rating = itemView.findViewById(R.id.tv_ssi_rating);
        }

        @Override
        void bind(SignalStrengthDataPoint dataPoint) {
            super.bind(dataPoint);
            rating.setText(GsmUtils.textRepresentationOfGsmLevel(dataPoint.getDataPoint()));
            rating.setTextColor(
                    ContextCompat.getColor(
                            rating.getContext(),
                            GsmUtils.colorRepresentationOfGsmLevel(dataPoint.getDataPoint())));
        }
    }

    private abstract class DataPointViewHolder<T extends DataPoint> extends RecyclerView.ViewHolder {
        private TextView timeStamp;
        private SimpleDateFormat simpleFormatter = new SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ssZ", Locale.getDefault());
        DataPointViewHolder(View itemView, @IdRes int timestampId ) {
            super(itemView);
            timeStamp = itemView.findViewById(timestampId);
        }

         void bind(T dataPoint) {
            Date date = new Date(dataPoint.getTimestampMillis());
            timeStamp.setText(simpleFormatter.format(date));
        }
    }
}
