package com.game.classes.client;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.game.models.PlayerModel;

public class ClientDriver {

	public static void main(String[] args) throws IOException, TimeoutException{
		PlayerModel player = new PlayerModel();
		player.setUserName(args[0]);
		new Thread(new Client(player)).start();
	}
}
