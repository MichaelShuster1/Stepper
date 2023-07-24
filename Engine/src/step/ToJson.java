package step;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import datadefinition.*;

public class ToJson extends Step{
    public ToJson(String name, boolean continue_if_failing) {
        super(name, true, continue_if_failing);
        defaultName = "To Json";

        DataString dataString = new DataString("CONTENT");
        inputs.add(new Input(dataString, true, true, "Content"));
        nameToInputIndex.put("CONTENT", 0);

        DataJson dataJson = new DataJson("JSON");
        outputs.add(new Output(dataJson, "Json representation"));
        nameToOutputIndex.put("JSON", 0);
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        String content = (String) inputs.get(0).getData();

        if (!checkGotInputs(1)) {
            runTime = System.currentTimeMillis() - startTime;
            return;
        }

        JsonParser jsonParser = new JsonParser();

        // Check if the JSON is in valid format
        try {
            Gson gson = new Gson();
            Object o = gson.fromJson(content, Object.class);
            addLineToLog("Content is JSON string. Converting it to json…");
            summaryLine = "Step ended successfully, converted the string to JSON";
            outputs.get(0).setData(content);
            stateAfterRun = State.SUCCESS;
        } catch (Exception e) {
            stateAfterRun = State.FAILURE;
            addLineToLog("Content is not a valid JSON representation");
            summaryLine = "Step failed, the given string is not a valid JSON representation";
        }
        runTime = System.currentTimeMillis() - startTime;
    }
}
