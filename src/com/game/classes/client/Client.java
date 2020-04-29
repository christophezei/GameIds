package com.game.classes.client;

import com.game.helper.Util;
import com.game.models.PlayerModel;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
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
	private PlayerModel playerModel = null;
	private Random random = new Random();
	
	protected Client(PlayerModel playerModel) throws IOException, TimeoutException {
		requestQueueName = "rpc_queue_main";
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
		while(true) {
			switch (this.checkPlayerZone()) {
			case 0:
				System.out.println("Player in zone 0");
				 positionX = this.playerModel.getPositionX();
				 positionY = this.playerModel.getPositionY();
				pressedKey = scanner.nextLine();
				this.playerMovement(pressedKey, positionX, positionY);	
				break;
			case 1:
				System.out.println("Player in zone 1");
				positionX = this.playerModel.getPositionX();
				positionY = this.playerModel.getPositionY();
				pressedKey = scanner.nextLine();
				this.playerMovement(pressedKey, positionX, positionY);
				break;
			case 2:
				System.out.println("Player in zone 2");
				positionX = this.playerModel.getPositionX();
				positionY = this.playerModel.getPositionY();
				pressedKey = scanner.nextLine();
				this.playerMovement(pressedKey, positionX, positionY);
				break;

			case 3:
				System.out.println("Player in zone 3");
				positionX = this.playerModel.getPositionX();
				positionY = this.playerModel.getPositionY();
				pressedKey = scanner.nextLine();
				this.playerMovement(pressedKey, positionX, positionY);
				break;
			default:
				// code block
			}	
		}
	}

	private void playerMovement(String pressedKey, int positionX, int positionY) {
		if(pressedKey.equals("w")) {
			this.playerModel.setPositionX((positionX + 1) % 4);
		}else if(pressedKey.equals("s")) {
			if(positionX <= 0) {
				positionX = 4;
			}
			this.playerModel.setPositionX(positionX - 1);
		}else if(pressedKey.equals("d")) {
			this.playerModel.setPositionY((positionY + 1) % 4);
		}else if(pressedKey.equals("a")) {
			if(positionY <= 0) {
				positionY = 4;
			}
			this.playerModel.setPositionY(positionY - 1);
		}
	}

	private int checkPlayerZone() {
		String playerZone = "";
		try {
			playerZone = this.getPlayerZone(this.playerModel.getPositionX(), this.playerModel.getPositionY());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Integer.parseInt(playerZone);
	}

	public String getPlayerZone(int positionX, int positionY) throws IOException, InterruptedException {
		final String corrId = UUID.randomUUID().toString();
		String replyQueueName = channel.queueDeclare().getQueue();
		AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(corrId).replyTo(replyQueueName)
				.build();
		
		String message = String.valueOf(positionX) + ":" + String.valueOf(positionY);
		
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
