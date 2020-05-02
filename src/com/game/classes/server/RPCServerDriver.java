package com.game.classes.server;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

import com.game.models.ZoneModel;

public class RPCServerDriver {
	public static void main(String[] args) throws IOException, TimeoutException {
		System.out.println("Enter Map dimension");
		ZoneModel zoneModel = new ZoneModel(0, 0, 0, 0);
		Scanner scanner = new Scanner(System.in);
		String dim = scanner.nextLine();
		zoneModel.setDim(dim);
		new Thread(new RPCServer(zoneModel)).start();
	}

}