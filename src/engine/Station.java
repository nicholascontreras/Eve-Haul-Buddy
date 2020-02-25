package engine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * @author Nicholas Contreras
 */

public class Station {

	private final String name;
	private final int stationID;
	private final int systemID;
	private final int regionID;
	
	private final double securityLevel;

	public Station(String name, int stationID, int systemID, int regionID, double securityLevel) {
		this.name = name;
		this.stationID = stationID;
		this.systemID = systemID;
		this.regionID = regionID;
		this.securityLevel = securityLevel;
	}

	public String getName() {
		return name;
	}

	public int getStationID() {
		return stationID;
	}

	public int getSystemID() {
		return systemID;
	}
	
	public int getRegionID() {
		return regionID;
	}
	
	public double getSecurityLevel() {
		return securityLevel;
	}

	@Override
	public int hashCode() {
		return stationID;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Station) {
			return this.stationID == ((Station) other).stationID;
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return name;
	}
}
