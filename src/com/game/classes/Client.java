package com.game.classes;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

import com.game.models.PlayerModel;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class Client implements Runnable{
	private ConnectionFactory factory;
	private String clientName = null;
	private String EXCHANGE_NAME="topic_chat_";
	private Player player;
	 private final static String QUEUE_NAME = "hello";
	
	protected Client(PlayerModel playerModel) {
		this.clientName = playerModel.getUserName();
		player = new Player();
		factory = player.connectToServer(factory);
		 
	}

	@Override
	public void run() {
		try (Connection connection = factory.newConnection();
	            Channel channel = connection.createChannel()) {
	            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
	            String message = "Hello World!";
	            channel.basicPublish("", QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));
	            System.out.println(" [x] Sent '" + message + "'");
	        } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		Connection connection;
	    Channel channel;
		try {
			connection = factory.newConnection();
			channel = connection.createChannel();
			channel.queueDeclare(QUEUE_NAME, false, false, false, null);
		    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

		    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
		        String message = new String(delivery.getBody(), "UTF-8");
		        System.out.println(" [x] Received '" + message + "'");
		    };
		    channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
		} catch (IOException | TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}	
	
}
