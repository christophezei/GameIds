package com.game.classes.server;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

import com.game.helper.Util;

public class RPCServerDriver {
	public static void main(String[] args) throws IOException, TimeoutException {
		Scanner scan = new Scanner(System.in);
		System.out.println("Enter how many zones you want to have in your game: ");
		int nbOfZones = scan.nextInt();
		Util.saveZoneNumberToFile(String.valueOf(nbOfZones));
		for (int zoneId = 0; zoneId < nbOfZones; zoneId++) {
			new Thread(new RPCServer(zoneId)).start();
		}
	}
}
