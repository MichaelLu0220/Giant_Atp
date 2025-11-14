package atp;

/**
 * 排序方向定義（純 Java 版本）
 */
public enum SortDirection {
    ASC("升冪（由小到大）"),
    DESC("降冪（由大到小）");

    private final String displayName;

    SortDirection(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
