package utils;

import com.google.gson.*;
import dto.DataDefintionDTO;
import dto.StepExtensionDTO;

import java.lang.reflect.Type;
import java.util.Map;

public class StepExtensionDTOSerializer implements JsonSerializer<StepExtensionDTO> {
    @Override
    public JsonElement serialize(StepExtensionDTO stepExtensionDTO, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();

        // Serialize logs
        if (stepExtensionDTO.getLogs() != null) {
            JsonArray logsArray = new JsonArray();
            for (String log : stepExtensionDTO.getLogs()) {
                logsArray.add(new JsonPrimitive(log));
            }
            jsonObject.add("logs", logsArray);
        }

        return jsonObject;
    }
}

