package utils;

import enginemanager.Statistics;
import enums.HCSteps;
import step.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class StepsUtils {

    public static Map<String, Statistics> getStatisticsMap() {
        Map<String, Statistics> res = new LinkedHashMap<>();
        for (HCSteps step : HCSteps.values()) {
            res.put(step.getStepName(), new Statistics());
        }
        return res;
    }

    public static Step CreateStep(String name,String finalName,boolean continueIfFailing)
    {
        Step newStep=null;
        switch (name) {
            case "Spend Some Time":
                newStep = new SpendSomeTime(finalName, continueIfFailing);
                break;
            case "Collect Files In Folder":
                newStep = new CollectFiles(finalName, continueIfFailing);
                break;
            case "Files Renamer":
                newStep = new FilesRenamer(finalName, continueIfFailing);
                break;
            case "Files Content Extractor":
                newStep = new FilesContentExtractor(finalName, continueIfFailing);
                break;
            case "CSV Exporter":
                newStep = new CSVExporter(finalName, continueIfFailing);
                break;
            case "Properties Exporter":
                newStep = new PropertiesExporter(finalName, continueIfFailing);
                break;
            case "File Dumper":
                newStep = new FileDumper(finalName, continueIfFailing);
                break;
            case "Files Deleter":
                newStep = new FilesDeleter(finalName, continueIfFailing);
                break;
            case "Zipper":
                newStep = new Zipper(finalName, continueIfFailing);
                break;
            case "Command Line":
                newStep = new CommandLine(finalName, continueIfFailing);
                break;
            case "To Json":
                newStep = new ToJson(finalName,continueIfFailing);
                break;
            case "Json Data Extractor":
                newStep = new JsonDataExtractor(finalName,continueIfFailing);
                break;
            case "HTTP Call":
                newStep =new HttpCall(finalName,continueIfFailing);
                break;

        }
        return newStep;
    }
}
