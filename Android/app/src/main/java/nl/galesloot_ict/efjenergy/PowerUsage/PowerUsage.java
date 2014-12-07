package nl.galesloot_ict.efjenergy.PowerUsage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by FlorisJan on 07-12-2014.
 */

//@JsonIgnoreProperties(ignoreUnknown = true)
public class PowerUsage {
    private float TotalkWh;
    private Date MeasurementStart, MeasurementEnd;
    private ArrayList<PowerUsageReading> powerUsageReadingArrayList;

    @JsonProperty("TotalkWh")
    public float getTotalkWh() {
        return TotalkWh;
    }

    @JsonProperty("TotalkWh")
    public void setTotalkWh(float TotalkWh) {
        this.TotalkWh = TotalkWh;
    }

    @JsonProperty("MeasurementStart")
    public Date getMeasurementStart() {
        return MeasurementStart;
    }

    @JsonProperty("MeasurementStart")
    public void setMeasurementStart(Date MeasurementStart) {
        this.MeasurementStart = MeasurementStart;
    }

    @JsonProperty("MeasurementEnd")
    public Date getMeasurementEnd() {
        return MeasurementEnd;
    }

    @JsonProperty("MeasurementEnd")
    public void setMeasurementEnd(Date MeasurementEnd) {
        this.MeasurementEnd = MeasurementEnd;
    }

    @JsonProperty("PowerUsageReadings")
    public ArrayList<PowerUsageReading> getPowerUsageReadings() {
        return powerUsageReadingArrayList;
    }

    @JsonProperty("PowerUsageReadings")
    public void setPowerUsageReadings(ArrayList<PowerUsageReading> PowerUsageReadings) {
        this.powerUsageReadingArrayList = PowerUsageReadings;
    }


    @JsonCreator
    public static PowerUsage Create(String jsonString) throws JsonParseException, JsonMappingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        PowerUsage powerUsage = null;
        powerUsage = mapper.readValue(jsonString, PowerUsage.class);
        return powerUsage;
    }
}
