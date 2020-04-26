package com.game.classes;

import com.game.models.ZoneModel;

public class Zone {
	public String checkPlayerZone(int[][] map,int positionX, int positionY,ZoneModel zoneModel) {
		String message = "";
		for (int row = 0; row < map.length; row++) {
			for (int col = 0; col < map.length; col++) {
				if (row >= zoneModel.start_x && row <= zoneModel.end_x && col >= zoneModel.start_y && col <= zoneModel.end_y) {
					if(map[row][col] == map[positionX][positionY])
						message = "Player in zone " + zoneModel.getZoneId();
				}
			}
		}
		return message;
	}
	

	public void printZoneCoord(int[][] zoneCoord) {
		for (int row = 0; row < zoneCoord.length; row++) {
			for (int col = 0; col < zoneCoord.length; col++) {
				System.out.print(zoneCoord[row][col] + "\t");
			}
			System.out.println();
		}
	}

	/*
	 * public void printZoneCoord(int[][] zoneCoord, ZoneModel zone ) { for (int row
	 * = 0; row < zoneCoord.length; row++) { for (int col = 0; col <
	 * zoneCoord.length; col++) { if (row >= zone.start_x && row <= zone.end_x &&
	 * col >= zone.start_y && col <= zone.end_y)
	 * System.out.print(zoneCoord[row][col] + "\t"); } System.out.println(); } }
	 */


	/*public int[][] fillZoneCoord(int[][] zoneCoord, ZoneModel zone) {
		int subRow = 0;
		int subCol = 0;
		for (int row = 0; row < zoneCoord.length; row++) {
			for (int col = 0; col < zoneCoord.length; col++) {
				if (row >= zone.start_x && row <= zone.end_x && col >= zone.start_y && col <= zone.end_y) {
					if (zoneCoord[row][col] != 0) {
						this.subZoneCoord[subRow][subCol] = zoneCoord[row][col];
					}

				}

			}
		}
		return subZoneCoord;
	}*/
}
