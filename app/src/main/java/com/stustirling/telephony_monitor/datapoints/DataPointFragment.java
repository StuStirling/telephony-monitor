package com.stustirling.telephony_monitor.datapoints;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stustirling.telephony_monitor.MainViewModel;
import com.stustirling.telephony_monitor.R;

/**
 * Created by Stu Stirling on 11/11/2017.
 */

public class DataPointFragment extends Fragment {

    private MainViewModel mainViewModel;
    private DataPointsViewModel viewModel;

    private RecyclerView recyclerView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(this)
                .get(DataPointsViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_data_point,container,false);
        recyclerView = view.findViewById(R.id.rv_dpf_recycler);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        DataPointsAdapter adapter = new DataPointsAdapter();
        recyclerView.setAdapter(adapter);
        viewModel.getDataPointsLiveData().observe(this,adapter.dataPointObserver);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mainViewModel = ViewModelProviders.of(getActivity())
                .get(MainViewModel.class);
        viewModel.observeDataChanges(
                mainViewModel.getSignalStrengthLiveData(),
                mainViewModel.getServiceStateLivedata(),
                mainViewModel.getCellLocationLiveData());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        viewModel.stopObservingDataChanges();
    }
}
