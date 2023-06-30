package utils;

import com.google.gson.*;
import dto.DataDefintionDTO;
import dto.FlowExecutionDTO;
import dto.StepExtensionDTO;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StepExtensionDTODeserializer  implements JsonDeserializer<StepExtensionDTO> {
    @Override
    public StepExtensionDTO deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        // Deserialize logs
        List<String> logs = context.deserialize(jsonObject.get("logs"), List.class);

        // Deserialize inputs
        JsonObject inputsJsonObject = jsonObject.getAsJsonObject("inputs");
        Map<DataDefintionDTO,Object> inputs = new HashMap<>();
        if (inputsJsonObject != null) {
            for (Map.Entry<String, JsonElement> entry : inputsJsonObject.entrySet()) {
                DataDefintionDTO key = context.deserialize(new JsonPrimitive(entry.getKey()), DataDefintionDTO.class);
                Object value = context.deserialize(entry.getValue(), String.class);
                inputs.put(key, value);
            }
        }

        // Deserialize outputs
        JsonObject outputsJsonObject = jsonObject.getAsJsonObject("outputs");
        Map<DataDefintionDTO, Object> outputs = new HashMap<>();
        if (outputsJsonObject != null) {
            for (Map.Entry<String, JsonElement> entry : outputsJsonObject.entrySet()) {
                DataDefintionDTO key = context.deserialize(new JsonPrimitive(entry.getKey()), DataDefintionDTO.class);
                Object value = context.deserialize(entry.getValue(), Object.class);
                outputs.put(key, value);
            }
        }

        // Create and return the StepExtensionDTO object
        StepExtensionDTO stepExtensionDTO = new StepExtensionDTO(logs,inputs,outputs);

        return stepExtensionDTO;
    }
}
