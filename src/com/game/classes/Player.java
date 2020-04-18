package com.game.classes;

import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import com.game.interfaces.IPlayer;
import com.rabbitmq.client.ConnectionFactory;

public class Player implements IPlayer {
	
	private static String uri = "amqp://rdraipzf:RX4SqBs7Zrgr9_jPmJLurG-i6znoF3ow@kangaroo.rmq.cloudamqp.com/rdraipzf";

	@Override
	public ConnectionFactory connectToServer(ConnectionFactory factory) {
		 factory = new ConnectionFactory();
	     try {
			factory.setUri(uri);
		} catch (KeyManagementException | NoSuchAlgorithmException | URISyntaxException e) {
			e.printStackTrace();
		}
	     return factory;
	}

}
