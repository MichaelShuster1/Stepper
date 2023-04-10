package Steps;

import DataDefinitions.DataString;
import DataDefinitions.Input;
import DataDefinitions.Output;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileDumper extends Step
{
    public FileDumper(String name,boolean continue_if_failing)
    {
        super(name, true,continue_if_failing);
        defaultName = "File Dumper";

        inputs.add(new Input(new DataString("CONTENT"),true,true,"Content:"));
        nameToInputIndex.put("CONTENT",0);

        inputs.add(new Input(new DataString("FILE_NAME"),true,true,"Target file path:"));
        nameToInputIndex.put("FILE_NAME",1);

        outputs.add(new Output(new DataString("RESULT"),"File creation result:"));
        nameToOutputIndex.put("RESULT",0);
    }

    @Override
    public void Run() {
        String content = (String) inputs.get(0).getData();
        String fileName = (String) inputs.get(1).getData();
        File file = new File(fileName);
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        formatter.format(new Date());
        String res;

        addLineToLog("About to create file named " + file.getName()
                + " [time: " + formatter.format(new Date()) + "]");


        try
        {
            if(!file.createNewFile())
                throw new Exception("The file with the provided name already exists");


            if(content != null && content.length() > 0)
            {
                try(Writer writer = new BufferedWriter(
                        new OutputStreamWriter(
                                new FileOutputStream(file), "UTF-8")))
                {
                    writer.write(content);
                }
                setState_after_run(State.SUCCESS);
                summaryLine = "Step ended successfully, file was created with the provided content";
            }
            else
            {
                setState_after_run(State.WARNING);
                summaryLine = "Warning: The content provided was empty, file was created successfully with no content (empty file)";
                addLineToLog("Created a file named " + file.getName() + ", tried to write content but the content was empty"
                        + " [time: " + formatter.format(new Date()) + "]");

            }
            res = "SUCCESS";
        }
        catch (Exception e)
        {
            setState_after_run(State.FAILURE);
            summaryLine = "Step failed. " + e.getMessage();
            addLineToLog("Failed to create a file named " + file.getName() + " in the provided location"
                    + " [time: " + formatter.format(new Date()) + "]");
            res = "Failed: " + e.getMessage();
        }
        outputs.get(0).setData(res);

    }
}