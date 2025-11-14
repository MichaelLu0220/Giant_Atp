package atp;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OrderLine implements Serializable {
	private static final long serialVersionUID = 1L;

	// 基本訂單資訊
	private String wareHouse;        // 倉庫代碼（會改變，用於嘗試不同倉庫）
	private String itemId;           // 商品代碼
	private Date orderDate;          // 訂單日期
	private int orderQty;            // 訂購數量（客戶要的數量）
	
	// 分配結果
	private int allocQty;            // 已分配數量（目前分到多少）
	private boolean fulfilled;       // 是否已完全滿足
	
	private String itemKey;
	private int unmetQty;
	
	// 倉庫替代相關（ODM 規則會設定）
	private String warehouseGroup;             // 群組代碼（例如："A"）
	private List<String> alternativeWarehouses; // 替代倉庫列表（例如：["WHX","WHY"]）
	private String assignedWarehouse;          // 最終使用的倉庫
    
	// ✨ 商品替代相關（新增）
	private boolean allowSubstitution;        // 是否允許使用替代品
	private String itemGroup;                 // 商品群組代碼（例如："A"）
	private List<String> alternativeItems;    // 替代商品清單（例如：["BikeA","BikeA1","BikeA2"]）
	private String assignedItem;              // 最終使用的商品（可能是單一或組合，例如："BikeA" 或 "BikeA+BikeA1"）
	
	// 分配明細
	private List<AtpAlloc> atpAllocs = new ArrayList<>();// 所有分配記錄

	public OrderLine() throws ParseException {
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

	public void setAllocQty(int allocQty) {
		this.allocQty = allocQty;
	}

	public boolean isFulfilled() {
		return allocQty >= orderQty;
	}

	public void setFulfilled(boolean fulfilled) {
		this.fulfilled = fulfilled;
	}

	public int getUnmetQty() {
		return orderQty - allocQty;
	}

	public void setUnmetQty(int unmetQty) {
		this.unmetQty = unmetQty;
	}

	public List<AtpAlloc> getAtpAllocs() {
		return atpAllocs;
	}

	public void setAtpAllocs(List<AtpAlloc> atpAllocs) {
		this.atpAllocs = atpAllocs;
	}

	public String getItemKey() {
		return wareHouse + itemId;
	}

	public void setItemKey(String itemKey) {
		this.itemKey = itemKey;
	}
	
	public String getWareHouse() {
		return wareHouse;
	}

	public void setWareHouse(String wareHouse) {
		this.wareHouse = wareHouse;
	}
	
	// ========== 倉庫替代相關 ==========
	
	public String getWarehouseGroup() {
        return warehouseGroup;
    }
    
    public void setWarehouseGroup(String warehouseGroup) {
        this.warehouseGroup = warehouseGroup;
    }
    
    public List<String> getAlternativeWarehouses() {
        return alternativeWarehouses;
    }
    
    public void setAlternativeWarehouses(List<String> alternativeWarehouses) {
        this.alternativeWarehouses = alternativeWarehouses;
    }
    
    public String getAssignedWarehouse() {
        return assignedWarehouse;
    }
    
    public void setAssignedWarehouse(String assignedWarehouse) {
        this.assignedWarehouse = assignedWarehouse;
    }
    
    /**
     * 取得所有候選倉庫（主倉庫 + 替代倉庫）
     * @return 候選倉庫清單
     */
    public List<String> doCandidateWarehouses() {
        List<String> candidates = new ArrayList<>();
        
        // 1. 主倉庫優先
        if (wareHouse != null) {
            candidates.add(wareHouse);
        }
        
        // 2. 加入替代倉庫
        if (alternativeWarehouses != null) {
            for (String alt : alternativeWarehouses) {
                // 避免重複加入主倉庫
                if (!alt.equals(wareHouse)) {
                    candidates.add(alt);
                }
            }
        }
        
        return candidates;
    }
    
    // ========== ✨ 商品替代相關（新增）==========
    
    /**
     * 是否允許使用替代品
     */
    public boolean isAllowSubstitution() {
        return allowSubstitution;
    }
    
    public void setAllowSubstitution(boolean allowSubstitution) {
        this.allowSubstitution = allowSubstitution;
    }
    
    /**
     * 商品群組代碼
     */
    public String getItemGroup() {
        return itemGroup;
    }
    
    public void setItemGroup(String itemGroup) {
        this.itemGroup = itemGroup;
    }
    
    /**
     * 替代商品清單
     */
    public List<String> getAlternativeItems() {
        return alternativeItems;
    }
    
    public void setAlternativeItems(List<String> alternativeItems) {
        this.alternativeItems = alternativeItems;
    }
    
    /**
     * 最終使用的商品
     * 可能是單一商品（例如："BikeA"）
     * 或組合商品（例如："BikeA+BikeA1+BikeA2"）
     */
    public String getAssignedItem() {
        return assignedItem;
    }
    
    public void setAssignedItem(String assignedItem) {
        this.assignedItem = assignedItem;
    }
    
    /**
     * ✨ 取得所有候選商品（主商品 + 替代商品）
     * 優先順序：主商品優先，然後是替代商品
     * @return 候選商品清單
     */
    public List<String> doCandidateItems() {
        List<String> candidates = new ArrayList<>();
        
        // 如果不允許替代，只返回主商品
        if (!allowSubstitution) {
            if (itemId != null) {
                candidates.add(itemId);
            }
            return candidates;
        }
        
        // 1. 主商品優先
        if (itemId != null) {
            candidates.add(itemId);
        }
        
        // 2. 加入替代商品
        if (alternativeItems != null) {
            for (String altItem : alternativeItems) {
                // 避免重複加入主商品
                if (!altItem.equals(itemId)) {
                    candidates.add(altItem);
                }
            }
        }
        
        return candidates;
    }
}