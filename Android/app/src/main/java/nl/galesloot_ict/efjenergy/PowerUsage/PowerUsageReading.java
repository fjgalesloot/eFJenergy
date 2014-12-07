package nl.galesloot_ict.efjenergy.PowerUsage;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

/**
 * Created by FlorisJan on 7-12-2014.
 */
public class PowerUsageReading {
    private float WattAverage, WattMinimum, WattMaximum;
    private Date MeasurementDate;

    @JsonProperty("WattAverage")
    public float getWattAverage() {
        return WattAverage;
    }

    @JsonProperty("WattAverage")
    public void setWattAverage(float WattAverage) {
        this.WattAverage = WattAverage;
    }

    @JsonProperty("WattLow")
    public float getWattMinimum() {
        return WattMinimum;
    }

    @JsonProperty("WattLow")
    public void setWattMinimum(float WattMinimum) {
        this.WattMinimum = WattMinimum;
    }

    @JsonProperty("WattHigh")
    public float getWattMaximum() {
        return WattMaximum;
    }

    @JsonProperty("WattHigh")
    public void setWattMaximum(float WattMaximum) {
        this.WattMaximum = WattMaximum;
    }

    @JsonProperty("MeasurementTimestamp")
    public Date getMeasurementDate() {
        return MeasurementDate;
    }

    @JsonProperty("MeasurementTimestamp")
    public void setMeasurementDate(Date MeasurementDate) {
        this.MeasurementDate = MeasurementDate;
    }

}
