package atp;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class AtpProfileService implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private List<Item> items;
	private Map<String, Item> itemMap = new HashMap<>();
	private static final int MAX_DAYS = 10000;
	
	
	public AtpProfileService() throws ParseException {
	}
	
	public List<Item> getItems() {
		return items;
	}

	public void setItems(List<Item> items) {
		this.items = items;
	}
	
	/**
     * 初始化 ATP Profile
     * 建立品項索引並排序供應
     */
	public void initialize() {
	    if (items == null) {
	        items = new ArrayList<>();
	    }
	    
	    itemMap.clear();
	    for (Item item : items) {
	        itemMap.put(item.getItemKey(), item);
	        
	        // 按供應日期升序排序
	        if (item.getSupplies() != null && !item.getSupplies().isEmpty()) {
	            Collections.sort(item.getSupplies(), 
	                Comparator.comparing(ItemSupply::getSupplyDate)
	            );
	        }
	    }
	}
    
    /**
     * 查找品項
     * @param itemKey 品項鍵值 (warehouse + itemId)
     * @return 品項物件，若找不到則返回 null
     */
    public Item findItem(String itemKey) {
        if (itemMap.size() > 0) {
            return itemMap.get(itemKey);
        }

        // Fallback: 如果 itemMap 未初始化，直接搜尋
        if (items != null) {
            for (Item item : items) {
                if (item.getItemKey().equals(itemKey)) {
                    return item;
                }
            }
        }
        
        return null;
    }
    
 // ========== 基礎分配方法 ==========
    /**
     * Backward 分配（從訂購日期往前找供應）
     */
//    private void allocateAtpBackward(Order order, OrderLine orderLine, int maxEarlyDays, int maxLateDays) {
//        Item item = findItem(orderLine.getItemKey());
//        if (item == null || orderLine.isFulfilled()) {
//            return;
//        }
//
//        Date startDate = DateUtil.addDays(orderLine.getOrderDate(), -maxEarlyDays);
//        Date endDate = DateUtil.addDays(orderLine.getOrderDate(), maxLateDays);
//
//        ListIterator<ItemSupply> it = item.supplies.listIterator(item.supplies.size());
//        while (it.hasPrevious()) {
//            ItemSupply supply = it.previous();
//            
//            if (supply.isExhausted()) {
//                continue;
//            }
//
//            if (supply.getSupplyDate().after(orderLine.getOrderDate())) continue;
//            if (supply.getSupplyDate().before(startDate)) continue;
//
//            int qty = Math.min(orderLine.getUnmetQty(), supply.getRemainQty());
//            try {
//	            AtpAlloc alloc = new AtpAlloc(
//	                orderLine.getOrderDate(), 
//	                supply.getSupplyDate(), 
//	                supply.getType(), 
//	                qty,
//	                orderLine.getItemId(), 
//	                orderLine.getWareHouse()
//	            );
//	            
//	            orderLine.addAtpAlloc(alloc);
//	            supply.addAtpAlloc(alloc);
//	        } catch (ParseException e) {
//	            e.printStackTrace();
//	            // 或者 log 出錯訊息，避免整個流程中斷
//	        }
//            if (orderLine.isFulfilled()) {
//                break;
//            }
//        }
//    }
    
    private void allocateAtpBackward(Order order, OrderLine orderLine, int maxEarlyDays, int maxLateDays) {
        Item item = findItem(orderLine.getItemKey());
        if (item == null || orderLine.isFulfilled()) {
            return;
        }

        Date startDate = DateUtil.addDays(orderLine.getOrderDate(), -maxEarlyDays);
        Date endDate = DateUtil.addDays(orderLine.getOrderDate(), maxLateDays);

        // ✅ 從第一個開始，往後遍歷（舊→新）
        ListIterator<ItemSupply> it = item.supplies.listIterator();
        while (it.hasNext()) {
            ItemSupply supply = it.next();
            
            if (supply.isExhausted()) {
                continue;
            }

            if (supply.getSupplyDate().after(orderLine.getOrderDate())) continue;
            if (supply.getSupplyDate().before(startDate)) continue;

            int qty = Math.min(orderLine.getUnmetQty(), supply.getRemainQty());
            try {
                AtpAlloc alloc = new AtpAlloc(
                    orderLine.getOrderDate(), 
                    supply.getSupplyDate(), 
                    supply.getType(), 
                    qty,
                    orderLine.getItemId(), 
                    orderLine.getWareHouse(),
                    order.getOrderNo()
                );
                
                orderLine.addAtpAlloc(alloc);
                supply.addAtpAlloc(alloc);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (orderLine.isFulfilled()) {
                break;
            }
        }
    }

    /**
     * Forward 分配（從訂購日期往後找供應）
     */
    private void allocateAtpForward(Order order, OrderLine orderLine, int maxEarlyDays, int maxLateDays) {
        Item item = findItem(orderLine.getItemKey());
        if (item == null || orderLine.isFulfilled()) {
            return;
        }

        Date startDate = DateUtil.addDays(orderLine.getOrderDate(), -maxEarlyDays);
        Date endDate = DateUtil.addDays(orderLine.getOrderDate(), maxLateDays);

        ListIterator<ItemSupply> it = item.supplies.listIterator();
        while (it.hasNext()) {
            ItemSupply supply = it.next();
            
            if (supply.isExhausted()) {
                continue;
            }

            if (supply.getSupplyDate().before(orderLine.getOrderDate())) continue;
            if (supply.getSupplyDate().after(endDate)) continue;

            int qty = Math.min(orderLine.getUnmetQty(), supply.getRemainQty());
            try {
                AtpAlloc alloc = new AtpAlloc(
                    orderLine.getOrderDate(), 
                    supply.getSupplyDate(), 
                    supply.getType(), 
                    qty,
                    orderLine.getItemId(), 
                    orderLine.getWareHouse(),
                    order.getOrderNo()
                );

                orderLine.addAtpAlloc(alloc);
                supply.addAtpAlloc(alloc);

            } catch (ParseException e) {
                e.printStackTrace();
                // 或者 log 出錯訊息，避免整個流程中斷
            }
            
            if (orderLine.isFulfilled()) {
                break;
            }
        }
    }
    
    /**
     * Backward 分配從到期日 (High used)
     */
    public void allocateAtpBackwardFromDueDay(Order order, OrderLine orderLine) {
        allocateAtpBackward(order, orderLine, MAX_DAYS, 0);
    }
    
    /**
     * Forward 分配無限制 (High used)
     */
    public void allocateAtpForwardUnlimited(Order order, OrderLine orderLine) {
        allocateAtpForward(order, orderLine, MAX_DAYS, MAX_DAYS);
    }
    
    /**
     * Backward 分配指定提前天數 (Other used)
     */
    public void allocateAtpBackwardWithEarlyDays(Order order, OrderLine orderLine, int maxEarlyDays) {
        allocateAtpBackward(order, orderLine, maxEarlyDays, 0);
    }
    
    /**
     * Forward 分配指定延遲天數 (Other used)
     */
    public void allocateAtpForwardWithLateDays(Order order, OrderLine orderLine, int maxLateDays) {
        allocateAtpForward(order, orderLine, 0, maxLateDays);
    }
}