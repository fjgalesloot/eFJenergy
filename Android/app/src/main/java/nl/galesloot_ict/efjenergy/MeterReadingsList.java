package nl.galesloot_ict.efjenergy;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;

import nl.galesloot_ict.efjenergy.MeterReading;

/**
 * Created by FlorisJan on 23-11-2014.
 */
public class MeterReadingsList extends ArrayList<MeterReading> {

    @JsonCreator
    public static MeterReadingsList Create(String jsonString) throws JsonParseException, JsonMappingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        MeterReadingsList meterReading = null;
        meterReading = mapper.readValue(jsonString, MeterReadingsList.class);
        return meterReading;
    }
}
