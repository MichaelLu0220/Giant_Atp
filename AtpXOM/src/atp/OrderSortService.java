package atp;

import java.text.ParseException;
import java.util.*;

public class OrderSortService {

    private List<SortRule> sortRules = new ArrayList<>();

    public OrderSortService() throws ParseException {}{
    	
    }

    public void addSortRule(int sortOrder, FieldNameDomain fieldName, SortDirection direction) {
        SortRule rule = new SortRule(sortOrder, fieldName, direction);
        sortRules.add(rule);
    }

    public void clearSortRules() {
        sortRules.clear();
    }

    public void sortOrders(AtpUpdateService atpUpdate) {
        if (atpUpdate == null || atpUpdate.getArrivedOrders() == null) {
            return;
        }

        List<Order> orders = atpUpdate.getArrivedOrders();
        if (orders.isEmpty()) return;

        // 依 sortOrder 排序
        Collections.sort(sortRules, Comparator.comparingInt(SortRule::getSortOrder));

        // 依規則對訂單排序
        Collections.sort(orders, (o1, o2) -> {
            for (SortRule rule : sortRules) {
                int result = compareByRule(o1, o2, rule);
                if (result != 0) return result;
            }
            return 0;
        });

        System.out.println("✅ 訂單排序完成，使用規則：");
        sortRules.forEach(rule ->
                System.out.println("  " + rule.getSortOrder() + ". " +
                        rule.getFieldName().getDisplayName() + " " + rule.getDirection().getDisplayName())
        );
    }

    private int compareByRule(Order o1, Order o2, SortRule rule) {
        int result = 0;

        switch (rule.getFieldName()) {
            case PRIORITY:
                result = Integer.compare(o1.getcustPriority(), o2.getcustPriority());
                break;
            case REQDATE:
                result = compareDates(o1.getRequestDate(), o2.getRequestDate());
                break;
            case CREATEDATE:
                result = compareDates(o1.getCreateDate(), o2.getCreateDate());
                break;
            case QTY:
                result = Integer.compare(getTotalQty(o1), getTotalQty(o2));
                break;
            case CUSTACCOUNT:
                result = compareStrings(o1.getCustAccount(), o2.getCustAccount());
                break;
            case ORDERNO:
                result = compareStrings(o1.getOrderNo(), o2.getOrderNo());
                break;
        }

        if (rule.getDirection() == SortDirection.DESC) {
            result = -result;
        }

        return result;
    }

    private int compareDates(Date d1, Date d2) {
        if (d1 == null && d2 == null) return 0;
        if (d1 == null) return 1;
        if (d2 == null) return -1;
        return d1.compareTo(d2);
    }

    private int compareStrings(String s1, String s2) {
        if (s1 == null && s2 == null) return 0;
        if (s1 == null) return 1;
        if (s2 == null) return -1;
        return s1.compareTo(s2);
    }

    private int getTotalQty(Order order) {
        int total = 0;
        if (order.getOrderLines() != null) {
            for (OrderLine line : order.getOrderLines()) {
                total += line.getOrderQty();
            }
        }
        return total;
    }

    public List<SortRule> getSortRules() {
        return sortRules;
    }

    public void setSortRules(List<SortRule> sortRules) {
        this.sortRules = sortRules;
    }
}
