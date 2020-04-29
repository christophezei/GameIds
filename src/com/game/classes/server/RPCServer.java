package com.game.classes.server;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

import com.game.classes.Zone;
import com.game.helper.Util;
import com.game.models.ZoneModel;
import com.rabbitmq.client.*;

public class RPCServer implements Runnable {
	private ConnectionFactory factory;
	private String RPC_QUEUE_NAME = "rpc_queue_main";
	private String EXCHANGE_NAME = "direct_main";
	private String zoneQueueName = "zone_";
	private Connection connection;
	private Channel channel;
	private ZoneModel zoneModel = new ZoneModel(0, 0, 0, 0);
	private Zone zone;

	public RPCServer() throws IOException, TimeoutException {
		this.zone = new Zone();
		this.factory = Util.connectToServer(factory);
		this.connection = factory.newConnection();
		this.channel = connection.createChannel();
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
		for (int i = 0; i < 3; i++) {
			this.channel.queueDeclare(zoneQueueName + i, false, false, false, null);
			this.channel.queuePurge(RPC_QUEUE_NAME);
		}
	}

	private void checkPlayerZone() throws IOException {
		this.channel.basicQos(1);
		Object monitor = new Object();
		DeliverCallback deliverCallback = (consumerTag, delivery) -> {
			AMQP.BasicProperties replyProps = new AMQP.BasicProperties.Builder()
					.correlationId(delivery.getProperties().getCorrelationId()).build();

			String response = "";

			try {
				String message = new String(delivery.getBody(), "UTF-8");
				String[] positions = message.split(":");
				String positionX = positions[0];
				String positionY = positions[1];
				System.out.println(" [.] (" + message + ")");
				this.zoneModel = this.zone.checkPlayerZone(Integer.parseInt(positionX), Integer.parseInt(positionY));
				response = String.valueOf(zoneModel.getZoneId());
			} catch (RuntimeException e) {
				System.out.println(" [.] " + e.toString());
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
