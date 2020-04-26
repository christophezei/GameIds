package com.game.classes;

import com.game.interfaces.IPlayer;

public class Player implements IPlayer {

	@Override
	public void moveHorizontal(int positionX) {
		positionX = positionX + 1;

	}

	@Override
	public void moveVertical(int positionY) {
		positionY = positionY + 1;

	}

}
