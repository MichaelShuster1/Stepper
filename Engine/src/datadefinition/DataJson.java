package datadefinition;

import java.util.List;

public class DataJson extends DataDefinition<String>{

    private String data;

    public DataJson(String name) {super(name, "DataJson");}

    @Override
    public String getData() {
        return data;
    }

    @Override
    public void setData(String data) {
        this.data = data;
    }

    @Override
    public List<String> getSecondaryData() {
        return null;
    }

    @Override
    public String toString() {
        return data;
    }
}
