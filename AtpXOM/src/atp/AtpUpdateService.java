package atp;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class AtpUpdateService implements Serializable {
	private static final long serialVersionUID = 1L;

	public AtpUpdateService() throws ParseException {
		this.arrivedOrders = new ArrayList<>();
		this.shipmentRequirements = new ArrayList<>();
	}

	private List<Order> arrivedOrders = new ArrayList<>();
	private int fulfilledOrderLineNumber;
	private int fulfilledOrderNumber;
	private int orderLineNumber;
	private int orderNumber;
	private int orderNumberHigh;
	private int orderNumberMiddle;
	private List<ShipmentRequirement> shipmentRequirements = new ArrayList<>();

	public int getOrderNumberHigh() {
		return orderNumberHigh;
	}

	public void setOrderNumberHigh(int orderNumberHigh) {
		this.orderNumberHigh = orderNumberHigh;
	}

	public int getOrderNumberMiddle() {
		return orderNumberMiddle;
	}

	public void setOrderNumberMiddle(int orderNumberMiddle) {
		this.orderNumberMiddle = orderNumberMiddle;
	}

	public int getOrderNumberLow() {
		return orderNumberLow;
	}

	public void setOrderNumberLow(int orderNumberLow) {
		this.orderNumberLow = orderNumberLow;
	}

	private int orderNumberLow;

	public List<Order> getArrivedOrders() {
		return arrivedOrders;
	}

	public void setArrivedOrders(List<Order> arrivedOrders) {
		this.arrivedOrders = arrivedOrders;
	}

	public int getOrderNumber() {
		return arrivedOrders.size();
	}

	public void setOrderNumber(int orderNumber) {
		this.orderNumber = orderNumber;
	}

	public int getOrderLineNumber() {
		int num = 0;
		for (Order order : arrivedOrders) {
			num += order.getOrderLines().size();
		}
		return num;
	}

	public void setOrderLineNumber(int orderLineNumber) {
		this.orderLineNumber = orderLineNumber;
	}

	public int getFulfilledOrderNumber() {
		int num = 0;
		for (Order order : arrivedOrders) {
			if (order.isFulfilled())
				num++;
		}
		return num;
	}

	public void setFulfilledOrderNumber(int fulfilledOrderNumber) {
		this.fulfilledOrderNumber = fulfilledOrderNumber;
	}

	public int getFulfilledOrderLineNumber() {
		int num = 0;
		for (Order order : arrivedOrders) {
			for (OrderLine line : order.getOrderLines())
				if (line.isFulfilled())
					num++;
		}
		return num;
	}

	public void setFulfilledOrderLineNumber(int fulfilledOrderLineNumber) {
		this.fulfilledOrderLineNumber = fulfilledOrderLineNumber;
	}
	
	public List<ShipmentRequirement> getShipmentRequirements() {
        return shipmentRequirements;
    }
    
    public void setShipmentRequirements(List<ShipmentRequirement> shipmentRequirements) {
        this.shipmentRequirements = shipmentRequirements;
    }
    
    public void addShipmentRequirement(ShipmentRequirement requirement) {
        this.shipmentRequirements.add(requirement);
    }

}