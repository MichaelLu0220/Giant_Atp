package atp;

import java.util.Date;

public class AtpAlloc {
	private Date orderDate;
	private Date supplyDate;
	private String supplyType;
	private int allocQty;
	
	public AtpAlloc() {
	}
	
	public AtpAlloc(Date orderDate, Date supplyDate, String supplyType, int allocQty) {
		this.orderDate = orderDate;
		this.supplyDate = supplyDate;
		this.supplyType = supplyType;
		this.allocQty = allocQty;
	}
	
	public Date getOrderDate() {
		return orderDate;
	}
	public void setOrderDate(Date orderDate) {
		this.orderDate = orderDate;
	}
	public Date getSupplyDate() {
		return supplyDate;
	}
	public void setSupplyDate(Date supplyDate) {
		this.supplyDate = supplyDate;
	}
	public int getAllocQty() {
		return allocQty;
	}
	public void setAllocQty(int allocQty) {
		this.allocQty = allocQty;
	}

	public String getSupplyType() {
		return supplyType;
	}

	public void setSupplyType(String supplyType) {
		this.supplyType = supplyType;
	}

}
