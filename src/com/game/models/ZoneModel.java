package com.game.models;

public class ZoneModel {
	public int start_x;
	public int end_x;
	public int start_y;
	public int end_y;
	private int zoneId;
	
	public ZoneModel(int start_x, int end_x, int start_y, int end_y) {
		this.start_x = start_x;
		this.end_x = end_x;
		this.start_y = start_y;
		this.end_y = end_y;
	}
	
	public int getZoneId() {
		return zoneId;
	}

	public void setZoneId(int zoneId) {
		this.zoneId = zoneId;
	}
}
