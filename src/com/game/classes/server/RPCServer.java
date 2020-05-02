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
	private ZoneModel zoneModel;
	private Zone zone;
	private HashMap<String, PlayerModel> players;
	private String dim;

	public RPCServer(ZoneModel zoneModel) throws IOException, TimeoutException {
		this.zone = new Zone();
		this.factory = Util.connectToServer(factory);
		this.connection = factory.newConnection();
		this.channel = connection.createChannel();
		this.players = new HashMap<String, PlayerModel>();
		this.zoneModel = zoneModel;
		this.dim = this.zoneModel.getDim();
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
		String currentUsername, collideBool = "0", isNeighbourBool = "0", neighbourZoneId = "-1", neighbourName = "-1";

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
				} else if ((diffX >= -1 && diffX <= 1) && (diffY >= -1 && diffY <= 1)) {
					System.out.println("Players are Neighbours !!");
					neighbourZoneId = ((PlayerModel) mapElement.getValue()).getZoneId();
					neighbourName = ((PlayerModel) mapElement.getValue()).getUserName();
					isNeighbourBool = "1";
				}
			}
		}
		return collideBool + ":" + isNeighbourBool + ":" + neighbourZoneId + ":" + neighbourName;
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
				this.zoneModel = this.zone.checkPlayerZone(playerModel.getPositionX(), playerModel.getPositionY(), Integer.parseInt(dim));
				playerModel.setZoneId(String.valueOf(zoneModel.getZoneId()));
				this.addPlayer(playerModel);
				System.out.println(playerModel.getUserName());
				String message = new String(delivery.getBody(), "UTF-8");
				System.out.println(" [.] (" + message + ")");
				response = this.checkIfPlayerIsNeighbour(playerModel);
				response += ":" + String.valueOf(zoneModel.getZoneId()) + ":" + dim;
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
