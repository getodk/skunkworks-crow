package org.odk.share.views.ui.instance.fragment;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import org.odk.share.views.ui.settings.PreferenceKeys;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
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
    ScatterChart detailedChart;

    @Inject
    InstancesDao instancesDao;
    @Inject
    TransferDao transferDao;

    private String formVersion;
    private String formId;
    private String formName;
    private DateFormat df = new SimpleDateFormat("dd/MM/YYYY");
    private SharedPreferences prefs;

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
        HashMap<String, Integer> sentDates = new HashMap<>();
        for (TransferInstance instance : transferInstances) {
            if (instanceMap.containsKey(instance.getInstanceId())) {
                sentCount++;
                String date = df.format(instance.getLastStatusChangeDate());
                if (sentDates.containsKey(date)) {
                    Integer count = sentDates.get(date);
                    sentDates.put(date, count + 1);
                } else {
                    sentDates.put(date, 1);
                }
            }
        }

        transferCursor = transferDao.getReceiveInstancesCursor();
        transferInstances = transferDao.getInstancesFromCursor(transferCursor);
        int receiveCount = 0;
        HashMap<String, Integer> receiveDates = new HashMap<>();
        for (TransferInstance instance : transferInstances) {
            if (instanceMap.containsKey(instance.getInstanceId())) {
                receiveCount++;
                String date = df.format(instance.getLastStatusChangeDate());
                if (receiveDates.containsKey(date)) {
                    Integer count = receiveDates.get(date);
                    receiveDates.put(date, count + 1);
                } else {
                    receiveDates.put(date, 1);
                }
            }
        }

        transferCursor = transferDao.getReviewedInstancesCursor();
        transferInstances = transferDao.getInstancesFromCursor(transferCursor);
        int reviewCount = 0;
        HashMap<String, Integer> reviewDates = new HashMap<>();
        for (TransferInstance instance : transferInstances) {
            if (instanceMap.containsKey(instance.getInstanceId())) {
                reviewCount++;
                String date = df.format(instance.getLastStatusChangeDate());
                if (reviewDates.containsKey(date)) {
                    Integer count = reviewDates.get(date);
                    reviewDates.put(date, count + 1);
                } else {
                    reviewDates.put(date, 1);
                }
            }
        }

        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean detailedStatisticsEnabled = prefs.getBoolean(PreferenceKeys.KEY_DETAILED_STATISTICS, false);
        if (detailedStatisticsEnabled) {
            drawScatterGraph(sentDates, receiveDates, reviewDates);
            chart.setVisibility(View.GONE);
        } else {
            drawBarGraph(sentCount, receiveCount, reviewCount);
            detailedChart.setVisibility(View.GONE);
        }

        super.onResume();
    }

    public void drawBarGraph(int sentCount, int receiveCount, int reviewCount) {
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


    public void drawScatterGraph(HashMap<String, Integer> sentDates, HashMap<String, Integer> receiveDates, HashMap<String, Integer> reviewDates) {
        List<Entry> sentEntries = new ArrayList<>();
        List<Entry> receiveEntries = new ArrayList<>();
        List<Entry> reviewEntries = new ArrayList<>();
        int maxValue = 0;

        // Map from indices to corresponding date for last 30 days
        Calendar cal = Calendar.getInstance();
        HashMap<Integer, String> indicesToDateMap = new HashMap<>();
        for (int i = 0; i < 30; i++) {
            cal.setTimeInMillis(System.currentTimeMillis());
            cal.add(Calendar.DATE, -29 + i);
            indicesToDateMap.put(i, df.format(cal.getTimeInMillis()));
        }

        Iterator mapIterator = indicesToDateMap.entrySet().iterator();
        while (mapIterator.hasNext()) {
            Map.Entry map = (Map.Entry) mapIterator.next();
            if (sentDates.containsKey(map.getValue())) {
                sentEntries.add(new Entry((Integer) map.getKey(), sentDates.get(map.getValue())));
                maxValue = sentDates.get(map.getValue()) > maxValue ? sentDates.get(map.getValue()) : maxValue;
            } else {
                sentEntries.add(new Entry((Integer) map.getKey(), -1));
            }

            if (receiveDates.containsKey(map.getValue())) {
                receiveEntries.add(new Entry((Integer) map.getKey(), receiveDates.get(map.getValue())));
                maxValue = receiveDates.get(map.getValue()) > maxValue ? receiveDates.get(map.getValue()) : maxValue;
            } else {
                receiveEntries.add(new Entry((Integer) map.getKey(), -1));
            }

            if (reviewDates.containsKey(map.getValue())) {
                reviewEntries.add(new Entry((Integer) map.getKey(), reviewDates.get(map.getValue())));
                maxValue = reviewDates.get(map.getValue()) > maxValue ? reviewDates.get(map.getValue()) : maxValue;
            } else {
                reviewEntries.add(new Entry((Integer) map.getKey(), -1));
            }
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

        sentSet.setScatterShapeSize(24f);
        sentSet.setDrawValues(false);
        receiveSet.setScatterShapeSize(22f);
        receiveSet.setDrawValues(false);
        reviewSet.setScatterShapeSize(24f);
        reviewSet.setDrawValues(false);

        XAxis axisX = detailedChart.getXAxis();
        YAxis axisYR = detailedChart.getAxisRight();
        YAxis axisYL = detailedChart.getAxisLeft();
        axisYL.setTypeface(Typeface.DEFAULT_BOLD);
        axisX.setTypeface(Typeface.DEFAULT_BOLD);
        axisYR.setEnabled(false);
        axisX.setPosition(XAxis.XAxisPosition.BOTTOM);
        axisX.setDrawGridLines(false);
        axisYR.setDrawGridLines(false);
        axisX.setDrawLabels(true);
        axisYL.setAxisMinimum(0);
        axisYL.setValueFormatter(new LargeValueFormatter());
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
        axisX.setLabelCount(30);
        axisX.setGranularity(1);

        axisX.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                String[] dateStrings = indicesToDateMap.get((int) value).split("/");
                return dateStrings[0] + "/" + dateStrings[1];
            }
        });

        ArrayList<IScatterDataSet> dataSets = new ArrayList<>();
        dataSets.add(sentSet); // add the data sets
        dataSets.add(receiveSet);
        dataSets.add(reviewSet);

        // create a data object with the data sets
        ScatterData data = new ScatterData(dataSets);

        detailedChart.setData(data);
        detailedChart.setDoubleTapToZoomEnabled(false);
        detailedChart.setPinchZoom(false);
        detailedChart.setDescription(null);
        detailedChart.setHorizontalScrollBarEnabled(true);
        detailedChart.setVisibleXRangeMaximum(8);
        detailedChart.moveViewToX(22);
    }
}
