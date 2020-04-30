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
import java.io.UnsupportedEncodingException;
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
	
	protected Client(PlayerModel playerModel) throws IOException, TimeoutException {
		this.requestQueueName = "rpc_queue_main";
		this.playerModel = playerModel;
		this.playerModel.setPositionX(random.nextInt(4));
		this.playerModel.setPositionY(random.nextInt(4));
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
		while (true) {
			switch (this.checkPlayerZone()) {
			case 0:
				try {
					this.initExchange(0);
					this.consumeMessage(0,"zone_0");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("Player in zone 0");
				positionX = this.playerModel.getPositionX();
				positionY = this.playerModel.getPositionY();
				try {
					this.communicateWithNeighbour(0,"zone_0");
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				pressedKey = scanner.nextLine();
				this.playerMovement(pressedKey, positionX, positionY);
				break;
			case 1:
				try {
					this.initExchange(1);
					this.consumeMessage(1,"zone_1");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("Player in zone 1");
				positionX = this.playerModel.getPositionX();
				positionY = this.playerModel.getPositionY();
				try {
					this.communicateWithNeighbour(1,"zone_1");
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				pressedKey = scanner.nextLine();
				this.playerMovement(pressedKey, positionX, positionY);
				break;
			case 2:
				try {
					this.initExchange(2);
					this.consumeMessage(2,"zone_2");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("Player in zone 2");
				positionX = this.playerModel.getPositionX();
				positionY = this.playerModel.getPositionY();
				try {
					this.communicateWithNeighbour(2,"zone_2");
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				pressedKey = scanner.nextLine();
				this.playerMovement(pressedKey, positionX, positionY);
				break;

			case 3:
				try {
					this.initExchange(3);
					this.consumeMessage(3,"zone_3");
				} catch (IOException e) {
					e.printStackTrace();
				}
				System.out.println("Player in zone 3");
				positionX = this.playerModel.getPositionX();
				positionY = this.playerModel.getPositionY();
				try {
					this.communicateWithNeighbour(3,"zone_3");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				pressedKey = scanner.nextLine();
				this.playerMovement(pressedKey, positionX, positionY);
				break;
			default:
				System.out.println("Player out of zones");
			}
		}
	}

	private void initExchange(int zoneId) throws IOException {
		this.channel.exchangeDeclare(EXCHANGE_NAME + zoneId, "topic");
	}

	private void consumeMessage(int zoneId, String queueRouting) throws IOException {
		this.channel.exchangeDeclare(EXCHANGE_NAME + zoneId, "topic");
	    String queueName = this.channel.queueDeclare().getQueue();
	    channel.queueBind(queueName, EXCHANGE_NAME + zoneId, queueRouting);
	    
	    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
	        String message = new String(delivery.getBody(), "UTF-8");
	        System.out.println(" [x] Received '" +
	            delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
	    };
	    channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
	  }
	/*private Boolean isPlayerNeighbour(PlayerModel player) {

	}*/

	private void communicateWithNeighbour(int zoneId, String queueRouting) throws UnsupportedEncodingException, IOException {
		this.channel.exchangeDeclare(EXCHANGE_NAME + zoneId, "topic");
		String message = "Hello from player " + this.playerModel.getUserName();
		channel.basicPublish(EXCHANGE_NAME + zoneId, queueRouting, null, message.getBytes("UTF-8"));
		System.out.println(" [x] Sent  ':'" + message + "'");
	}

	private void playerMovement(String pressedKey, int positionX, int positionY) {
		if (pressedKey.equals("w")) {
			this.playerModel.setPositionX((positionX + 1) % 4);
		} else if (pressedKey.equals("s")) {
			if (positionX <= 0) {
				positionX = 4;
			}
			this.playerModel.setPositionX(positionX - 1);
		} else if (pressedKey.equals("d")) {
			this.playerModel.setPositionY((positionY + 1) % 4);
		} else if (pressedKey.equals("a")) {
			if (positionY <= 0) {
				positionY = 4;
			}
			this.playerModel.setPositionY(positionY - 1);
		}
	}

	private int checkPlayerZone() {
		String playerZone = "";
		try {
			playerZone = this.getPlayerZone(this.playerModel);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Integer.parseInt(playerZone);
	}

	public String getPlayerZone(PlayerModel playerModel) throws IOException, InterruptedException {
		final String corrId = UUID.randomUUID().toString();
		String replyQueueName = channel.queueDeclare().getQueue();
		AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(corrId).replyTo(replyQueueName)
				.build();
		
		FileOutputStream fos = new FileOutputStream("serialization.txt");
	    ObjectOutputStream oos = new ObjectOutputStream(fos);
	    oos.writeObject(playerModel);
		
		//String message = String.valueOf(playerModel.getPositionX()) + ":" + String.valueOf(this.playerModel.getPositionY());
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
