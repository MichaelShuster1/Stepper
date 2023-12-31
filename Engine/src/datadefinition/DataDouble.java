package datadefinition;


import java.util.List;

public class DataDouble extends DataDefinition<Double> {
    private Double data;

    public DataDouble(String name) {
        super(name, "DataDouble");
    }

    @Override
    public String toString() {
        return data.toString();
    }

    @Override
    public Double getData() {
        return data;
    }

    @Override
    public void setData(Double data) {
        this.data = data;
    }

    @Override
    public List<String> getSecondaryData() {
        return null;
    }
}