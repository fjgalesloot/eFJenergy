package nl.galesloot_ict.efjenergy.MeterReading;

import android.content.Context;

import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

import java.util.Date;

import nl.galesloot_ict.efjenergy.helpers.APIHelper;

/**
 * Created by FlorisJan on 16-11-2014.
 */
public class MeterReadingsRequest extends SpringAndroidSpiceRequest<MeterReadingsList> {

    private String apiFunction ="";
    Context context;

    public MeterReadingsRequest(String apiFunction, Context context ) {
        super(MeterReadingsList.class);
        this.apiFunction = apiFunction;

        this.context = context;
    }

    @Override
    public MeterReadingsList loadDataFromNetwork() throws Exception {
        APIHelper apiHelper = new APIHelper(context);


        //String url = String.format("http://daffy.internal.triplew.nl/eFJenergy/api/v1/%s?apiKey=01e3567f-7b2a-485a-a382-92dbe620f366",apiFunction);
        String url = apiHelper.GetUrl(apiFunction);

        return getRestTemplate().getForObject(url, MeterReadingsList.class);
    }

    /**
     * This method generates a unique cache key for this request. In this case
     * our cache key depends just on the keyword.
     * @return
     */
    public String createCacheKey() {
        Date now = new Date();
        return "MeterReadings." + now.getTime();
    }
}
