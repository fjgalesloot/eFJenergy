package nl.galesloot_ict.efjenergy;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Date;

/**
 * Created by FlorisJan on 16-11-2014.
 */

//@JsonIgnoreProperties(ignoreUnknown = true)
public class MeterReading {
    private float TotalkWhTarif1;
    private float TotalkWhTarif2;
    private Date MeasurementTimestamp;

    @JsonProperty("TotalkWhTarif1")
    public float getTotalkWhTarif1() {
        return TotalkWhTarif1;
    }

    @JsonProperty("TotalkWhTarif1")
    public void setTotalkWhTarif1(float TotalkWhTarif1) {
        this.TotalkWhTarif1 = TotalkWhTarif1;
    }

    @JsonProperty("TotalkWhTarif2")
    public float getTotalkWhTarif2() {
        return TotalkWhTarif2;
    }

    @JsonProperty("TotalkWhTarif2")
    public void setTotalkWhTarif2(float TotalkWhTarif2) {
        this.TotalkWhTarif2 = TotalkWhTarif2;
    }

    @JsonProperty("MeasurementTimestamp")
    public Date getMeasurementTimestamp() {
        return MeasurementTimestamp;
    }

    @JsonProperty("MeasurementTimestamp")
    public void setMeasurementTimestamp(Date MeasurementTimestamp) {
        this.MeasurementTimestamp = MeasurementTimestamp;
    }

    @JsonCreator
    public static MeterReading Create(String jsonString) throws JsonParseException, JsonMappingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        MeterReading meterReading = null;
        meterReading = mapper.readValue(jsonString, MeterReading.class);
        return meterReading;
    }
}
