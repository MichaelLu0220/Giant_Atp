package atp;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Date;

public class AtpAlloc implements Serializable {
	private Date orderDate;
	private Date supplyDate;
	private String supplyType;
	private int allocQty;
	private String itemId;
	private String warehouse;
	private String orderNo;

	private static final long serialVersionUID = 1L;
	
	public AtpAlloc() throws ParseException {
	}

	public AtpAlloc(Date orderDate, Date supplyDate, String supplyType, int allocQty, String itemId, String warehouse, String orderNo)
			throws ParseException {
		this.orderDate = orderDate;
		this.supplyDate = supplyDate;
		this.supplyType = supplyType;
		this.allocQty = allocQty;
		this.itemId = itemId;
		this.warehouse = warehouse;
		this.orderNo = orderNo;
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

	public String getItemId() {
		return itemId;
	}

	public void setItemId(String itemId) {
		this.itemId = itemId;
	}

	public String getWarehouse() {
		return warehouse;
	}

	public void setWarehouse(String warehouse) {
		this.warehouse = warehouse;
	}
	
	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}
	
}
