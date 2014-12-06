package nl.galesloot_ict.efjenergy;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.octo.android.robospice.Jackson2SpringAndroidSpiceService;
import com.octo.android.robospice.JacksonSpringAndroidSpiceService;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import org.achartengine.GraphicalView;

import nl.galesloot_ict.efjenergy.chart.MeterReadingsChart;

/**
 * Created by FlorisJan on 16-11-2014.
 */
public class FragmentMeterReadings extends Fragment {

    protected SpiceManager spiceManager = new SpiceManager(JsonSpiceService.class);
    protected String lastRequestCacheKey = "";

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
        return inflater.inflate(R.layout.fragment_meterreadings, container, false);

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

        MeterReadingsRequest request = new MeterReadingsRequest("getmeterreadingslastweek", this.getActivity());
        lastRequestCacheKey = request.createCacheKey();
        spiceManager.execute(request, lastRequestCacheKey, DurationInMillis.ONE_MINUTE, new ListMeterReadingsRequestListener());
    }

    private class ListMeterReadingsRequestListener implements RequestListener<MeterReadingsList> {

        @Override
        public void onRequestFailure(SpiceException e) {
            Toast.makeText(getActivity(), getString(R.string.MeterReadingFailed), Toast.LENGTH_SHORT).show();            //update your UI
        }

        @Override
        public void onRequestSuccess(MeterReadingsList meterReadingsList) {
            if ( isAdded()) {
                if ( !meterReadingsList.isEmpty() ) {
                    ((TextView) getView().findViewById(R.id.textViewTarif1)).setText(String.valueOf(meterReadingsList.get(meterReadingsList.size()-1).getTotalkWhTarif1()) + " kWh");
                    ((TextView) getView().findViewById(R.id.textViewTarif2)).setText(String.valueOf(meterReadingsList.get(meterReadingsList.size()-1).getTotalkWhTarif2()) + " kWh");

                    MeterReadingsChart meterReadingsBarChart = new MeterReadingsChart();
                    GraphicalView chartView = meterReadingsBarChart.execute(getActivity(), meterReadingsList);
                    LinearLayout layout = (LinearLayout) getView().findViewById(R.id.meterreadings_chart);
                    if ( layout != null ) {
                        layout.removeAllViewsInLayout();
                        layout.addView(chartView);
                    }
                }
            }
        }
    }
}

