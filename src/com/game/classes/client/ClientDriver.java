package com.game.classes.client;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;
import com.game.models.PlayerModel;


public class ClientDriver {
	public static void main(String[] args) throws IOException, TimeoutException{
		PlayerModel player = new PlayerModel();
		System.out.println("Enter player username");
		Scanner scanner = new Scanner(System.in);
		String playerUserName = scanner.nextLine();
		player.setUserName(playerUserName); 
		new Thread(new Client(player)).start();
	}


}
