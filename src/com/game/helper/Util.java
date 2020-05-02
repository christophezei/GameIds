package com.game.helper;

import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import com.rabbitmq.client.ConnectionFactory;

public class Util{
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
	public static HashMap<String,String> parseMap(String text) {
		HashMap<String,String> map = new HashMap<String,String>();
	    for(String keyValue: text.split(", ")) {
	        String[] parts = keyValue.split("=", 2);
	        map.put(parts[0], parts[1]);
	    }
	    return map;
	}
}
