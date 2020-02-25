package engine;

/**
 * @author Nicholas Contreras
 */

public class Order {

	private final Item item;
	private final Station station;
	private final boolean isBuy;
	private final double price;
	private final int volume;

	public Order(Item item, Station station, boolean isBuy, double price, int volume) {
		this.item = item;
		this.station = station;
		this.isBuy = isBuy;
		this.price = price;
		this.volume = volume;

	}

	public Item getItem() {
		return item;
	}

	public Station getStation() {
		return station;
	}

	public boolean isBuy() {
		return isBuy;
	}

	public double getPrice() {
		return price;
	}

	public int getVolume() {
		return volume;
	}

	@Override
	public int hashCode() {
		return item.getItemID();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Order) {
			Order other = (Order) o;
			return this.item.equals(other.item) && this.station.equals(other.station) && this.isBuy == other.isBuy
					&& this.price == other.price && this.volume == other.volume;
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return item.getName() + " at " + station.getName();
	}
}
