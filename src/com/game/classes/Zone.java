package com.game.classes;

import com.game.models.ZoneModel;

public class Zone {
	private ZoneModel zoneModel;
	public ZoneModel checkPlayerZone(int positionX, int positionY) {
		ZoneModel zone0 = new ZoneModel(0, 1, 0, 1);
		ZoneModel zone1 = new ZoneModel(0, 1, 2, 3);
		ZoneModel zone2 = new ZoneModel(2, 3, 0, 1);
		ZoneModel zone3 = new ZoneModel(2, 3, 2, 3);
		if (positionX >= zone0.start_x && positionX <= zone0.end_x && positionY >= zone0.start_y
				&& positionY <= zone0.end_y) {
			zoneModel = zone0;
			zoneModel.setZoneId(0);
		} else if (positionX >= zone1.start_x && positionX <= zone1.end_x && positionY >= zone1.start_y
				&& positionY <= zone1.end_y) {
			zoneModel = zone1;
			zoneModel.setZoneId(1);

		} else if (positionX >= zone2.start_x && positionX <= zone2.end_x && positionY >= zone2.start_y
				&& positionY <= zone2.end_y) {
			zoneModel = zone2;
			zoneModel.setZoneId(2);
		} else if (positionX >= zone3.start_x && positionX <= zone3.end_x && positionY >= zone3.start_y
				&& positionY <= zone3.end_y) {
			zoneModel = zone3;
			zoneModel.setZoneId(3);
		}
		return zoneModel;
	}
}
