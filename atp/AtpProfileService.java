package atp;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.stream.Collectors;

public class AtpProfileService {
	private List<Item> items;
	private Map<String, Item> itemMap = new HashMap<>();
	
	private static final int MAX_DAYS = 10000;
	
	public AtpProfileService() {
	}
	
	public void printHello() {
		System.out.println("hello world!");
	}
	
	public void initialize() {
		for (Item item : this.items) {
			itemMap.put(item.getItemKey(), item);
			item.sortSuppliesByPriority();
		}
	}
	
	public Item findItem(String itemKey) {
		if (itemMap.size() > 0)
			return itemMap.get(itemKey);
		
		for (Item item : items)
			if (item.getItemKey().equals(itemKey))
				return item;
		
		return null;
	}
	
	public List<Item> getItems() {
		return items;
	}

	public void setItems(List<Item> items) {
		this.items = items;
	}
	
	private void allocateAtpForward(Order order, OrderLine orderLine, int maxEarlyDays, int maxLateDays) {
		Item item = findItem(orderLine.getItemKey());
		if (item == null || orderLine.isFulfilled())
			return;
		
		Date startDate = DateUtil.addDays(orderLine.getOrderDate(), -maxEarlyDays);
		Date endDate = DateUtil.addDays(orderLine.getOrderDate(), maxLateDays);
		
		ListIterator<ItemSupply> it = item.supplies.listIterator();
		while (it.hasNext()) {
			ItemSupply supply = it.next();
			if (supply.isExhausted())
				continue;
			
			if (supply.getSupplyDate().before(startDate) 
					|| supply.getSupplyDate().after(endDate))
				continue;

			int qty = Math.min(orderLine.getUnmetQty(), supply.getRemainQty());
			AtpAlloc alloc = new AtpAlloc(orderLine.getOrderDate(), supply.getSupplyDate(), supply.getType(), qty);
			orderLine.addAtpAlloc(alloc);
			supply.addAtpAlloc(alloc);
			if (orderLine.isFulfilled())
				break;
		}
	}
	
	private void allocateAtpBackward(Order order, OrderLine orderLine, int maxEarlyDays, int maxLateDays) {
		Item item = findItem(orderLine.getItemKey());
		if (item == null || orderLine.isFulfilled())
			return;
		
		Date startDate = DateUtil.addDays(orderLine.getOrderDate(), -maxEarlyDays);
		Date endDate = DateUtil.addDays(orderLine.getOrderDate(), maxLateDays);
		
		ListIterator<ItemSupply> it = item.supplies.listIterator(item.supplies.size());
		while (it.hasPrevious()) {
			ItemSupply supply = it.previous();
			if (supply.isExhausted())
				continue;
			
			if (supply.getSupplyDate().before(startDate) 
					|| supply.getSupplyDate().after(endDate))
				continue;

			int qty = Math.min(orderLine.getUnmetQty(), supply.getRemainQty());
			AtpAlloc alloc = new AtpAlloc(orderLine.getOrderDate(), supply.getSupplyDate(), supply.getType(), qty);
			orderLine.addAtpAlloc(alloc);
			supply.addAtpAlloc(alloc);
			if (orderLine.isFulfilled())
				break;
		}		
	}
	
	public void allocateAtpForwardTillDueDay(Order order, OrderLine orderLine) {
		allocateAtpForward(order, orderLine, MAX_DAYS,  0);
	}
	
	public void allocateAtpBackwardFromDueDay(Order order, OrderLine orderLine) {
		allocateAtpBackward(order, orderLine, MAX_DAYS,  0);
	}
	
	public void allocateAtpForwardUnlimited(Order order, OrderLine orderLine) {
		allocateAtpForward(order, orderLine, MAX_DAYS,  MAX_DAYS);
	}
	
	public void allocateAtpForwardWithLateDays(Order order, OrderLine orderLine, int maxLateDays) {
		allocateAtpForward(order, orderLine, 0,  maxLateDays);
	}	
	
	public void allocateAtpBackwardWithEarlyDays(Order order, OrderLine orderLine, int maxEarlyDays) {
		allocateAtpBackward(order, orderLine, maxEarlyDays,  0);
	}		
	
	public void releaseOrderAtp(Order order) {
		for (OrderLine ol : order.getOrderLines()) {
			Item item = findItem(ol.getItemKey());
			if (item == null)
				continue;
			
			for (AtpAlloc alloc : ol.getAtpAllocs()) {
				ItemSupply supply = item.findItemSupply(alloc.getSupplyDate(), alloc.getSupplyType());
				if (supply != null) {
					supply.removeAtpAlloc(alloc);
				}
			}
			ol.removeAllAtps();
		}
	}
	
