package utils;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import datadefinition.DataType;
import datadefinition.Relation;
import dto.DataExecutionDTO;

import java.io.File;
import java.lang.reflect.Type;

public class DataExecutionDTODeserializer implements JsonDeserializer<DataExecutionDTO> {

    @Override
    public DataExecutionDTO deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();




        // Deserialize data
        String name = jsonObject.get("name").getAsString();
        String type = jsonObject.get("type").getAsString();
        JsonElement dataElement = jsonObject.get("data");

        Object data;

        switch (DataType.valueOf(type.toUpperCase()))
        {
            case LIST:
                Type listType = new TypeToken<java.util.List<File>>() {}.getType();
                data=context.deserialize(dataElement,listType);
                break;
            case RELATION:
                data=context.deserialize(dataElement, Relation.class);
                break;
            case NUMBER:
                data=context.deserialize(dataElement,Integer.class);
                break;
            case DOUBLE:
                data=context.deserialize(dataElement,Double.class);
                break;
            default:
                data = context.deserialize(dataElement, Object.class);
                break;
        }

        return new DataExecutionDTO(name,type,data);
    }
}
