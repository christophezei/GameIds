package com.game.classes.client;

import com.game.helper.Util;
import com.game.models.PlayerModel;
import com.game.ui.MapGidLayout;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;

import javax.swing.JFrame;

public class Client implements Runnable {
	private ConnectionFactory factory;
	private Connection connection;
	private Channel channel;
	private String requestQueueName = "";
	private static final String EXCHANGE_NAME = "zone_";
	private static final String EXCHANGE_TYPE = "topic";
	private PlayerModel playerModel = null;
	private Random random = new Random();
	private String zoneId;
	private String prevZoneId;
	private String isCollide;
	private String isNeighbour;
	private String neighbourZoneId;
	private String neighbourPlayerUsername;
	private String dim;
	private MapGidLayout map;
	private JFrame f;
	private String playerCoordinates;

	protected Client(PlayerModel playerModel) throws IOException, TimeoutException {
		this.requestQueueName = "rpc_queue_main";
		this.playerModel = playerModel;
		this.factory = Util.connectToServer(factory);
		connection = factory.newConnection();
		channel = connection.createChannel();
		this.map = new MapGidLayout();
		this.f = new JFrame();
	}

	@Override
	public void run() {
		Scanner scanner = new Scanner(System.in);
		String pressedKey = "";
		int positionX = this.playerModel.getPositionX();
		int positionY = this.playerModel.getPositionY();
		System.out.println("press 'e' to enter the map from a random entry");
		try {
			consumeCoordinates();
		} catch (IOException | TimeoutException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		while (true) {
			pressedKey = scanner.nextLine();
			if (pressedKey.equals("/exit")) {
				try {
					this.close(this.channel);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			try {
				this.prevZoneId = this.zoneId;
				this.playerMovement(pressedKey, positionX, positionY);
				positionX = this.playerModel.getPositionX();
				positionY = this.playerModel.getPositionY();
				try {
					if (isNeighbour.equals("1"))
						this.sayHello();
					broadCastCoordinatesToAllZones();
					if (prevZoneId != null || this.zoneId != null) {
						this.consumeMessages(this.zoneId, prevZoneId);
					}
					System.out.println("Player in zone " + Integer.parseInt(this.zoneId) + " at position " + positionX
							+ " " + positionY);
				} catch (TimeoutException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (TimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void initGrid(MapGidLayout map) {
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.add(map);
		f.setSize(400, 400);
		f.setLocation(200, 200);
		f.setVisible(true);
	}

	private void consumeMessages(String zoneId, String prevZoneId) throws IOException, TimeoutException {
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();

		String queueName = "zone_" + zoneId;
		String prevQueueName = "zone_" + prevZoneId;
		channel.exchangeDeclare(EXCHANGE_NAME + zoneId, EXCHANGE_TYPE);
		channel.queueDeclare(queueName, false, false, false, null);
		if (!zoneId.equals(prevZoneId) && prevZoneId != null) {
			channel.queueUnbind(prevQueueName, EXCHANGE_NAME + prevZoneId, "_" + this.playerModel.getUserName());
			channel.queueBind(queueName, EXCHANGE_NAME + zoneId, "_" + this.playerModel.getUserName());
		} else {
			channel.queueBind(queueName, EXCHANGE_NAME + zoneId, "_" + this.playerModel.getUserName());

		}
		System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

		DeliverCallback deliverCallback = (consumerTag, delivery) -> {
			String message = new String(delivery.getBody(), "UTF-8");
			System.out.println(" [x] Received '" + delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
		};
		channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
		});
	}

	private void sayHello() {
		try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
			String message = "hello from " + this.playerModel.getUserName();
			channel.exchangeDeclare(EXCHANGE_NAME + neighbourZoneId, EXCHANGE_TYPE);
			channel.basicPublish(EXCHANGE_NAME + neighbourZoneId, "_" + this.neighbourPlayerUsername, null,
					message.getBytes("UTF-8"));
			System.out.println(" [x] Sent '" + "zone_" + this.neighbourZoneId + "_" + this.neighbourPlayerUsername
					+ "':'" + message + "'");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void consumeCoordinates() throws IOException, TimeoutException {
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();

		String queueName = "BRODACAST";
		channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
		channel.queueDeclare(queueName, false, false, false, null);
		channel.queueBind(queueName, EXCHANGE_NAME, "BRODACAST");

		System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

		DeliverCallback deliverCallback = (consumerTag, delivery) -> {
			String message = new String(delivery.getBody(), "UTF-8");
			System.out.println(" [x] Received '" + delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
			this.playerCoordinates = message;
			this.draw();
		};
		channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
		});
	}

	private void broadCastCoordinatesToAllZones() {
		try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
			String message = this.playerModel.getPositionX() + ":" + this.playerModel.getPositionY() + ":"
					+ this.playerModel.getUserName();
			channel.exchangeDeclare(EXCHANGE_NAME + "BROADCAST", "fanout");
			channel.basicPublish(EXCHANGE_NAME, "BROADCAST", null, message.getBytes("UTF-8"));
			System.out.println(" [x] Sent '" + "Broadcast" + "':'" + message + "'");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void playerMovement(String pressedKey, int positionX, int positionY) throws TimeoutException {
		if (pressedKey.equals("e")) {
			this.checkPlayerInfo();
			this.playerModel.setPositionX(random.nextInt(Integer.parseInt(dim)));
			this.playerModel.setPositionY(random.nextInt(Integer.parseInt(dim)));
			this.checkPlayerInfo();
			positionX = this.playerModel.getPositionX();
			positionY = this.playerModel.getPositionY();
			if (isCollide.equals("1")) {
				this.playerModel.setPositionX(random.nextInt(Integer.parseInt(dim)));
				this.playerModel.setPositionY(random.nextInt(Integer.parseInt(dim)));
			}
		}
		if (pressedKey.equals("w")) {
			positionX = incPositionX(positionX);
			this.checkPlayerInfo();
			if (isCollide.equals("1")) {
				positionX = decPositionX(positionX);
				System.out.println("Can't move to the forward we have a collision");
			}

		} else if (pressedKey.equals("s")) {
			positionX = decPositionX(positionX);
			this.checkPlayerInfo();
			if (isCollide.equals("1")) {
				positionX = incPositionX(positionX);
				System.out.println("Can't move to the backward we have a collision");
			}
		} else if (pressedKey.equals("d")) {
			positionY = incPositionY(positionY);
			this.checkPlayerInfo();
			if (isCollide.equals("1")) {
				positionY = decPositionY(positionY);
				System.out.println("Can't move to the right we have a collision");
			}

		} else if (pressedKey.equals("a")) {
			positionY = decPositionY(positionY);
			this.checkPlayerInfo();
			if (isCollide.equals("1")) {
				positionY = incPositionY(positionY);
				System.out.println("Can't move to the left we have a collision");
			}

		}
	}

	private int decPositionY(int positionY) {
		if (positionY <= 0) {
			positionY = Integer.parseInt(dim);
		}
		positionY -= 1;
		this.playerModel.setPositionY(positionY);
		return positionY;
	}

	private int incPositionY(int positionY) {
		positionY += 1;
		this.playerModel.setPositionY((positionY) % Integer.parseInt(dim));
		return positionY;
	}

	private int decPositionX(int positionX) {
		if (positionX <= 0) {
			positionX = Integer.parseInt(dim);
		}
		positionX -= 1;
		this.playerModel.setPositionX(positionX);
		return positionX;
	}

	private int incPositionX(int positionX) {
		positionX += 1;
		this.playerModel.setPositionX((positionX) % Integer.parseInt(dim));
		return positionX;
	}

	private void checkPlayerInfo() throws TimeoutException {
		String playerZone = "";

		try {
			playerZone = this.getPlayerZone(this.playerModel);
			String[] parts = playerZone.split(":");
			String collideBool = parts[0];
			String isNeighbourBool = parts[1];
			String neighbourId = parts[2];
			String neighbourName = parts[3];
			String zone = parts[4];
			String dime = parts[5];

			this.zoneId = zone;
			this.isCollide = collideBool;
			this.isNeighbour = isNeighbourBool;
			this.neighbourZoneId = neighbourId;
			this.neighbourPlayerUsername = neighbourName;
			this.dim = dime;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void draw() {
		String[] parts = this.playerCoordinates.split(":");
		String positionX = parts[0];
		String positionY = parts[1];
		String userName = parts[2];
		this.initGrid(this.map);
		this.map.playerAtPosition(Integer.parseInt(positionX), Integer.parseInt(positionY), userName);
	}

	private void close(Channel channel) throws IOException {
		channel.queueUnbind("zone_" + this.zoneId, EXCHANGE_NAME + this.zoneId, "_" + this.playerModel.getUserName());
		System.exit(0);
	}

	private String getPlayerZone(PlayerModel playerModel) throws IOException, InterruptedException, TimeoutException {
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();
		final String corrId = UUID.randomUUID().toString();
		String replyQueueName = channel.queueDeclare().getQueue();
		AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(corrId).replyTo(replyQueueName)
				.build();

		FileOutputStream fos = new FileOutputStream("serialization.txt");
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(playerModel);

		String message = playerModel.toString();
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
}
