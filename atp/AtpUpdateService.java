package atp;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
public class AtpUpdateService {
	private List<Order> arrivedOrders;
	
	public AtpUpdateService() {
	}

	public List<Order> getArrivedOrders() {
		return arrivedOrders;
	}

	public void setArrivedOrders(List<Order> arrivedOrders) {
		this.arrivedOrders = arrivedOrders;
	}
	@JsonIgnore
	public int getOrderNumber() {
		return arrivedOrders.size();
	}
	@JsonIgnore
	public int getOrderLineNumber() {
		int num = 0;
		for (Order order : arrivedOrders) {
			num += order.getOrderLines().size();
		}
		return num;
	}
	@JsonIgnore
	public int getFulfilledOrderNumber() {
		int num = 0;
		for (Order order : arrivedOrders) {
			if (order.isFulfilled())
				num ++;
		}
		return num;
	}
	@JsonIgnore
	public int getFulfilledOrderLineNumber() {
		int num = 0;
		for (Order order : arrivedOrders) {
			for (OrderLine line : order.getOrderLines())
				if (line.isFulfilled())
					num ++;
		}
		return num;
	}
}
