package atp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class OrderLine {
	private String wareHouse;           // 實際供應倉庫（會改變）
	private String requestedWarehouse;  // 原始請求倉庫（不變） ← 新增
	private String itemId;
	private Date orderDate;
	private int orderQty;
	private int allocQty;
	private Date promiseDate;
	private int promiseQty;
	private List<AtpAlloc> atpAllocs = new ArrayList<>();
	
	public OrderLine() {
	}
	
	public String getWareHouse() {
		return wareHouse;
	}
	public void setWareHouse(String wareHouse) {
		this.wareHouse = wareHouse;
	}
	
	// ← 新增 getter/setter
	public String getRequestedWarehouse() {
		return requestedWarehouse;
	}
	public void setRequestedWarehouse(String requestedWarehouse) {
		this.requestedWarehouse = requestedWarehouse;
	}
	
	public String getItemId() {
		return itemId;
	}
	public void setItemId(String itemId) {
		this.itemId = itemId;
	}
	public Date getOrderDate() {
		return orderDate;
	}
	public void setOrderDate(Date orderDate) {
		this.orderDate = orderDate;
	}
	public int getOrderQty() {
		return orderQty;
	}
	public void setOrderQty(int orderQty) {
		this.orderQty = orderQty;
	}
	public List<AtpAlloc> getAtpAllocs() {
		return atpAllocs;
	}
	public void setAtpAllocs(List<AtpAlloc> atpAllocs) {
		this.atpAllocs = atpAllocs;
	}
	
	@JsonIgnore
	public String getItemKey() {
		return wareHouse + itemId;
	}
	
	// ← 新增：判斷是否使用替代倉庫
	@JsonIgnore
	public boolean isFromAlternateWarehouse() {
		return requestedWarehouse != null 
			&& !wareHouse.equals(requestedWarehouse);
	}
	
	public void addAtpAlloc(AtpAlloc atpAlloc) {
		atpAllocs.add(atpAlloc);
		allocQty += atpAlloc.getAllocQty();
	}
	public void removeAtpAlloc(AtpAlloc atpAlloc) {
		if (atpAllocs.remove(atpAlloc))
			allocQty -= atpAlloc.getAllocQty();
	}
	public void removeAllAtps() {
		atpAllocs.clear();
		allocQty = 0;
	}
	public int getAllocQty() {
		return allocQty;
	}
	
	@JsonIgnore
	public boolean isFulfilled() {
		return allocQty >= orderQty;
	}
	
	@JsonIgnore
	public int getUnmetQty() {
		return orderQty - allocQty;
	}
	
	public void calculatePromise() {
	    promiseQty = allocQty;
	    promiseDate = null;
	    
	    for (AtpAlloc alloc : atpAllocs) {
	        if (promiseDate == null || alloc.getSupplyDate().after(promiseDate)) {
	            promiseDate = alloc.getSupplyDate();
	        }
	    }
	}

	public Date getPromiseDate() {
	    return promiseDate;
	}

	public int getPromiseQty() {
	    return promiseQty;
	}
}