package nl.galesloot_ict.efjenergy.PowerUsage;

import android.content.Context;

import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

import java.text.SimpleDateFormat;
import java.util.Date;

import nl.galesloot_ict.efjenergy.MeterReading.MeterReadingsList;
import nl.galesloot_ict.efjenergy.helpers.APIHelper;

/**
 * Created by FlorisJan on 16-11-2014.
 */
public class PowerUsageRequest extends SpringAndroidSpiceRequest<PowerUsage> {

    private String apiFunction ="";
    private String apiArguments[];
    Context context;

    public PowerUsageRequest(String apiFunction, Date startDateTime, Date endDateTime, int precision, Context context) {
        super(PowerUsage.class);
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

        this.apiFunction = apiFunction;
        this.apiArguments = new String[] {fmt.format(startDateTime), fmt.format(endDateTime), String.valueOf(precision) };
        this.context = context;
    }

    @Override
    public PowerUsage loadDataFromNetwork() throws Exception {

        APIHelper apiHelper = new APIHelper(context);
        String url = apiHelper.GetUrl(apiFunction, apiArguments );
        return getRestTemplate().getForObject(url, PowerUsage.class);
    }

    /**
     * This method generates a unique cache key for this request. In this case
     * our cache key depends just on the keyword.
     * @return
     */
    public String createCacheKey() {
        Date now = new Date();
        return "PowerUsageRequest." + now.getTime();
    }
}