	/**
	 * 取得替代倉庫清單（所有其他倉庫）
	 * @param selectedWarehouse 指定的倉庫
	 * @param itemId 品項ID
	 * @return 替代倉庫清單
	 */
	private List<String> getAlternateWarehouses(String selectedWarehouse, String itemId) {
		return items.stream()
			.filter(item -> item.getItemId().equals(itemId))  // 同品項
			.filter(item -> !item.getWareHouse().equals(selectedWarehouse))  // 不同倉庫
			.map(Item::getWareHouse)
			.distinct()
			.collect(Collectors.toList());
	}
	
	/**
	 * 使用替代倉庫進行分配（Forward，無限制）
	 * @param order 訂單
	 * @param orderLine 訂單行
	 */
	public void allocateAtpForwardUnlimitedWithAlternate(Order order, OrderLine orderLine) {
		// 記錄原始倉庫
		String originalWarehouse = orderLine.getWareHouse();
		
		if (orderLine.getRequestedWarehouse() == null) {
			orderLine.setRequestedWarehouse(originalWarehouse);
		}
		
		// 1. 先從指定倉庫分配
		allocateAtpForwardUnlimited(order, orderLine);
		
		// 2. 如果還沒滿足，嘗試替代倉庫
		if (!orderLine.isFulfilled()) {
			List<String> alternateWarehouses = getAlternateWarehouses(originalWarehouse, orderLine.getItemId());
			
			for (String altWarehouse : alternateWarehouses) {
				// 暫時切換到替代倉庫
				orderLine.setWareHouse(altWarehouse);
				
				// 嘗試分配
				allocateAtpForwardUnlimited(order, orderLine);
				
				// 如果已滿足，停止尋找
				if (orderLine.isFulfilled()) {
					break;
				}
			}
		}
		
		// 注意：不恢復原始倉庫，因為實際供應來源可能已改變
		// 如果需要記錄原始倉庫，應該在 OrderLine 加入 requestedWarehouse 欄位
	}
	
	/**
	 * 使用替代倉庫進行分配（Forward，指定延遲天數）
	 * @param order 訂單
	 * @param orderLine 訂單行
	 * @param maxLateDays 最大延遲天數
	 */
	public void allocateAtpForwardWithLateDaysAndAlternate(Order order, OrderLine orderLine, int maxLateDays) {
		String originalWarehouse = orderLine.getWareHouse();
		
		if (orderLine.getRequestedWarehouse() == null) {
			orderLine.setRequestedWarehouse(originalWarehouse);
		}
		
		// 1. 先從指定倉庫分配
		allocateAtpForwardWithLateDays(order, orderLine, maxLateDays);
		
		// 2. 如果還沒滿足，嘗試替代倉庫
		if (!orderLine.isFulfilled()) {
			List<String> alternateWarehouses = getAlternateWarehouses(originalWarehouse, orderLine.getItemId());
			
			for (String altWarehouse : alternateWarehouses) {
				orderLine.setWareHouse(altWarehouse);
				allocateAtpForwardWithLateDays(order, orderLine, maxLateDays);
				
				if (orderLine.isFulfilled()) {
					break;
				}
			}
		}
	}
	
	/**
	 * 使用替代倉庫進行分配（Backward + Forward）
	 * @param order 訂單
	 * @param orderLine 訂單行
	 * @param maxEarlyDays 最大提前天數
	 * @param maxLateDays 最大延遲天數
	 */
	public void allocateAtpBackwardForwardWithAlternate(Order order, OrderLine orderLine, 
	                                                     int maxEarlyDays, int maxLateDays) {
		String originalWarehouse = orderLine.getWareHouse();
		
		if (orderLine.getRequestedWarehouse() == null) {
			orderLine.setRequestedWarehouse(originalWarehouse);
		}
		
		// 1. 先從指定倉庫分配（Backward + Forward）
		allocateAtpBackward(order, orderLine, maxEarlyDays, 0);
		allocateAtpForward(order, orderLine, 0, maxLateDays);
		
		// 2. 如果還沒滿足，嘗試替代倉庫
		if (!orderLine.isFulfilled()) {
			List<String> alternateWarehouses = getAlternateWarehouses(originalWarehouse, orderLine.getItemId());
			
			for (String altWarehouse : alternateWarehouses) {
				orderLine.setWareHouse(altWarehouse);
				allocateAtpBackward(order, orderLine, maxEarlyDays, 0);
				allocateAtpForward(order, orderLine, 0, maxLateDays);
				
				if (orderLine.isFulfilled()) {
					break;
				}
			}
		}
	}
	/**
	 * 使用替代倉庫進行分配（Backward Unlimited + Forward Unlimited）
	 */
	public void allocateAtpBackwardForwardUnlimitedWithAlternate(Order order, OrderLine orderLine) {
	    String originalWarehouse = orderLine.getWareHouse();
	    
	    // 記錄原始倉庫
	    if (orderLine.getRequestedWarehouse() == null) {
	        orderLine.setRequestedWarehouse(originalWarehouse);
	    }
	    
	    // 1. 先從指定倉庫分配（Backward + Forward Unlimited）
	    allocateAtpBackwardFromDueDay(order, orderLine);
	    allocateAtpForwardUnlimited(order, orderLine);
	    
	    // 2. 如果還沒滿足，嘗試替代倉庫
	    if (!orderLine.isFulfilled()) {
	        List<String> alternateWarehouses = getAlternateWarehouses(originalWarehouse, orderLine.getItemId());
	        
	        for (String altWarehouse : alternateWarehouses) {
	            orderLine.setWareHouse(altWarehouse);
	            allocateAtpBackwardFromDueDay(order, orderLine);
	            allocateAtpForwardUnlimited(order, orderLine);
	            
	            if (orderLine.isFulfilled()) {
	                break;
	            }
	        }
	    }
	}

