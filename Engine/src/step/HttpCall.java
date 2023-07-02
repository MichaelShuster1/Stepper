package step;

import com.google.gson.Gson;
import datadefinition.*;
import okhttp3.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class HttpCall extends Step{


    protected HttpCall(String name, boolean continueIfFailing) {
        super(name, false, continueIfFailing);
        defaultName="HttpCall";

        DataString dataString = new DataString("RESOURCE");
        inputs.add(new Input(dataString, true, true,
                "Resource Name (include query parameters)"));
        nameToInputIndex.put("RESOURCE",0);


        dataString = new DataString("ADDRESS");
        inputs.add(new Input(dataString, true, true, "Domain:Port"));
        nameToInputIndex.put("ADDRESS", 1);

        Set<String> values = new HashSet<>();
        values.add("http");
        values.add("https");
        DataEnumerator dataEnumerator = new DataEnumerator("PROTOCOL", values);
        inputs.add(new Input(dataEnumerator, true, true, "protocol"));
        nameToInputIndex.put("PROTOCOL", 2);

        values = new HashSet<>();
        values.add("GET");
        values.add("PUT");
        values.add("POST");
        values.add("DELETE");
        dataEnumerator = new DataEnumerator("METHOD", values);
        inputs.add(new Input(dataEnumerator, true, false, "Method"));
        nameToInputIndex.put("METHOD", 3);


        DataJson dataJson= new DataJson("BODY");
        inputs.add(new Input(dataJson,true,false,"Request Body"));
        nameToInputIndex.put("BODY",4);

        DataNumber dataNumber=new DataNumber("CODE");
        outputs.add(new Output(dataNumber,"Response code"));
        nameToOutputIndex.put("CODE",0);

        dataString=new DataString("RESPONSE_BODY");
        outputs.add(new Output(dataString,"Response body"));
        nameToOutputIndex.put("RESPONSE_BODY",1);
    }


    @Override
    public void run() {
        Long startTime = System.currentTimeMillis();

        if (!checkGotInputs(3)) {
            addLineToLog("One or more of the necessary inputs not received!");
            summaryLine="One or more of the necessary inputs not received!";
            stateAfterRun=State.FAILURE;
            runTime = System.currentTimeMillis() - startTime;
            return;
        }

        String resource = (String) inputs.get(0).getData();
        String address = (String) inputs.get(1).getData();
        String protocol= (String) inputs.get(2).getData();
        String method =(String) inputs.get(3).getData();
        String body=(String) inputs.get(4).getData();

        if(method==null)
            method="GET";

        String finalUrl=protocol+"//"+address+"/"+resource;
        Request request=buildRequest(finalUrl,method,body);

        OkHttpClient httpClient=new OkHttpClient();

        addLineToLog("About to invoke http request: "+protocol+" |"
                +method+" |"+address+" |"+resource);

        Call call=httpClient.newCall(request);

        try (Response response = call.execute()) {
            int code= response.code();

            addLineToLog("Received Response. Status code: "+code);
            summaryLine="Received Response. Status code: "+code;
            stateAfterRun=State.SUCCESS;
            outputs.get(0).setData(code);

            if(response.body()!=null)
                outputs.get(1).setData(response.body().string());
        }
        catch (IOException e) {
           stateAfterRun=State.FAILURE;
           summaryLine="Failed to reach the given destination";
           addLineToLog("Failed to reach the given destination");
        }
    }

    private Request buildRequest(String finalUrl,String method,String body)
    {
        Request request;
        RequestBody requestBody=null;

        if(body!=null){
            requestBody=RequestBody.create(MediaType.parse("application/json"), body);
        }

        request =new Request.Builder()
                .url(finalUrl)
                .method(method,requestBody)
                .build();

        return request;
    }


}
