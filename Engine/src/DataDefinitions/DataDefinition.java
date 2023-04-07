package DataDefinitions;

public abstract class DataDefinition<T>
{
    protected String name;
    protected String type;

    public DataDefinition(String name, String type)
    {
        this.name = name;
        this.type = type;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public abstract void setData(T data);

    public abstract T getData();
}
