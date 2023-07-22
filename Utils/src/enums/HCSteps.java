package enums;


import java.util.ArrayList;
import java.util.List;


public enum HCSteps {
    SPEND_SOME_TIME("Spend Some Time"),
    COLLECT_FILES("Collect Files In Folder"),
    FILES_RENAMER("Files Renamer"),
    FILES_CONTENT("Files Content Extractor"),
    CSV_EXPORTER("CSV Exporter"),
    PROPERTIES_EXPORTER("Properties Exporter"),
    FILE_DUMPER("File Dumper"),
    FILE_DELETER("Files Deleter"),
    ZIPPER("Zipper"),
    COMMAND_LINE("Command Line"),
    TO_JSON("To Json"),
    HTTP_CALL("HTTP Call"),
    JSON_DATA_EXTRACTOR("Json Data Extractor");

    private String stepName;

    HCSteps(String stepName) {
        this.stepName = stepName;
    }

    public String getStepName() {
        return stepName;
    }

    public static List<String> getAllStepsName() {
        List<String> names=new ArrayList<>();
        for(HCSteps step: HCSteps.values())
        {
            names.add(step.getStepName());
        }
        return names;
    }

}
