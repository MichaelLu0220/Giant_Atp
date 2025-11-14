package atp;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class ItemSubstitutionService {
    
    private AtpProfileService atpProfileService;
    
    public ItemSubstitutionService() throws ParseException {
    }
    
    public ItemSubstitutionService(AtpProfileService atpProfileService) {
        this.atpProfileService = atpProfileService;
    }
    
    /**
     * 為訂單進行倉庫和商品的綜合分配
     * 這是主入口方法，會被 ODM 規則呼叫
     */
    public boolean allocateWithSubstitution(Order order) {
        for (OrderLine orderLine : order.getOrderLines()) {
            boolean allocated = tryAllocateFromCandidateWarehouses(order, orderLine);
            if (!allocated) {
                System.out.println("❌ 訂單 " + order.getOrderNo() + " 的訂單行無法滿足");
                return false;
            }
        }
        return true;
    }
    
    /**
     * 嘗試從候選倉庫分配
     * 每個倉庫內可以混合使用替代商品
     */
    private boolean tryAllocateFromCandidateWarehouses(Order order, OrderLine orderLine) {
        List<String> candidateWarehouses = orderLine.doCandidateWarehouses();
        
        if (candidateWarehouses == null || candidateWarehouses.isEmpty()) {
            System.out.println("⚠️ 訂單行沒有候選倉庫");
            return false;
        }
        
        System.out.println("🔍 嘗試分配訂單，候選倉庫：" + candidateWarehouses);
        
        String originalWarehouse = orderLine.getWareHouse();
        String originalItem = orderLine.getItemId();
        
        // 依序嘗試每個候選倉庫
        for (String candidateWarehouse : candidateWarehouses) {
            System.out.println("  → 嘗試倉庫：" + candidateWarehouse);
            
            orderLine.setWareHouse(candidateWarehouse);
            
            // ✨ 關鍵：在單一倉庫內嘗試所有候選商品（可混合）
            boolean allocated = allocateFromCandidateItems(order, orderLine);
            
            if (orderLine.isFulfilled()) {
                System.out.println("  ✅ 倉庫 " + candidateWarehouse + " 分配成功！");
                orderLine.setAssignedWarehouse(candidateWarehouse);
                orderLine.setWareHouse(originalWarehouse);
                orderLine.setItemId(originalItem);
                return true;
            } else {
                System.out.println("  ❌ 倉庫 " + candidateWarehouse + " 無法完全滿足");
                // 回滾此倉庫的所有分配
                rollbackOrderLine(orderLine);
                orderLine.setWareHouse(originalWarehouse);
                orderLine.setItemId(originalItem);
            }
        }
        
        System.out.println("  ⚠️ 所有候選倉庫都無法滿足");
        return false;
    }
    
    /**
     * ✨ 核心方法：在單一倉庫內嘗試所有候選商品（可混合使用）
     * 這是與倉庫替代最大的差異：商品可以混合，倉庫不能混合
     */
    private boolean allocateFromCandidateItems(Order order, OrderLine orderLine) {
        
        // 如果不允許商品替代，只使用原商品
        if (!orderLine.isAllowSubstitution()) {
            System.out.println("    不允許商品替代，只使用原商品");
            allocateForOrderLine(order, orderLine);
            return orderLine.isFulfilled();
        }
        
        // 取得候選商品清單
        List<String> candidateItems = orderLine.doCandidateItems();
        
        if (candidateItems == null || candidateItems.isEmpty()) {
            System.out.println("    ⚠️ 沒有候選商品");
            return false;
        }
        
        System.out.println("    候選商品：" + candidateItems);
        
        String originalItem = orderLine.getItemId();
        List<String> usedItems = new ArrayList<>();  // 記錄使用了哪些商品
        
        // 依序嘗試每個候選商品，可以累加
        for (String candidateItem : candidateItems) {
            
            // 如果已經滿足，跳出迴圈
            if (orderLine.isFulfilled()) {
                break;
            }
            
            int beforeAllocQty = orderLine.getAllocQty();
            
            System.out.println("      → 嘗試商品：" + candidateItem + " (還需要 " + orderLine.getUnmetQty() + " 個)");
            
            orderLine.setItemId(candidateItem);
            
            // 嘗試分配（不完全滿足也沒關係，可以累加）
            allocateForOrderLine(order, orderLine);
            
            int afterAllocQty = orderLine.getAllocQty();
            int allocatedQty = afterAllocQty - beforeAllocQty;
            
            // 如果有分配到任何數量，記錄這個商品
            if (allocatedQty > 0) {
                usedItems.add(candidateItem);
                System.out.println("      ✓ 從 " + candidateItem + " 分配了 " + allocatedQty + " 個");
            } else {
                System.out.println("      ✗ " + candidateItem + " 無可用庫存");
            }
        }
        
        // 恢復原始商品ID
        orderLine.setItemId(originalItem);
        
        // 記錄實際使用的商品（可能是多個）
        if (!usedItems.isEmpty()) {
            if (usedItems.size() == 1) {
                // 只用了一種商品
                orderLine.setAssignedItem(usedItems.get(0));
            } else {
                // 使用了多個替代品，記錄為組合
                orderLine.setAssignedItem(String.join("+", usedItems));
            }
            System.out.println("      📦 使用商品: " + orderLine.getAssignedItem());
        }
        
        return orderLine.isFulfilled();
    }
    
    /**
     * 根據訂單優先級選擇分配策略
     */
    private void allocateForOrderLine(Order order, OrderLine orderLine) {
        int priority = order.getcustPriority();
        
        if (priority == 3) {
            // 高優先級：無限制
            System.out.println("        使用策略：高優先級（無限制）");
            atpProfileService.allocateAtpBackwardFromDueDay(order, orderLine);
            if (!orderLine.isFulfilled()) {
                atpProfileService.allocateAtpForwardUnlimited(order, orderLine);
            }
            
        } else if (priority == 2) {
            // 中優先級：90天限制
            System.out.println("        使用策略：中優先級（90天）");
            atpProfileService.allocateAtpBackwardWithEarlyDays(order, orderLine, 90);
            if (!orderLine.isFulfilled()) {
                atpProfileService.allocateAtpForwardWithLateDays(order, orderLine, 90);
            }
            
        } else if (priority == 1) {
            // 低優先級：60天限制
            System.out.println("        使用策略：低優先級（60天）");
            atpProfileService.allocateAtpBackwardWithEarlyDays(order, orderLine, 60);
            if (!orderLine.isFulfilled()) {
                atpProfileService.allocateAtpForwardWithLateDays(order, orderLine, 60);
            }
            
        } else {
            // 預設策略（未定義的優先級）
            System.out.println("        使用策略：預設");
            atpProfileService.allocateAtpBackwardFromDueDay(order, orderLine);
            if (!orderLine.isFulfilled()) {
                atpProfileService.allocateAtpForwardUnlimited(order, orderLine);
            }
        }
    }
    
    /**
     * 回滾訂單行的分配
     */
    private void rollbackOrderLine(OrderLine orderLine) {
        List<AtpAlloc> allocsToRemove = new ArrayList<>(orderLine.getAtpAllocs());
        
        for (AtpAlloc alloc : allocsToRemove) {
            String itemKey = alloc.getWarehouse() + alloc.getItemId();
            Item item = atpProfileService.findItem(itemKey);
            
            if (item != null) {
                for (ItemSupply supply : item.getSupplies()) {
                    supply.removeAtpAlloc(alloc);
                }
            }
        }
        
        orderLine.getAtpAllocs().clear();
        orderLine.setAllocQty(0);
    }
}