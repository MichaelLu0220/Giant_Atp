package atp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemSupply {
	private String type;  // 'on-hand' or 'in-transit'
	private Date supplyDate;
	private int supplyQty;
	private int allocQty;
	
	private List<AtpAlloc> atpAllocs = new ArrayList<>();
	
	public ItemSupply() {
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Date getSupplyDate() {
		return supplyDate;
	}
	public void setSupplyDate(Date supplyDate) {
		this.supplyDate = supplyDate;
	}
	public int getSupplyQty() {
		return supplyQty;
	}
	public void setSupplyQty(int supplyQty) {
		this.supplyQty = supplyQty;
	}
	public List<AtpAlloc> getAtpAllocs() {
		return atpAllocs;
	}
	public void setAtpAllocs(List<AtpAlloc> atpAllocs) {
		this.atpAllocs = atpAllocs;
	}
	
	public void addAtpAlloc(AtpAlloc atpAlloc) {
		atpAllocs.add(atpAlloc);
		allocQty += atpAlloc.getAllocQty();
	}
	public void removeAtpAlloc(AtpAlloc atpAlloc) {
		if (atpAllocs.remove(atpAlloc))
			allocQty -= atpAlloc.getAllocQty();
	}	
	public int getAllocQty() {
		return allocQty;
	}
	public boolean isExhausted() {
		return allocQty >= supplyQty;
	}
	public int getRemainQty() {
		return supplyQty - allocQty;
	}
}
