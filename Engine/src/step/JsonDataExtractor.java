package step;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import datadefinition.*;

import java.io.File;

public class JsonDataExtractor extends Step{
    public JsonDataExtractor(String name, boolean continue_if_failing) {
        super(name, true, continue_if_failing);
        defaultName = "Json Data Extractor";

        DataJson dataJson = new DataJson("JSON");
        inputs.add(new Input(dataJson, true, true, "Json source"));
        nameToInputIndex.put("JSON", 0);

        DataString dataString = new DataString("JSON_PATH");
        inputs.add(new Input(dataString, true, true, "Data"));
        nameToInputIndex.put("JSON_PATH", 1);

        outputs.add(new Output(new DataString("VALUE"), "Data value"));
        nameToOutputIndex.put("VALUE", 0);

    }


    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        String jsonData = (String) inputs.get(0).getData();
        String jsonPaths = (String) inputs.get(1).getData();
        String res = "";

        if (!checkGotInputs(2)) {
            runTime = System.currentTimeMillis() - startTime;
            return;
        }
        boolean isSuccessful = true;

        String[] jsonPathsArr = jsonPaths.split("\\|");
        for(int i = 0; i < jsonPathsArr.length && isSuccessful; i++ ) {
            String jsonPath = jsonPathsArr[i];
            try {
                String data = JsonPath.read(jsonData, jsonPath).toString();
                res = res + data + ", ";
                addLineToLog("Extracting data " + jsonPath + ". Value: " + data);
            }
            catch (PathNotFoundException e) {
                addLineToLog("No value found for json path " + jsonPath);
            }
            catch (Exception e) {
                isSuccessful = false;
                stateAfterRun = State.FAILURE;
                addLineToLog("Filed to extract data for path: " + jsonPath);
                summaryLine = "Step failed, there was a problem retrieving data from the JSON data";
            }
        }

        if(isSuccessful) {
            stateAfterRun = State.SUCCESS;
            summaryLine = "Step ended successfully, retrieved available data from the JSON data";
            if(!res.equals(""))
                res = res.substring(0, res.length() -2);

            outputs.get(0).setData(res);
        }

        runTime = System.currentTimeMillis() - startTime;

    }
}
