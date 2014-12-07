package nl.galesloot_ict.efjenergy.helpers;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by FlorisJan on 7-12-2014.
 */
public class DateHelper {
    public static Date RoundQuarterHourDown ( Date input ) {
        return RoundQuarterHour( input, true );
    }

    public static Date RoundQuarterHourUp( Date input ) {
        return RoundQuarterHour( input, false );
    }

    public static Date RoundQuarterHour( Date input ) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(input);
        int unroundedMinutes = calendar.get(Calendar.MINUTE);
        int mod = unroundedMinutes % 15;
        calendar.add(Calendar.MINUTE, mod < 8 ? -mod : (15-mod));
        calendar.clear(Calendar.SECOND);
        calendar.clear(Calendar.MILLISECOND);
        return calendar.getTime();
    }

    private static Date RoundQuarterHour( Date input, Boolean down ) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(input);
        int unroundedMinutes = calendar.get(Calendar.MINUTE);
        int mod = unroundedMinutes % 15;
        calendar.add(Calendar.MINUTE, down ? -mod : (15-mod));
        calendar.clear(Calendar.SECOND);
        calendar.clear(Calendar.MILLISECOND);
        return calendar.getTime();
    }
}
