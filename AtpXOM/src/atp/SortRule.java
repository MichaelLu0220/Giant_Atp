package atp;

import java.io.Serializable;

/**
 * 排序規則（純 Java 版本）
 */
public class SortRule implements Serializable {
    private static final long serialVersionUID = 1L;

    private int sortOrder;
    private FieldNameDomain fieldName;
    private SortDirection direction;

    public SortRule() {}

    public SortRule(int sortOrder, FieldNameDomain fieldName, SortDirection direction) {
        this.sortOrder = sortOrder;
        this.fieldName = fieldName;
        this.direction = direction;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public FieldNameDomain getFieldName() {
        return fieldName;
    }

    public void setFieldName(FieldNameDomain fieldName) {
        this.fieldName = fieldName;
    }

    public SortDirection getDirection() {
        return direction;
    }

    public void setDirection(SortDirection direction) {
        this.direction = direction;
    }

    @Override
    public String toString() {
        return "SortRule{" +
                "order=" + sortOrder +
                ", field=" + fieldName +
                ", direction=" + direction +
                '}';
    }
}
