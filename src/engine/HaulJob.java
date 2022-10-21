package engine;

/**
 * @author Nicholas Contreras
 */

public class HaulJob {

	private final Item item;
	private final Order pickupOrder, deliveryOrder;
	private final int maxVolume;

	private int routeLength;

	public HaulJob(Item item, Order pickupOrder, Order deliveryOrder, int maxVolume, int maxSpend) {
		if (pickupOrder.isBuy()) {
			throw new IllegalArgumentException("Cannot start with a buy order");
		}
		if (!deliveryOrder.isBuy()) {
			throw new IllegalArgumentException("Cannot end with a sell order");
		}

		this.item = item;
		this.pickupOrder = pickupOrder;
		this.deliveryOrder = deliveryOrder;
		this.maxVolume = (int) Math.min(Math.min(Math.min(pickupOrder.getVolume(), deliveryOrder.getVolume()),
				maxSpend / pickupOrder.getPrice()), (maxVolume / item.getVolume()));
		routeLength = -1;
	}

	public Order getPickupOrder() {
		return pickupOrder;
	}

	public Order getDeliveryOrder() {
		return deliveryOrder;
	}

	public int getRouteLength() {
		return routeLength;
	}

	public void setRouteLength(int routeLength) {
		this.routeLength = routeLength;
	}

	private double getPriceDifference() {
		return deliveryOrder.getPrice() - pickupOrder.getPrice();
	}

	private double getTotalProfit() {
		return getPriceDifference() * maxVolume;
	}

	public double getProfitPerJump() {
		return getTotalProfit() / Math.max(routeLength, 1);
	}

	public int getMaxVolume() {
		return maxVolume;
	}

	public boolean isViable() {
		return maxVolume > 0 && !pickupOrder.getStation().equals(deliveryOrder.getStation()) && getTotalProfit() > 0;
	}

	public Object[] seperateForTable() {
		return new Object[] { item.getName(), pickupOrder.getStation().getName(), pickupOrder.getPrice(),
				deliveryOrder.getStation().getName(), deliveryOrder.getPrice(), maxVolume,
				(int) Math.round(getTotalProfit()), routeLength, (int) Math.round(getProfitPerJump()) };
	}

	@Override
	public String toString() {
		return item + " from " + pickupOrder.getStation() + " to " + deliveryOrder.getStation();
	}

	@Override
	public int hashCode() {
		return item.getItemID();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof HaulJob) {
			HaulJob other = (HaulJob) o;
			return this.pickupOrder.equals(other.pickupOrder) && this.deliveryOrder.equals(other.deliveryOrder);
		} else {
			return false;
		}
	}
}
