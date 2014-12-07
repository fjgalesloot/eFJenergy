package nl.galesloot_ict.efjenergy.PowerUsage;

import android.app.Activity;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.Calendar;
import java.util.Date;

import nl.galesloot_ict.efjenergy.JsonSpiceService;
import nl.galesloot_ict.efjenergy.R;
import nl.galesloot_ict.efjenergy.SettingsActivity;
import nl.galesloot_ict.efjenergy.chart.PowerUsageChart;

/**
 * Created by FlorisJan on 16-11-2014.
 */
public class FragmentPowerUsage extends Fragment {

    protected SpiceManager spiceManager = new SpiceManager(JsonSpiceService.class);
    protected String lastRequestCacheKey = "";
    protected MenuItem refreshMenuItem;

    MenuItem refresh;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        performRefresh();
        //setContentView(R.layout.activity_main);
    }

    public void onResume() {
        super.onResume();
        performRefresh();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_powerusage, container, false);

    }

    @Override
    public void onStart() {
        super.onStart();
        spiceManager.start(getActivity());
    }

    @Override
    public void onStop() {
        spiceManager.shouldStop();
        super.onStop();
    }

    private void performRefresh() {
        //this.setProgressBarIndeterminateVisibility(true);

        Calendar startCalender = Calendar.getInstance();
        Date endDate   = new Date();
        startCalender.add(Calendar.HOUR, -1);
        Date startDate = startCalender.getTime();

        PowerUsageRequest request = new PowerUsageRequest(getString(R.string.api_getpowerusage), startDate, endDate, 100,  this.getActivity());
        lastRequestCacheKey = request.createCacheKey();
        spiceManager.execute(request, lastRequestCacheKey, DurationInMillis.ONE_MINUTE, new PowerUsageRequestListener());
    }

    private class PowerUsageRequestListener implements RequestListener<PowerUsage> {

        @Override
        public void onRequestFailure(SpiceException e) {
            Toast.makeText(getActivity(), getString(R.string.PowerUsageReadingRequestFailed), Toast.LENGTH_SHORT).show();            //update your UI
        }

        @Override
        public void onRequestSuccess(PowerUsage powerUsage) {
            if ( isAdded()) {
                if ( powerUsage.getMeasurementStart() != null) {
                }
                if ( powerUsage.getMeasurementEnd() != null) {
                }
                if ( powerUsage.getTotalkWh() != 0 ) {
                    ((TextView) getView().findViewById(R.id.textViewTotalkWh)).setText(String.format("%.2f", powerUsage.getTotalkWh()) + " kWh");
                }
                else {
                }
                if ( !powerUsage.getPowerUsageReadings().isEmpty() ) {

                    LinearLayout layout = (LinearLayout) getView().findViewById(R.id.powerusage_chart);

                    PowerUsageChart powerUsageChart = new PowerUsageChart();
                    GraphicalView chartView = powerUsageChart.execute(getActivity(), powerUsage.getPowerUsageReadings());
                    if ( layout != null ) {
                        layout.removeAllViewsInLayout();
                        layout.addView(chartView);
                    }
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == refreshMenuItem.getItemId()) {
            performRefresh();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Add your menu entries here
        refreshMenuItem = menu.add(getString(R.string.action_refresh));
        super.onCreateOptionsMenu(menu, inflater);
    }

}