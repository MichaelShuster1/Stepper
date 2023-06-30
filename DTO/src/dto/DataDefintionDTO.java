package dto;

import java.io.Serializable;
import java.util.Objects;

public class DataDefintionDTO implements Serializable
{
    private final String name;
    private final String defaultName;
    private final String type;

    public DataDefintionDTO(String name,String type)
    {
        this.name=name;
        this.type=type.substring(4);
        this.defaultName = null;
    }

    public DataDefintionDTO(String name, String defaultName, String type) {
        this.name = name;
        this.type=type.substring(4);
        this.defaultName = defaultName;
    }

    public DataDefintionDTO(DataDefintionDTO other)
    {
        this.name=other.name;
        this.type=other.type;
        this.defaultName = other.defaultName;
    }

    public String getDefaultName() {
        return defaultName;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataDefintionDTO that = (DataDefintionDTO) o;
        return Objects.equals(name, that.name) && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }
}
