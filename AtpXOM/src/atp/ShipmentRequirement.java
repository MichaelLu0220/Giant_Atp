package atp;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
public class ShipmentRequirement implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String warehouse;
    private String itemId;
    private int requiredQty;
    private Date requiredDate;           // 最晚到貨日期（客戶要求）
    private Date orderByDate;            // 必須在此日期前下單
    private Date estimatedDeliveryDate;  // 預計實際交貨日期（新增）
    private boolean canFulfill;          // 是否還來得及
    private int daysOverdue;             // 如果來不及，超過幾天（或延遲幾天）
    
    private List<OrderDetail> orderDetails = new ArrayList<>();
    
    public ShipmentRequirement() {
    }
    
    // Getters and Setters
    
    public String getWarehouse() {
        return warehouse;
    }
    
    public void setWarehouse(String warehouse) {
        this.warehouse = warehouse;
    }
    
    public String getItemId() {
        return itemId;
    }
    
    public void setItemId(String itemId) {
        this.itemId = itemId;
    }
    
    public int getRequiredQty() {
        return requiredQty;
    }
    
    public void setRequiredQty(int requiredQty) {
        this.requiredQty = requiredQty;
    }
    
    public Date getRequiredDate() {
        return requiredDate;
    }
    
    public void setRequiredDate(Date requiredDate) {
        this.requiredDate = requiredDate;
    }
    
    public Date getOrderByDate() {
        return orderByDate;
    }
    
    public void setOrderByDate(Date orderByDate) {
        this.orderByDate = orderByDate;
    }
    
    public Date getEstimatedDeliveryDate() {
        return estimatedDeliveryDate;
    }
    
    public void setEstimatedDeliveryDate(Date estimatedDeliveryDate) {
        this.estimatedDeliveryDate = estimatedDeliveryDate;
    }
    
    public boolean isCanFulfill() {
        return canFulfill;
    }
    
    public void setCanFulfill(boolean canFulfill) {
        this.canFulfill = canFulfill;
    }
    
    public int getDaysOverdue() {
        return daysOverdue;
    }
    
    public void setDaysOverdue(int daysOverdue) {
        this.daysOverdue = daysOverdue;
    }
    
    public List<OrderDetail> getOrderDetails() {
        return orderDetails;
    }
    
    public void setOrderDetails(List<OrderDetail> orderDetails) {
        this.orderDetails = orderDetails;
    }
    
    public void addOrderDetail(String orderNo, int unmetQty) {
        OrderDetail detail = new OrderDetail();
        detail.setOrderNo(orderNo);
        detail.setUnmetQty(unmetQty);
        orderDetails.add(detail);
    }
    
    // 內部類別：訂單明細
    public static class OrderDetail implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private String orderNo;
        private int unmetQty;
        
        public String getOrderNo() {
            return orderNo;
        }
        
        public void setOrderNo(String orderNo) {
            this.orderNo = orderNo;
        }
        
        public int getUnmetQty() {
            return unmetQty;
        }
        
        public void setUnmetQty(int unmetQty) {
            this.unmetQty = unmetQty;
        }
    }
}