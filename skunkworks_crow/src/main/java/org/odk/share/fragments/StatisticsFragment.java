package org.odk.share.fragments;

import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.LargeValueFormatter;

import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.dto.Instance;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.share.R;
import org.odk.share.dao.TransferDao;
import org.odk.share.dto.TransferInstance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import butterknife.BindView;
import butterknife.ButterKnife;

import static org.odk.share.activities.MainActivity.FORM_DISPLAY_NAME;
import static org.odk.share.activities.MainActivity.FORM_ID;
import static org.odk.share.activities.MainActivity.FORM_VERSION;

/**
 * Created by laksh on 6/27/2018.
 */

public class StatisticsFragment extends InjectableFragment {

    @BindView(R.id.formTitle)
    TextView title;
    @BindView(R.id.formSubTitle)
    TextView subtitle;
    @BindView(R.id.chart)
    BarChart chart;

    @Inject
    InstancesDao instancesDao;
    @Inject
    TransferDao transferDao;

    private String formVersion;
    private String formId;
    private String formName;

    public StatisticsFragment() {

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);
        ButterKnife.bind(this, view);

        formVersion = getActivity().getIntent().getStringExtra(FORM_VERSION);
        formId = getActivity().getIntent().getStringExtra(FORM_ID);
        formName = getActivity().getIntent().getStringExtra(FORM_DISPLAY_NAME);

        title.setText(formName);
        StringBuilder sb = new StringBuilder();
        if (formVersion != null) {
            sb.append(getString(R.string.version, formVersion));
        }
        sb.append(getString(R.string.id, formId));

        subtitle.setText(sb);
        return view;
    }

    @Override
    public void onResume() {
        String[] selectionArgs;
        String selection;

        if (formVersion == null) {
            selectionArgs = new String[]{formId};
            selection = InstanceProviderAPI.InstanceColumns.JR_FORM_ID + "=? AND "
                    + InstanceProviderAPI.InstanceColumns.JR_VERSION + " IS NULL";
        } else {
            selectionArgs = new String[]{formId, formVersion};
            selection = InstanceProviderAPI.InstanceColumns.JR_FORM_ID + "=? AND "
                    + InstanceProviderAPI.InstanceColumns.JR_VERSION + "=?";
        }
        Cursor cursor = instancesDao.getInstancesCursor(selection, selectionArgs);
        HashMap<Long, Instance> instanceMap = instancesDao.getMapFromCursor(cursor);

        Cursor transferCursor = transferDao.getSentInstancesCursor();
        List<TransferInstance> transferInstances = transferDao.getInstancesFromCursor(transferCursor);
        int sentCount = 0;
        for (TransferInstance instance : transferInstances) {
            if (instanceMap.containsKey(instance.getInstanceId())) {
                sentCount++;
            }
        }

        transferCursor = transferDao.getReceiveInstancesCursor();
        transferInstances = transferDao.getInstancesFromCursor(transferCursor);
        int receiveCount = 0;
        for (TransferInstance instance : transferInstances) {
            if (instanceMap.containsKey(instance.getInstanceId())) {
                receiveCount++;
            }
        }

        transferCursor = transferDao.getReviewedInstancesCursor();
        transferInstances = transferDao.getInstancesFromCursor(transferCursor);
        int reviewCount = 0;
        for (TransferInstance instance : transferInstances) {
            if (instanceMap.containsKey(instance.getInstanceId())) {
                reviewCount++;
            }
        }
        drawGraph(sentCount, receiveCount, reviewCount);
        super.onResume();
    }

    public void drawGraph(int sentCount, int receiveCount, int reviewCount) {
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, sentCount));
        entries.add(new BarEntry(1, receiveCount));
        entries.add(new BarEntry(2, reviewCount));

        BarDataSet set = new BarDataSet(entries, "Counts");
        set.setValueFormatter(new LargeValueFormatter());
        set.setValueTextSize(10);
        BarData data = new BarData(set);
        data.setBarWidth(0.9f); // set custom bar width

        XAxis axisX = chart.getXAxis();
        YAxis axisYR = chart.getAxisRight();
        YAxis axisYL = chart.getAxisLeft();
        axisYL.setTypeface(Typeface.DEFAULT_BOLD);
        axisX.setPosition(XAxis.XAxisPosition.BOTTOM);
        axisYR.setEnabled(false);
        axisX.setDrawGridLines(false);
        axisYR.setDrawGridLines(false);
        axisX.setDrawLabels(true);
        axisX.setTypeface(Typeface.DEFAULT_BOLD);
        String[] values = getResources().getStringArray(R.array.stats_field);
        axisX.setLabelCount(3);
        axisX.setValueFormatter((value, axis) -> values[(int) (value)]);
        axisYL.setValueFormatter(new LargeValueFormatter());
        axisYL.setAxisMinimum(0);
        int maxValue = Math.max(Math.max(sentCount, receiveCount), reviewCount);
        int granularity = 1;
        if (maxValue <= 5) {
            axisYL.setAxisMaximum(5);
        } else {
            int axisMax;
            maxValue += 1;
            if (maxValue % 5 != 0) {
                granularity = (maxValue / 5) + 1;
                axisMax = 5 * granularity;
            } else {
                axisMax = maxValue;
                granularity = maxValue / 5;
            }
            axisYL.setAxisMaximum(axisMax);
        }
        axisYL.setGranularity(granularity);
        axisYL.setLabelCount(6, true);
        axisX.setTextSize(13);

        chart.setData(data);
        chart.setFitBars(true); // make the x-axis fit exactly all bars
        chart.setDoubleTapToZoomEnabled(false);
        chart.setPinchZoom(false);
        chart.setTouchEnabled(false);
        chart.setDescription(null);
        chart.invalidate(); // refresh
    }
}
