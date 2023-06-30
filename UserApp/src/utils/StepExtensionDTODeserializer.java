package utils;

import com.google.gson.*;
import dto.DataDefintionDTO;
import dto.DataExecutionDTO;
import dto.FlowExecutionDTO;
import dto.StepExtensionDTO;

import java.lang.reflect.Type;
import java.util.ArrayList;
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
        JsonArray inputsArray = jsonObject.getAsJsonArray("inputs");
        List<DataExecutionDTO> inputs = new ArrayList<>();
        for (JsonElement element : inputsArray) {
            DataExecutionDTO dataExecutionDTO = context.deserialize(element, DataExecutionDTO.class);
            inputs.add(dataExecutionDTO);
        }

        // Deserialize outputs
        JsonArray outputsArray = jsonObject.getAsJsonArray("outputs");
        List<DataExecutionDTO> outputs = new ArrayList<>();
        for (JsonElement element : outputsArray) {
            DataExecutionDTO dataExecutionDTO = context.deserialize(element, DataExecutionDTO.class);
            outputs.add(dataExecutionDTO);
        }

        // Create and return the StepExtensionDTO object
        StepExtensionDTO stepExtensionDTO = new StepExtensionDTO(logs,inputs,outputs);

        return stepExtensionDTO;
    }
}
