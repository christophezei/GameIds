package com.game.classes.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import com.game.classes.Zone;
import com.game.helper.Util;
import com.game.models.PlayerModel;
import com.game.models.ZoneModel;
import com.rabbitmq.client.*;

public class RPCServer implements Runnable {
	private ConnectionFactory factory;
	private String RPC_QUEUE_NAME = "rpc_queue_main";
	private Connection connection;
	private Channel channel;
	private ZoneModel zoneModel = new ZoneModel(0, 0, 0, 0);
	private Zone zone;
	private HashMap<String, PlayerModel> players;

	public RPCServer() throws IOException, TimeoutException {
		this.zone = new Zone();
		this.factory = Util.connectToServer(factory);
		this.connection = factory.newConnection();
		this.channel = connection.createChannel();
		this.players = new HashMap<String, PlayerModel>();
	}

	@Override
	public void run() {
		try {
			System.out.println("Server Started...");
			this.initQueues();
			this.checkPlayerZone();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void initQueues() throws IOException {
		this.channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);
		this.channel.queuePurge(RPC_QUEUE_NAME);
	}

	private synchronized void addPlayer(PlayerModel playerModel) {
		this.players.put(playerModel.getUserName(), playerModel);
	}

	private String checkIfPlayerIsNeighbour(PlayerModel playerModel) {
		Iterator hmIterator = players.entrySet().iterator();
		int neighbourPosX, neighbourPosY, diffX, diffY;
		String currentUsername, collideBool = "0", isNeighbourBool = "0", neighbourZoneId = "-1";

		while (hmIterator.hasNext()) {
			Map.Entry mapElement = (Map.Entry) hmIterator.next();
			currentUsername = ((PlayerModel) mapElement.getValue()).getUserName();
			if (!currentUsername.equals(playerModel.getUserName())) {
				neighbourPosX = ((PlayerModel) mapElement.getValue()).getPositionX();
				neighbourPosY = ((PlayerModel) mapElement.getValue()).getPositionY();
				diffX = neighbourPosX - playerModel.getPositionX();
				diffY = neighbourPosY - playerModel.getPositionY();
				if (neighbourPosX == playerModel.getPositionX() && neighbourPosY == playerModel.getPositionY()) {
					System.out.println("Players collide !!");
					collideBool = "1";
				} else if (diffX == 1 || diffY == 1) {
					System.out.println("Players are Neighbours !!");
					neighbourZoneId = ((PlayerModel) mapElement.getValue()).getZoneId();
					isNeighbourBool = "1";
				}
			}
		}
		return collideBool + ":" + isNeighbourBool + ":" + neighbourZoneId;
	}

	private void checkPlayerZone() throws IOException {
		this.channel.basicQos(1);
		Object monitor = new Object();
		DeliverCallback deliverCallback = (consumerTag, delivery) -> {
			AMQP.BasicProperties replyProps = new AMQP.BasicProperties.Builder()
					.correlationId(delivery.getProperties().getCorrelationId()).build();

			String response = "";

			try {
				FileInputStream fis = new FileInputStream("serialization.txt");
				ObjectInputStream ois = new ObjectInputStream(fis);
				PlayerModel playerModel = (PlayerModel) ois.readObject();
				this.addPlayer(playerModel);
				System.out.println(playerModel.getUserName());
				String message = new String(delivery.getBody(), "UTF-8");

				System.out.println(" [.] (" + message + ")");
				System.out.println(" [.] (" + playerModel.getPositionX() + "," + playerModel.getPositionY() + ")");
				this.zoneModel = this.zone.checkPlayerZone(playerModel.getPositionX(), playerModel.getPositionY());
				playerModel.setZoneId(String.valueOf(zoneModel.getZoneId()));
				response = this.checkIfPlayerIsNeighbour(playerModel);
				response += ":" + String.valueOf(zoneModel.getZoneId());
			} catch (RuntimeException e) {
				System.out.println(" [.] " + e.toString());
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				channel.basicPublish("", delivery.getProperties().getReplyTo(), replyProps, response.getBytes("UTF-8"));
				channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
				// RabbitMq consumer worker thread notifies the RPC server owner thread
				synchronized (monitor) {
					monitor.notify();
				}
			}
		};
		this.channel.basicConsume(RPC_QUEUE_NAME, false, deliverCallback, (consumerTag -> {
		}));
		// Wait and be prepared to consume the message from RPC client.
		while (true) {
			synchronized (monitor) {
				try {
					monitor.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
