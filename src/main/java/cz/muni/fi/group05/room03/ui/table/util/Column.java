package cz.muni.fi.group05.room03.ui.table.util;

public class Column<T> {

    private final Class<T> type;
    private final String name;

    private Column(Class<T> type, String name) {
        this.type = type;
        this.name = name.toUpperCase();
    }

    public static <T> Column<T> of(Class<T> type, String name) {
        return new Column<>(type, name);
    }

    public String getName() {
        return name;
    }

    public Class<T> getType() {
        return type;
    }

    @SuppressWarnings("unchecked")
    public T valueOf(Object obj) {
        if (!type.isInstance(obj))
            throw new IllegalArgumentException("Column Error: Column got value with wrong type!");
        return (T) obj;
    }
}
