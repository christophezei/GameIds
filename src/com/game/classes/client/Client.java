package com.game.classes.client;

import com.game.helper.Util;
import com.game.models.PlayerModel;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Random;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;

public class Client implements Runnable, AutoCloseable {
	private ConnectionFactory factory;
	private Connection connection;
	private Channel channel;
	private String requestQueueName = "";
	private static final String EXCHANGE_NAME = "zone_";
	private PlayerModel playerModel = null;
	private Random random = new Random();
	private String zoneId;
	private String isCollide;
	private String isNeighbour;
	private String neighbourZoneId;

	protected Client(PlayerModel playerModel) throws IOException, TimeoutException {
		this.requestQueueName = "rpc_queue_main";
		this.playerModel = playerModel;
		//this.playerModel.setPositionX(random.nextInt(4));
		//this.playerModel.setPositionY(random.nextInt(4));
		this.playerModel.setPositionX(0);
		this.playerModel.setPositionY(0);
		factory = Util.connectToServer(factory);
		connection = factory.newConnection();
		channel = connection.createChannel();
	}

	@Override
	public void run() {
		Scanner scanner = new Scanner(System.in);
		String pressedKey = "";
		int positionX = this.playerModel.getPositionX();
		int positionY = this.playerModel.getPositionY();
		try {
			consumeMessages();
		} catch (IOException | TimeoutException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		while (true) {
			this.checkPlayerZone();
			System.out.println("Player in zone " + Integer.parseInt(this.zoneId));
			positionX = this.playerModel.getPositionX();
			positionY = this.playerModel.getPositionY();
			try {
				if (this.isNeighbour.equals("1")) {
					produceMessages();
				}

			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			pressedKey = scanner.nextLine();
			this.playerMovement(pressedKey, positionX, positionY, this.isCollide);
		}
	}
	
	private void consumeMessages() throws IOException, TimeoutException {
			Connection connection = factory.newConnection();
		    Channel channel = connection.createChannel();

		    channel.exchangeDeclare(EXCHANGE_NAME , "topic");
		    String queueName = channel.queueDeclare().getQueue();

		    channel.queueBind(queueName, EXCHANGE_NAME , "zone_0");
		    channel.queueBind(queueName, EXCHANGE_NAME , "zone_1");
		    channel.queueBind(queueName, EXCHANGE_NAME , "zone_2");
		    channel.queueBind(queueName, EXCHANGE_NAME , "zone_3");

		    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

		    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
		        String message = new String(delivery.getBody(), "UTF-8");
		        System.out.println(" [x] Received '" +
		            delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
		    };
		    channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });		
	}

	private void produceMessages() {
		if (!neighbourZoneId.equals("-1")) {
			try (Connection connection = factory.newConnection();
			        Channel channel = connection.createChannel()) {
					String message = "hello";
					channel.exchangeDeclare(EXCHANGE_NAME , "topic");
			        channel.basicPublish(EXCHANGE_NAME ,  "zone_" + this.neighbourZoneId, null, message.getBytes("UTF-8"));
			        System.out.println(" [x] Sent '" +  "zone_" + this.neighbourZoneId + "':'" + message + "'");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
	}


	private void playerMovement(String pressedKey, int positionX, int positionY, String isCollide) {
		if (pressedKey.equals("w")) {
			if (isCollide.equals("0"))
				this.playerModel.setPositionX((positionX + 1) % 4);
			else if (isCollide.equals("1")) {
				System.out.println("Can't move otherwise players will collide");
			}
		} else if (pressedKey.equals("s")) {
			if (positionX <= 0) {
				positionX = 4;
			}
			if (isCollide.equals("0"))
				this.playerModel.setPositionX(positionX - 1);
			else if (isCollide.equals("1")) {
				System.out.println("Can't move otherwise players will collide");
			}
		} else if (pressedKey.equals("d")) {
			if (isCollide.equals("0"))
				this.playerModel.setPositionY((positionY + 1) % 4);
			else if (isCollide.equals("1")) {
				System.out.println("Can't move otherwise players will collide");
			}
		} else if (pressedKey.equals("a")) {
			if (positionY <= 0) {
				positionY = 4;
			}
			if (isCollide.equals("0"))
				this.playerModel.setPositionY(positionY - 1);
			else if (isCollide.equals("1")) {
				System.out.println("Can't move otherwise players will collide");
			}
		}
	}

	private void checkPlayerZone() {
		String playerZone = "";

		try {
			playerZone = this.getPlayerZone(this.playerModel);
			String[] parts = playerZone.split(":");
			String collideBool = parts[0];
			String isNeighbourBool = parts[1];
			String neighbourId = parts[2];
			String zone = parts[3];
			this.zoneId = zone;
			this.isCollide = collideBool;
			this.isNeighbour = isNeighbourBool;
			this.neighbourZoneId = neighbourId;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getPlayerZone(PlayerModel playerModel) throws IOException, InterruptedException {
		final String corrId = UUID.randomUUID().toString();
		String replyQueueName = channel.queueDeclare().getQueue();
		AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(corrId).replyTo(replyQueueName)
				.build();

		FileOutputStream fos = new FileOutputStream("serialization.txt");
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(playerModel);

		String message = "Sending object..";
		channel.basicPublish("", requestQueueName, props, message.getBytes("UTF-8"));

		final BlockingQueue<String> response = new ArrayBlockingQueue<>(1);

		String ctag = channel.basicConsume(replyQueueName, true, (consumerTag, delivery) -> {
			if (delivery.getProperties().getCorrelationId().equals(corrId)) {
				response.offer(new String(delivery.getBody(), "UTF-8"));
			}
		}, consumerTag -> {
		});

		String result = response.take();
		channel.basicCancel(ctag);
		return result;
	}

	@Override
	public void close() throws Exception {
		connection.close();

	}
}
