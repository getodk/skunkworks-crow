package org.odk.share.views.ui.instance.fragment;

import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.LargeValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IScatterDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.odk.collect.android.dao.InstancesDao;
import org.odk.collect.android.dto.Instance;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.share.R;
import org.odk.share.dao.TransferDao;
import org.odk.share.dto.TransferInstance;
import org.odk.share.views.ui.common.injectable.InjectableFragment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import butterknife.BindView;
import butterknife.ButterKnife;

import static org.odk.share.views.ui.main.MainActivity.FORM_DISPLAY_NAME;
import static org.odk.share.views.ui.main.MainActivity.FORM_ID;
import static org.odk.share.views.ui.main.MainActivity.FORM_VERSION;

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
    @BindView(R.id.detailed_chart)
    ScatterChart detail_chart;

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
        HashMap<Long, Integer> sentDates = new HashMap<>();
        for (TransferInstance instance : transferInstances) {
            if (instanceMap.containsKey(instance.getInstanceId())) {
                sentCount++;
                if (sentDates.containsKey(instance.getLastStatusChangeDate())) {
                    Integer count = sentDates.get(instance.getLastStatusChangeDate());
                    sentDates.put(instance.getLastStatusChangeDate(), count + 1);
                } else {
                    sentDates.put(instance.getLastStatusChangeDate(), 1);
                }
            }
        }

        transferCursor = transferDao.getReceiveInstancesCursor();
        transferInstances = transferDao.getInstancesFromCursor(transferCursor);
        int receiveCount = 0;
        HashMap<Long, Integer> receiveDates = new HashMap<>();
        for (TransferInstance instance : transferInstances) {
            if (instanceMap.containsKey(instance.getInstanceId())) {
                receiveCount++;
                if (receiveDates.containsKey(instance.getLastStatusChangeDate())) {
                    Integer count = receiveDates.get(instance.getLastStatusChangeDate());
                    receiveDates.put(instance.getLastStatusChangeDate(), count + 1);
                } else {
                    receiveDates.put(instance.getLastStatusChangeDate(), 1);
                }
            }
        }

        transferCursor = transferDao.getReviewedInstancesCursor();
        transferInstances = transferDao.getInstancesFromCursor(transferCursor);
        int reviewCount = 0;
        HashMap<Long, Integer> reviewDates = new HashMap<>();
        for (TransferInstance instance : transferInstances) {
            if (instanceMap.containsKey(instance.getInstanceId())) {
                reviewCount++;
                if (reviewDates.containsKey(instance.getLastStatusChangeDate())) {
                    Integer count = reviewDates.get(instance.getLastStatusChangeDate());
                    reviewDates.put(instance.getLastStatusChangeDate(), count + 1);
                } else {
                    reviewDates.put(instance.getLastStatusChangeDate(), 1);
                }
            }
        }

        drawGraph(sentCount, receiveCount, reviewCount);
        drawGraph(sentDates, receiveDates, reviewDates);
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

    public void drawGraph(HashMap<Long, Integer> sentDates, HashMap<Long, Integer> receiveDates, HashMap<Long, Integer> reviewDates) {
        List<Entry> sentEntries = new ArrayList<>();
        List<Entry> receiveEntries = new ArrayList<>();
        List<Entry> reviewEntries = new ArrayList<>();
        DateFormat df = new SimpleDateFormat("dd:MM");
        int maxValue = 0;
        HashSet<Long> uniqueDates = new HashSet<>();

        Iterator sentDatesIterator = sentDates.entrySet().iterator();
        while(sentDatesIterator.hasNext()) {
            Map.Entry date = (Map.Entry)sentDatesIterator.next();
            sentEntries.add(new Entry((Long)date.getKey(), (Integer)date.getValue()));
            maxValue = (Integer)date.getValue() > maxValue ? (Integer)date.getValue():maxValue;
            uniqueDates.add((Long)date.getKey());
        }
        if (sentEntries.isEmpty()) {
           sentEntries.add(new Entry(0, 0));
        }
        Iterator receiveDatesIterator = receiveDates.entrySet().iterator();
        while(receiveDatesIterator.hasNext()) {
            Map.Entry date = (Map.Entry)receiveDatesIterator.next();
            receiveEntries.add(new Entry((Long)date.getKey(), (Integer)date.getValue()));
            maxValue = (Integer)date.getValue() > maxValue ? (Integer)date.getValue():maxValue;
            uniqueDates.add((Long)date.getKey());
        }
        if (receiveEntries.isEmpty()) {
            receiveEntries.add(new Entry(0, 0));
        }

        Iterator reviewDatesIterator = reviewDates.entrySet().iterator();
        while (reviewDatesIterator.hasNext()) {
            Map.Entry date = (Map.Entry)reviewDatesIterator.next();
            reviewEntries.add(new Entry((Long)date.getKey(), (Integer)date.getValue()));
            maxValue = (Integer)date.getValue() > maxValue ? (Integer)date.getValue():maxValue;
            uniqueDates.add((Long)date.getKey());
        }
        if (reviewEntries.isEmpty()) {
            reviewEntries.add(new Entry(0 , 0));
        }

        ScatterDataSet sentSet = new ScatterDataSet(sentEntries, "Sent");
        sentSet.setScatterShape(ScatterChart.ScatterShape.SQUARE);
        sentSet.setColor(ColorTemplate.COLORFUL_COLORS[0]);

        ScatterDataSet receiveSet = new ScatterDataSet(receiveEntries, "Received");
        receiveSet.setScatterShape(ScatterChart.ScatterShape.TRIANGLE);
        receiveSet.setColor(ColorTemplate.COLORFUL_COLORS[1]);

        ScatterDataSet reviewSet = new ScatterDataSet(reviewEntries, "Reviewed");
        reviewSet.setScatterShape(ScatterChart.ScatterShape.CROSS);
        reviewSet.setColor(ColorTemplate.COLORFUL_COLORS[2]);

        sentSet.setScatterShapeSize(8f);
        receiveSet.setScatterShapeSize(8f);
        reviewSet.setScatterShapeSize(8f);

        XAxis axisX = detail_chart.getXAxis();
        YAxis axisYR = detail_chart.getAxisRight();
        YAxis axisYL = detail_chart.getAxisLeft();
        axisYL.setTypeface(Typeface.DEFAULT_BOLD);
        axisX.setPosition(XAxis.XAxisPosition.BOTTOM);
        axisYR.setEnabled(false);
        axisX.setDrawGridLines(false);
        axisYR.setDrawGridLines(false);
        axisX.setDrawLabels(true);
        axisX.setTypeface(Typeface.DEFAULT_BOLD);
        axisYL.setAxisMinimum(0);
        axisYL.setValueFormatter(new LargeValueFormatter());
        axisX.setAxisMinimum(0);
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
        axisX.setLabelCount(uniqueDates.size(), true);
        axisYL.setLabelCount(6, true);

        axisX.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                if (value > 0) {
                    return df.format(new Date((long)value));
                }
                return "";
            }
        });


        ArrayList<IScatterDataSet> dataSets = new ArrayList<>();
        dataSets.add(sentSet); // add the data sets
        dataSets.add(receiveSet);
        dataSets.add(reviewSet);

        // create a data object with the data sets
        ScatterData data = new ScatterData(dataSets);
        //data.setValueTypeface(tfLight);

        detail_chart.setData(data);
        detail_chart.setDoubleTapToZoomEnabled(false);
        detail_chart.setPinchZoom(false);
        detail_chart.setDescription(null);
        detail_chart.setHorizontalScrollBarEnabled(true);
        detail_chart.invalidate();
    }
}
