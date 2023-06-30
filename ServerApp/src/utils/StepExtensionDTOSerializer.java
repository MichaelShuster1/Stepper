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

        // Serialize inputs
        if (stepExtensionDTO.getInputs() != null) {
            JsonObject inputsJsonObject = new JsonObject();
            for (Map.Entry<DataDefintionDTO, Object> entry : stepExtensionDTO.getInputs().entrySet()) {
                JsonElement keyElement = context.serialize(entry.getKey());
                JsonElement valueElement = context.serialize(entry.getValue());
                inputsJsonObject.add(keyElement.getAsJsonObject().toString(), valueElement);
            }
            jsonObject.add("inputs", inputsJsonObject);
        }

        // Serialize outputs
        if (stepExtensionDTO.getOutputs() != null) {
            JsonObject outputsJsonObject = new JsonObject();
            for (Map.Entry<DataDefintionDTO, Object> entry : stepExtensionDTO.getOutputs().entrySet()) {
                JsonElement keyElement = context.serialize(entry.getKey());
                JsonElement valueElement = context.serialize(entry.getValue());
                outputsJsonObject.add(keyElement.getAsJsonObject().toString(), valueElement);
            }
            jsonObject.add("outputs", outputsJsonObject);
        }

        return jsonObject;
    }
}

