package atp;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
public class Order {
	private String orderNo;
	private String custAccount;
	private Date requestDate;
	private Date createDate;
	private int custPriority;
	private List<OrderLine> orderLines;
	
	public Order() {
	}
	
	public String getOrderNo() {
		return orderNo;
	}
	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}
	public String getCustAccount() {
		return custAccount;
	}
	public void setCustAccount(String custAccount) {
		this.custAccount = custAccount;
	}
	public Date getRequestDate() {
		return requestDate;
	}
	public void setRequestDate(Date requestDate) {
		this.requestDate = requestDate;
	}
	public Date getCreateDate() {
	    return createDate;
	}
	public void setCreateDate(Date createDate) {
	    this.createDate = createDate;
	}
	public int getcustPriority() {
		return custPriority;
	}
	public void setcustPriority(int custPriority) {
		this.custPriority = custPriority;
	}
	public List<OrderLine> getOrderLines() {
		return orderLines;
	}
	public void setOrderLines(List<OrderLine> orderLines) {
		this.orderLines = orderLines;
	}
	@JsonIgnore
	public boolean isFulfilled() {
		for (OrderLine line : orderLines) {
			if (!line.isFulfilled())
				return false;
		}
		return true;
	}
}
