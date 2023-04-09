package Steps;

import DataDefinitions.DataRelation;
import DataDefinitions.DataString;
import DataDefinitions.Input;
import DataDefinitions.Output;

public class PropertiesExporter extends Step{
    public PropertiesExporter(String name,boolean continue_if_failing)
    {
        super(name, true,continue_if_failing);
        defaultName = "Properties Exporter";

        DataRelation input = new DataRelation("SOURCE");
        inputs.add(new Input(input,false,true));
        nameToInputIndex.put("SOURCE",0);

        outputs.add(new Output(new DataString("RESULT")));
        nameToOutputIndex.put("RESULT",0);
    }

    @Override
    public void Run() {

    }
}
