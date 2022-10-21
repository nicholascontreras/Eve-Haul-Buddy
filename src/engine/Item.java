package engine;

/**
 * @author Nicholas Contreras
 */

public class Item {

	private final String name;
	private final int itemID;
	private final double volume;

	public Item(String name, int itemID, double volume) {
		this.name = name;
		this.itemID = itemID;
		this.volume = volume;
	}

	public String getName() {
		return name;
	}

	public int getItemID() {
		return itemID;
	}

	public double getVolume() {
		return volume;
	}

	@Override
	public int hashCode() {
		return itemID;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Item) {
			return this.itemID == ((Item) other).itemID;
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return name;
	}
}
