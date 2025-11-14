package atp;

/**
 * 排序欄位定義（純 Java 版本）
 * 若未來導入 Decision Center，可加上 @BusinessEnumeration 註解
 */
public enum FieldNameDomain {
    PRIORITY("客戶優先順序"),
    REQDATE("需求日期"),
    CREATEDATE("建立日期"),
    QTY("訂單總數量"),
    CUSTACCOUNT("客戶帳號"),
    ORDERNO("訂單編號");

    private final String displayName;

    FieldNameDomain(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