	/**
	 * 為未滿足的訂單建立新出貨計畫
	 * @param order 訂單
	 * @param orderLine 訂單行
	 * @param leadTimeDays 前置時間（天數）
	 */
	public void createNewShipmentIfNeeded(Order order, OrderLine orderLine, int leadTimeDays) {
	    // 如果已經滿足，不需要建立
	    if (orderLine.isFulfilled()) {
	        return;
	    }
	    
	    // 找到品項
	    Item item = findItem(orderLine.getItemKey());
	    if (item == null) {
	        return;
	    }
	    
	    // 計算需要的數量和出貨日期
	    int unmetQty = orderLine.getUnmetQty();
	    Date shipmentDate = DateUtil.addDays(orderLine.getOrderDate(), leadTimeDays);
	    
	    // 建立新的 ItemSupply
	    ItemSupply newSupply = new ItemSupply();
	    newSupply.setSupplyDate(shipmentDate);
	    newSupply.setType("new-shipment");
	    newSupply.setSupplyQty(unmetQty);
	    
	    // 加入到品項的供應清單
	    item.getSupplies().add(newSupply);
	    
	    // 重新排序供應（確保優先順序正確）
	    item.sortSuppliesByPriority();
	    
	    // 立即分配給這個訂單
	    AtpAlloc alloc = new AtpAlloc(
	        orderLine.getOrderDate(), 
	        shipmentDate, 
	        "new-shipment", 
	        unmetQty
	    );
	    orderLine.addAtpAlloc(alloc);
	    newSupply.addAtpAlloc(alloc);
	}
	
	/**
	 * 使用替代品和替代倉庫進行分配
	 * 邏輯：
	 * 1. 先嘗試主品項（所有倉庫）
	 * 2. 如果不夠且有替代品 → 嘗試替代品（所有倉庫）
	 * 3. 如果還是不夠 → 結束（等 New Shipment）
	 * 
	 * @param order 訂單
	 * @param orderLine 訂單行
	 */
	public void allocateAtpSubstitutes(
	        Order order, OrderLine orderLine, int maxEarlyDays, int maxLateDays) {
	    
	    String originalWarehouse = orderLine.getWareHouse();
	    String originalItemId = orderLine.getItemId();
	    
	    if (orderLine.getRequestedWarehouse() == null) {
	        orderLine.setRequestedWarehouse(originalWarehouse);
	    }
	    if (orderLine.getOriginalItemId() == null) {
	        orderLine.setOriginalItemId(originalItemId);
	    }
	    
	    // Step 1: 嘗試主品項
//	    allocateAtpBackwardForwardWithAlternate(order, orderLine, maxEarlyDays, maxLateDays);
//	    
//	    if (orderLine.isFulfilled()) {
//	        return;
//	    }
	    int priority = order.getcustPriority();
	    
	    // Step 2: 查詢替代品
	    Item primaryItem = findItem(originalWarehouse + originalItemId);
	    
	    if (primaryItem == null || !primaryItem.hasSubstitute()) {
	        return;
	    }
	    
	    // Step 3: 嘗試替代品	    
	    List<String> substituteItemIds = primaryItem.getSubstituteItemIds();
	    for (String substituteItemId : substituteItemIds) {
	        orderLine.setWareHouse(originalWarehouse);
	        orderLine.setItemId(substituteItemId);
	        if(priority==3) {
	        	allocateAtpBackwardForwardUnlimitedWithAlternate(order, orderLine);
	        }
	        else {
	        	allocateAtpBackwardForwardWithAlternate(order, orderLine, maxEarlyDays, maxLateDays);
	        }
	        if (orderLine.isFulfilled()) {
	            break;  // 已滿足，停止
	        }
	    }
	}
}
