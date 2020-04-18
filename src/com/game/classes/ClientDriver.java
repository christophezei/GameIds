package com.game.classes;

import com.game.models.PlayerModel;

public class ClientDriver {

	public static void main(String[] args){
		PlayerModel player = new PlayerModel();
		player.setUserName(args[0]);
		new Thread(new Client(player)).start();
	}
}
