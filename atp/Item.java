package atp;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class Item {
	private String wareHouse;
	private String itemId;
	List<ItemSupply> supplies;
	
	public Item() {
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
	
	@JsonIgnore
	public String getItemKey() {
		return wareHouse + itemId;
	}
	
	public ItemSupply findItemSupply(Date supplyDate, String type) {
		for (ItemSupply supply : supplies) {
			if (supply.getSupplyDate().equals(supplyDate) && supply.getType().equals(type))
				return supply;
		}
		return null;
	}
	
	/**
	 * 按優先順序排序供應：Stock > Scheduled Receipt > New Shipment
	 * 相同類型按日期排序（越早越優先）
	 */
	public void sortSuppliesByPriority() {
		if (supplies == null) return;
		
		supplies.sort((s1, s2) -> {
			// 1. 先比較類型優先級
			int type1 = getTypePriority(s1.getType());
			int type2 = getTypePriority(s2.getType());
			if (type1 != type2) {
				return Integer.compare(type1, type2);
			}
			
			// 2. 同類型再比較日期（越早越優先）
			return s1.getSupplyDate().compareTo(s2.getSupplyDate());
		});
	}
	
	/**
	 * 取得供應類型的優先順序
	 * @param type 供應類型
	 * @return 優先級數字（越小越優先）
	 */
	private int getTypePriority(String type) {
		if (type == null) return 999;
		
		switch(type.toLowerCase()) {
			case "on-hand":
			case "stock":
				return 1;  // 最優先：現有庫存
			case "in-transit":
			case "scheduled-receipt":
			case "scheduled receipt":
				return 2;  // 次優先：在途貨物
			case "new-shipment":
			case "new shipment":
				return 3;  // 最後：新出貨
			default:
				return 999;  // 未知類型放最後
		}
	}
}