package atp;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ItemSupply implements Serializable {
	private static final long serialVersionUID = 1L;

	private String type;
	private Date supplyDate;
	private int supplyQty;
	private int allocQty;
	private List<AtpAlloc> atpAllocs = new ArrayList<>();
	private boolean exhausted;
	private int remainQty;

	public ItemSupply() throws ParseException {
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

	public int getAllocQty() {
		return allocQty;
	}

	public void addAtpAlloc(AtpAlloc atpAlloc) {
		atpAllocs.add(atpAlloc);
		allocQty += atpAlloc.getAllocQty();
	}

	public void removeAtpAlloc(AtpAlloc atpAlloc) {
		if (atpAllocs.remove(atpAlloc))
			allocQty -= atpAlloc.getAllocQty();
	}

	public List<AtpAlloc> getAtpAllocs() {
		return atpAllocs;
	}

	public void setAtpAllocs(List<AtpAlloc> atpAllocs) {
		this.atpAllocs = atpAllocs;
	}

	public boolean isExhausted() {
		return allocQty >= supplyQty;
	}

	public void setExhausted(boolean exhausted) {
		this.exhausted = exhausted;
	}

	public int getRemainQty() {
		return supplyQty - allocQty;
	}

	public void setRemainQty(int remainQty) {
		this.remainQty = remainQty;
	}
}