/**
 * Copyright (C) 2009 - 2013 SC 4ViewSoft SRL
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.galesloot_ict.efjenergy.chart;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.BasicStroke;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import nl.galesloot_ict.efjenergy.MeterReading.MeterReadingsList;
import nl.galesloot_ict.efjenergy.PowerUsage.PowerUsage;
import nl.galesloot_ict.efjenergy.PowerUsage.PowerUsageReading;
import nl.galesloot_ict.efjenergy.R;
import nl.galesloot_ict.efjenergy.helpers.DateHelper;


/**
 * Sales demo bar chart.
 */
public class PowerUsageChart { //extends AbstractChart {

  /**
   * Executes the chart demo.
   * 
   * @param context the context
   * @return the built intent
   */

    protected Date minTime=new Date(Long.MAX_VALUE), maxTime = new Date(0);
    protected Float minWatt = Float.MAX_VALUE, maxWatt = Float.MIN_VALUE;

    public GraphicalView execute(Context context, ArrayList<PowerUsageReading> powerUsageReadings) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();

        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();

        renderer.setAxisTitleTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16, metrics));
        renderer.setChartTitleTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20, metrics));
        renderer.setLabelsTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 15, metrics));
        renderer.setLegendTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 15, metrics));


        int[] colors = new int[]{Color.BLUE, 0xFF007F46, 0xFF7F0000};
        int length = colors.length;
        for (int i = 0; i < length; i++) {
            XYSeriesRenderer r = new XYSeriesRenderer();
            r.setDisplayChartValues(false);
            r.setColor(colors[i]);
            r.setLineWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 1, metrics));
            renderer.addSeriesRenderer(r);
        }
       colors = new int[]{0xFF007F46, 0xFF7F0000};
        length = colors.length;
        for (int i = 0; i < length; i++) {
            XYSeriesRenderer r = new XYSeriesRenderer();
            r.setDisplayChartValues(false);
            r.setColor(colors[i]);
            r.setLineWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 1, metrics));
            r.setStroke(BasicStroke.DASHED);
            r.setAnnotationsTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 15, metrics));
            r.setAnnotationsColor(colors[i]);
            r.setAnnotationsTextAlign(Align.RIGHT);
            r.setShowLegendItem(false);
            renderer.addSeriesRenderer(r);
        }



        renderer.setChartTitle("");
        renderer.setXTitle("");
        renderer.setYTitle(context.getString(R.string.puchart_titley));

        renderer.setAxesColor(Color.BLACK);
        renderer.setLabelsColor(Color.LTGRAY);
        renderer.setXLabelsColor(Color.DKGRAY);
        renderer.setYLabelsPadding(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 3, metrics));



        renderer.setXLabels(0);
        //renderer.setXLabelsAngle(90.0f);
        renderer.setYLabels(5);

        renderer.setXLabelsAlign(Align.CENTER);
        renderer.setYLabelsAlign(Align.RIGHT );
        renderer.setPanEnabled(true, true);
        renderer.setZoomEnabled(true);
        renderer.setZoomRate(1.1f);
        renderer.setBarSpacing(0.5f);
        renderer.setShowGridX(true);
        renderer.setShowGridY(true);
        renderer.setGridColor(Color.DKGRAY);

        float margin;
        int[] margins = new int[]{
                (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 25, metrics), // top
                (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 65, metrics), // left
                (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 35, metrics), // bottom
                (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, metrics) // right
        };

        renderer.setMargins(margins);

        renderer.setLabelsColor(Color.BLACK);
        renderer.setBackgroundColor(Color.WHITE);
        renderer.setMarginsColor(Color.WHITE);

        renderer.setFitLegend(true);
        //renderer.setLegendHeight(-100);
        XYMultipleSeriesDataset dataset = buildDataset(context, powerUsageReadings);


        Date minDateLabel = DateHelper.RoundQuarterHour(minTime);
        Date maxDateLabel = DateHelper.RoundQuarterHour(maxTime);

        Long minDateAxis = minTime.getTime();
        if ( minDateAxis > minDateLabel.getTime() ) {
            minDateAxis = minDateLabel.getTime();
        }
        Long maxDateAxis = maxTime.getTime();
        if ( maxDateAxis < maxDateLabel.getTime() ) {
            maxDateAxis = maxDateLabel.getTime();
        }

        renderer.setXAxisMin(minDateAxis-240000);
        renderer.setXAxisMax(maxDateAxis+240000);
        renderer.setYAxisMin(minWatt * 0.9f);
        renderer.setYAxisMax(maxWatt * 1.1f);

        SimpleDateFormat sdf = new SimpleDateFormat("k:mm");
        for ( Long labelDate = minDateLabel.getTime(); Math.round(labelDate/1000) <= Math.round(maxDateLabel.getTime()/1000); labelDate+=900000 ){
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(labelDate);
            renderer.addXTextLabel(labelDate, sdf.format(calendar.getTime()));
        }
        renderer.setShowCustomTextGrid(true);

        return ChartFactory.getTimeChartView(context, dataset, renderer, "k:mm"  );
    }

    protected XYMultipleSeriesDataset buildDataset(Context context, ArrayList<PowerUsageReading> powerUsageReadings) {
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        TimeSeries seriesPowerUsageAvg = new TimeSeries(context.getString(R.string.puchart_avg));
        TimeSeries seriesPowerUsageMin = new TimeSeries(context.getString(R.string.puchart_min));
        TimeSeries seriesPowerUsageMax = new TimeSeries(context.getString(R.string.puchart_max));

        int length = powerUsageReadings.size();
        for (int i = 0; i < length; i++) {
            Date date = (Date) powerUsageReadings.get(i).getMeasurementDate();
            if ( date.getTime() < minTime.getTime() ) minTime = date;
            if ( date.getTime() > maxTime.getTime() ) maxTime = date;

            float value = Math.round(powerUsageReadings.get(i).getWattAverage());
            seriesPowerUsageAvg.add( date , value );
            value = Math.round(powerUsageReadings.get(i).getWattMinimum());
            if ( value < minWatt ) minWatt = value;
            seriesPowerUsageMin.add( date, value );
            value = Math.round(powerUsageReadings.get(i).getWattMaximum());
            if ( value > maxWatt ) maxWatt = value;
            seriesPowerUsageMax.add( date, value );
        }
        dataset.addSeries(seriesPowerUsageAvg);
        dataset.addSeries(seriesPowerUsageMin);
        dataset.addSeries(seriesPowerUsageMax);

        XYSeries minPower = new XYSeries("");
        minPower.add(minTime.getTime(), minWatt);
        minPower.add(maxTime.getTime(), minWatt);
        dataset.addSeries(minPower);

        XYSeries maxPower = new XYSeries("");
        maxPower.add(minTime.getTime(), maxWatt);
        maxPower.add(maxTime.getTime(), maxWatt);
        dataset.addSeries(maxPower);

        return dataset;
    }

}
