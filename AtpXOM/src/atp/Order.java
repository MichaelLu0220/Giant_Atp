package atp;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

public class Order implements Serializable {
	private static final long serialVersionUID = 1L;

	private String orderNo;
	private String custAccount;
	private Date requestDate;
	private Date createDate;
	private int custPriority;
	private boolean fulfilled;
	

	private Date promiseDate;  //訂單承諾日期

	private List<OrderLine> orderLines;

	public Order() throws ParseException {
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
	
	public Date getPromiseDate() {
		return promiseDate;
	}

	public void setPromiseDate(Date promiseDate) {
		this.promiseDate = promiseDate;
	}
	
	/**
     * 計算訂單的承諾日期
     * 承諾日期 = 所有訂單行中最晚的承諾日期
     */
	public void calculatePromise() {
	    promiseDate = null;
	    
	    if (orderLines == null || orderLines.isEmpty()) {
	        return;
	    }
	    
	    // 直接從所有訂單行的分配記錄中找最晚的供應日期
	    for (OrderLine line : orderLines) {
	        for (AtpAlloc alloc : line.getAtpAllocs()) {
	            if (promiseDate == null || alloc.getSupplyDate().after(promiseDate)) {
	                promiseDate = alloc.getSupplyDate();
	            }
	        }
	    }
	    
	    // 如果最晚供應日期不晚於需求日期，使用需求日期
	    if (promiseDate != null && !promiseDate.after(requestDate)) {
	        promiseDate = requestDate;
	    }
	}

	public List<OrderLine> getOrderLines() {
		return orderLines;
	}

	public void setOrderLines(List<OrderLine> orderLines) {
		this.orderLines = orderLines;
	}

	public boolean isFulfilled() {
		for (OrderLine line : orderLines) {
			if (!line.isFulfilled())
				return false;
		}
		return true;
	}

	public void setFulfilled(boolean fulfilled) {
		this.fulfilled = fulfilled;
	}
}