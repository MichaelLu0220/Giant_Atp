package atp;

import java.io.Serializable;
import java.text.ParseException;
import java.util.List;

public class Item implements Serializable {
	private static final long serialVersionUID = 1L;

	private String wareHouse;
	private String itemId;
	private String itemKey;
	
	List<ItemSupply> supplies;

	public Item() throws ParseException {
	}

	public String getWareHouse() {
		return wareHouse;
	}

	public void setWareHouse(String wareHouse) {
		this.wareHouse = wareHouse;
	}

	public String getItemId() {
		return itemId;
	}

	public void setItemId(String itemId) {
		this.itemId = itemId;
	}

	public List<ItemSupply> getSupplies() {
		return supplies;
	}

	public void setSupplies(List<ItemSupply> supplies) {
		this.supplies = supplies;
	}
	
	public String getItemKey() {
		return wareHouse + itemId;
	}
	
	public void setItemKey(String itemKey) {
		this.itemKey = itemKey;
	}

}