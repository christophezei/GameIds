package com.game.helper;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import com.rabbitmq.client.ConnectionFactory;

public class Util {
	private static String uri = "amqp://rdraipzf:RX4SqBs7Zrgr9_jPmJLurG-i6znoF3ow@kangaroo.rmq.cloudamqp.com/rdraipzf";
	public static ConnectionFactory connectToServer(ConnectionFactory factory) {
		factory = new ConnectionFactory();
		try {
			factory.setUri(uri);
		} catch (KeyManagementException | NoSuchAlgorithmException | URISyntaxException e) {
			e.printStackTrace();
		}
		return factory;
	}
	
	public static void saveZoneNumberToFile(String zoneNumber) {
		try {
			Path currentRelativePath = Paths.get("");
			FileWriter myWriter = new FileWriter(currentRelativePath.toAbsolutePath().toString() + "/zoneNumber.txt");
			myWriter.write(zoneNumber);
			myWriter.close();
		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
	}
	
	public static String getZoneNumber(String absolutePath){
		String zoneNumber = null;

		try {
			zoneNumber = new String(Files.readAllBytes(Paths.get(absolutePath)), StandardCharsets.UTF_8);
		} catch (IOException e) {
			// can print any error
		}
		return zoneNumber;
	}

}
