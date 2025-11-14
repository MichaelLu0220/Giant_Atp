package atp;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShipmentRequirementService implements Serializable{
	private static final long serialVersionUID = 1L;
    private static final int DEFAULT_LEAD_TIME_DAYS = 90;
    
    public ShipmentRequirementService() throws ParseException {
    }
    
    /**
     * 計算所有未滿足訂單的補貨需求
     */
    public void calculateShipmentRequirements(AtpUpdateService atpUpdate) {
        
        if (atpUpdate.getArrivedOrders() == null || atpUpdate.getArrivedOrders().isEmpty()) {
            return;
        }
        
        // 用 Map 來分組未滿足的訂單
        // key = warehouse_itemId
        // value = List<Object[]> {orderNo, unmetQty, orderDate}
        Map<String, List<Object[]>> groupMap = new HashMap<>();
        
        for (Order order : atpUpdate.getArrivedOrders()) {
            if (order.getOrderLines() == null) {
                continue;
            }
            
            for (OrderLine line : order.getOrderLines()) {
                // 只處理未完全滿足的訂單行
                if (!line.isFulfilled() && line.getUnmetQty() > 0) {
                    
                    String warehouse = line.getAssignedWarehouse() != null 
                        ? line.getAssignedWarehouse() 
                        : line.getWareHouse();
                    
                    String itemId = line.getAssignedItem() != null
                        ? extractFirstItem(line.getAssignedItem())
                        : line.getItemId();
                    
                    String key = warehouse + "_" + itemId;
                    
                    // 用 Object[] 儲存資料：[warehouse, itemId, orderNo, unmetQty, orderDate]
                    Object[] data = new Object[5];
                    data[0] = warehouse;
                    data[1] = itemId;
                    data[2] = order.getOrderNo();
                    data[3] = line.getUnmetQty();
                    data[4] = line.getOrderDate();
                    
                    // 加入分組
                    List<Object[]> list = groupMap.get(key);
                    if (list == null) {
                        list = new ArrayList<>();
                        groupMap.put(key, list);
                    }
                    list.add(data);
                }
            }
        }
        
        // 為每個群組建立補貨需求
        Date today = DateUtil.now();
        
        for (Map.Entry<String, List<Object[]>> entry : groupMap.entrySet()) {
            List<Object[]> dataList = entry.getValue();
            
            if (dataList == null || dataList.isEmpty()) {
                continue;
            }
            
            // 計算總數量和最早日期
            int totalQty = 0;
            Date earliestDate = null;
            String warehouse = null;
            String itemId = null;
            
            List<ShipmentRequirement.OrderDetail> details = new ArrayList<>();
            
            for (Object[] data : dataList) {
                warehouse = (String) data[0];
                itemId = (String) data[1];
                String orderNo = (String) data[2];
                int unmetQty = (Integer) data[3];
                Date orderDate = (Date) data[4];
                
                totalQty += unmetQty;
                
                if (earliestDate == null || orderDate.before(earliestDate)) {
                    earliestDate = orderDate;
                }
                
                ShipmentRequirement.OrderDetail detail = new ShipmentRequirement.OrderDetail();
                detail.setOrderNo(orderNo);
                detail.setUnmetQty(unmetQty);
                details.add(detail);
            }
            
            // 建立補貨需求
            ShipmentRequirement requirement = new ShipmentRequirement();
            requirement.setWarehouse(warehouse);
            requirement.setItemId(itemId);
            requirement.setRequiredQty(totalQty);
            requirement.setRequiredDate(earliestDate);
            
            // 計算理論上的下單日期（客戶要求日期往前推 90 天）
            Date idealOrderByDate = DateUtil.addDays(earliestDate, -DEFAULT_LEAD_TIME_DAYS);
            
            // 判斷是否還來得及
            boolean canFulfill = !today.after(idealOrderByDate);
            requirement.setCanFulfill(canFulfill);
            
            if (canFulfill) {
                // ========== 情況 A：還來得及 ==========
                // 使用理論下單日期，可以準時交貨
                requirement.setOrderByDate(idealOrderByDate);
                requirement.setEstimatedDeliveryDate(earliestDate);  // 準時交貨
                requirement.setDaysOverdue(0);
                
            } else {
                // ========== 情況 B：來不及了 ==========
                // 今天立即下單，計算實際可交貨日期
                requirement.setOrderByDate(today);  // 今天立即下單
                
                // 計算實際可交貨日期（今天 + 90 天前置時間）
                Date actualDeliveryDate = DateUtil.addDays(today, DEFAULT_LEAD_TIME_DAYS);
                requirement.setEstimatedDeliveryDate(actualDeliveryDate);
                
                // 計算會延遲多少天
                int delayDays = DateUtil.getDuration(earliestDate, actualDeliveryDate);
                requirement.setDaysOverdue(delayDays);
            }
            
            // 加入訂單明細
            requirement.setOrderDetails(details);
            
            atpUpdate.addShipmentRequirement(requirement);
        }
    }
    
    /**
     * 從組合商品字串中提取第一個商品
     */
    private String extractFirstItem(String assignedItem) {
        if (assignedItem == null || assignedItem.isEmpty()) {
            return null;
        }
        
        int plusIndex = assignedItem.indexOf('+');
        if (plusIndex > 0) {
            return assignedItem.substring(0, plusIndex);
        }
        
        return assignedItem;
    }
}