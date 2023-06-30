package utils;

import com.google.gson.*;
import dto.DataDefintionDTO;

import java.lang.reflect.Type;

public class DataDefintionDTODeserializer implements JsonDeserializer<DataDefintionDTO> {
    @Override
    public DataDefintionDTO deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        String name = jsonObject.get("name").getAsString();
        String defaultName=null;
        if(jsonObject.has("defaultName"))
            defaultName = jsonObject.get("defaultName").getAsString();
        String type = jsonObject.get("type").getAsString();

        DataDefintionDTO dataDefintionDTO;

        if(defaultName!=null)
            dataDefintionDTO=new DataDefintionDTO(name, defaultName, type);
        else
            dataDefintionDTO=new DataDefintionDTO(name,type);

        return dataDefintionDTO;
    }
}

