package utils;

import com.google.gson.*;
import dto.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FlowExecutionDTODeserializer implements JsonDeserializer<FlowExecutionDTO> {
    @Override
    public FlowExecutionDTO deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();


        // Deserialize FlowExecutionDetailsDTO fields
        String name = jsonObject.get("name").getAsString();
        String id = jsonObject.get("id").getAsString();
        String activationTime = jsonObject.get("activationTime").getAsString();

        String stateAfterRun=null;
        if(jsonObject.has("stateAfterRun"))
            stateAfterRun = jsonObject.get("stateAfterRun").getAsString();

        Long runTime=null;
        if(jsonObject.has("runTime"))
            runTime = jsonObject.get("runTime").getAsLong();


        FlowExecutionDetailsDTO flowExecutionDetailsDTO;
        if(stateAfterRun!=null)
             flowExecutionDetailsDTO= new FlowExecutionDetailsDTO(name,id,stateAfterRun,activationTime,runTime);
        else
            flowExecutionDetailsDTO=new FlowExecutionDetailsDTO(name,id,activationTime);


        // Deserialize steps
        List<StepExecutionDTO> steps = new ArrayList<>();
        if (jsonObject.has("steps")) {
            JsonArray stepsJsonArray = jsonObject.getAsJsonArray("steps");
            for (JsonElement stepJsonElement : stepsJsonArray) {
                StepExecutionDTO step = context.deserialize(stepJsonElement, StepExecutionDTO.class);
                steps.add(step);
            }
        }

        // Deserialize freeInputs
        List<FreeInputExecutionDTO> freeInputs = new ArrayList<>();
        if (jsonObject.has("freeInputs")) {
            JsonArray freeInputsJsonArray = jsonObject.getAsJsonArray("freeInputs");
            for (JsonElement freeInputJsonElement : freeInputsJsonArray) {
                FreeInputExecutionDTO freeInput = context.deserialize(freeInputJsonElement, FreeInputExecutionDTO.class);
                freeInputs.add(freeInput);
            }
        }

        // Deserialize outputs
        List<DataExecutionDTO> outputs = new ArrayList<>();
        if (jsonObject.has("outputs")) {
            JsonArray outputsJsonArray = jsonObject.getAsJsonArray("outputs");
            for (JsonElement outputJsonElement : outputsJsonArray) {
                DataExecutionDTO output = context.deserialize(outputJsonElement, DataExecutionDTO.class);
                outputs.add(output);
            }
        }

        double progress =jsonObject.get("progress").getAsDouble();


        return new FlowExecutionDTO(flowExecutionDetailsDTO,steps,freeInputs,outputs,progress);
    }
}
