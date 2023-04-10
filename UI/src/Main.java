import Flow.Flow;
import Steps.*;
import javafx.util.Pair;

import java.util.*;

public class Main
{
    public static void main(String[] args)
    {
        Scanner inputStream=new Scanner(System.in);


        Flow flow=new Flow("Rename Files","Given a folder adds prefix and\\or" +
                " suffix to each file name. The renaming results are expressed via CSV and Properties files");


        flow.AddFormalOutput("PROP_RESULT");
        flow.AddFormalOutput("CSV_RESULT");

        /*
        flow.AddStep(new CollectFiles("Collect Files In Folder"));
        flow.AddStep(new SpendSomeTime("Spend Some Time"));
        flow.AddStep(new FilesDeleter("Files Deleter"));
        */


        flow.AddStep(new CollectFiles("Collect Files In Folder",false));
        flow.AddStep(new FilesRenamer("Files Renamer",false));
        flow.AddStep(new CSVExporter("CSV Exporter",false));
        flow.AddStep(new FileDumper("CSV File Dumper",true));
        flow.AddStep(new PropertiesExporter( "Properties Exporter",false));
        flow.AddStep(new FileDumper("Properties File Dumper",true));


        flow.getStep(1).ChangeOutputName("RENAME_RESULT","SOURCE");
        flow.getStep(2).ChangeOutputName("RESULT","CSV_RESULT");
        flow.getStep(3).ChangeInputName("FILE_NAME","CSV_FILE_NAME");
        flow.getStep(3).ChangeOutputName("RESULT","CSV_FILE_DUMP_RESULT");
        flow.getStep(4).ChangeOutputName("RESULT","PROP_RESULT");
        flow.getStep(5).ChangeInputName("FILE_NAME","PROP_FILE_NAME");
        flow.getStep(5).ChangeOutputName("RESULT","PROP_FILE_DUMP_RESULT");


        Map<Pair<String,String>,Pair<String,String>> customMappingInput = new HashMap<>();
        customMappingInput.put(new Pair<>("Collect Files In Folder","FILES_LIST"), new Pair<>("Files Renamer","FILES_TO_RENAME"));
        customMappingInput.put(new Pair<>("CSV Exporter","CSV_RESULT"), new Pair<>("CSV File Dumper","CONTENT"));
        customMappingInput.put(new Pair<>("Properties Exporter","PROP_RESULT"), new Pair<>("Properties File Dumper","CONTENT"));


        flow.CustomMapping(customMappingInput);
        flow.AutomaticMapping();
        flow.CalculateFreeInputs();
        flow.flowPrintData();


        while (true)
            getFlowInputsFromUser(flow);

    }
    public static void getFlowInputsFromUser(Flow flow)
    {
        Scanner inputStream=new Scanner(System.in);
        List<String> inputNames= flow.getInputList();
        int i=1,choice;
        String inputName,data;
        boolean flowReady=false,runFlow= false;

        while (!runFlow)
        {
            System.out.println("inputs to choose:");
            i=1;
            for (String inputName1 : inputNames)
            {
                System.out.println(i + "." +
                        inputName1.toLowerCase().replace("_", " "));
                i++;
            }

            if(flowReady)
                System.out.println("\n"+i+".run the flow");

            System.out.println("please choose a number: ");

            choice = inputStream.nextInt();
            if(choice<i)
            {
                inputName = inputNames.get(choice - 1).split(" ")[0];
                System.out.println("user string: ");
                inputStream.nextLine();
                data = inputStream.nextLine();

                flow.processInput(inputName, data);
                if(flow.checkIfFlowReady())
                    flowReady=true;
            }
            else if(choice==i)
            {
                runFlow=true;
                System.out.println(flow.executeFlow());
            }
            else
                System.out.println("wrong number entered");
        }
    }
}
