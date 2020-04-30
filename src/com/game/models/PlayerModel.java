package com.game.models;

import java.io.Serializable;

public class PlayerModel implements Serializable {
	private String userName;
	private String zoneId;
	private int positionX;
	private int positionY;

	public int getPositionX() {
		return positionX;
	}

	public void setPositionX(int positionX) {
		this.positionX = positionX;
	}

	public int getPositionY() {
		return positionY;
	}

	public void setPositionY(int positionY) {
		this.positionY = positionY;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getZoneId() {
		return zoneId;
	}

	public void setZoneId(String userId) {
		this.zoneId = userId;
	}

}
