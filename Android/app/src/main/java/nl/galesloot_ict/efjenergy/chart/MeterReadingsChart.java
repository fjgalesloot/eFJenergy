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
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;


import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nl.galesloot_ict.efjenergy.MainActivity;
import nl.galesloot_ict.efjenergy.MeterReadingsList;
import nl.galesloot_ict.efjenergy.R;


/**
 * Sales demo bar chart.
 */
public class MeterReadingsChart { //extends AbstractChart {

  /**
   * Executes the chart demo.
   * 
   * @param context the context
   * @return the built intent
   */
    public GraphicalView execute(Context context, MeterReadingsList meterReadingsList) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();

        int[] colors = new int[]{0xFF3D7719, 0xFF0766FF, 0xFFA500FF };
        XYMultipleSeriesRenderer renderer = buildRenderer(colors, context);

        renderer.setChartTitle( context.getString(R.string.MeterReadingsBarChartTitle));
        renderer.setXTitle("");
        renderer.setYTitle(context.getString(R.string.MaterReadingsBarChartTitleY));
        renderer.setAxesColor(Color.DKGRAY);
        renderer.setLabelsColor(Color.LTGRAY);
        //renderer.setXAxisMin(xMin);
        //renderer.setXAxisMax(xMax);
        renderer.setYAxisMin(0);
        //renderer.setYAxisMax(yMax);



        renderer.setXLabels(7);
        //renderer.addXTextLabel(1, "label");
        //renderer.addXTextLabel(7, "label");
        //renderer.setXLabelsAngle(90.0f);
        renderer.setYLabels(10);
        //renderer.setXLabelsAlign(Align.LEFT);
        //renderer.setYLabelsAlign(Align.RIGHT);
        renderer.setPanEnabled(true, true);
        renderer.setZoomEnabled(true);
        renderer.setZoomRate(1.1f);
        renderer.setBarSpacing(0.5f);
        renderer.setShowGridX(true);

        float margin;
        int[] margins = new int[]{
                (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 25, metrics), // top
                (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 65, metrics), // left
                (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 100, metrics), // bottom
                (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, metrics) // right
        };

        renderer.setMargins(margins);

        renderer.setLabelsColor(Color.BLACK);
        renderer.setBackgroundColor(Color.WHITE);
        renderer.setMarginsColor(Color.WHITE);

        renderer.setFitLegend(false);
        renderer.setLegendHeight(-100);

        return ChartFactory.getTimeChartView(context, buildDataset(context, meterReadingsList), renderer, "E"  );
    }

    protected XYMultipleSeriesDataset buildDataset(Context context, MeterReadingsList meterReadingsList) {
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        TimeSeries seriesTarif1 = new TimeSeries(context.getString(R.string.title_tarif1));
        TimeSeries seriesTarif2 = new TimeSeries(context.getString(R.string.title_tarif2));
        TimeSeries seriesTariftotal = new TimeSeries(context.getString(R.string.title_tariftotal));
        int length = meterReadingsList.size();
        for (int i = 0; i < length; i++) {
            seriesTarif1.add( (Date) meterReadingsList.get(i).getMeasurementTimestamp(), Math.round(meterReadingsList.get(i).getTotalkWhTarif1()) );
            seriesTarif2.add( (Date) meterReadingsList.get(i).getMeasurementTimestamp(), Math.round(meterReadingsList.get(i).getTotalkWhTarif2()) );
            seriesTariftotal.add( (Date) meterReadingsList.get(i).getMeasurementTimestamp(), Math.round(meterReadingsList.get(i).getTotalkWhTarif1()+meterReadingsList.get(i).getTotalkWhTarif2()) );
        }
        dataset.addSeries(seriesTarif1);
        dataset.addSeries(seriesTarif2);
        dataset.addSeries(seriesTariftotal);
        return dataset;
    }

    protected XYMultipleSeriesRenderer buildRenderer(int[] colors, Context context) {
        float val;
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();

        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
        renderer.setAxisTitleTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16, metrics));
        renderer.setChartTitleTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20, metrics));
        renderer.setLabelsTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 15, metrics));
        renderer.setLegendTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 15, metrics));
        int length = colors.length;
        for (int i = 0; i < length; i++) {
            XYSeriesRenderer r = new XYSeriesRenderer();
            r.setChartValuesTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, metrics));
            r.setColor(colors[i]);
            r.setDisplayChartValues(true);
            renderer.addSeriesRenderer(r);
        }
        return renderer;
    }

}
