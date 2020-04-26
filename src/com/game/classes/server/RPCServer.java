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
	private String RPC_QUEUE_NAME = "";
	private Connection connection;
	private Channel channel;
	private int[][] map;
	private ZoneModel zoneModel = new ZoneModel(0, 0, 0, 0);
	private Zone zone;
	private String rpc_queue_tag = "";

	public RPCServer(String rpc_queue_tag, int[][] map) throws IOException, TimeoutException {
		this.map = map;
		this.zone = new Zone();
		this.RPC_QUEUE_NAME = "rpc_queue_" + rpc_queue_tag;
		this.factory = Util.connectToServer(factory);
		this.connection = factory.newConnection();
		this.channel = connection.createChannel();
	}

	private ZoneModel getZoneModel(int positionX, int positionY) {
		ZoneModel zone0 = new ZoneModel(0, 1, 0, 1);
		ZoneModel zone1 = new ZoneModel(0, 1, 2, 3);
		ZoneModel zone2 = new ZoneModel(2, 3, 0, 1);
		ZoneModel zone3 = new ZoneModel(2, 3, 2, 3);
		if (positionX >= zone0.start_x && positionX <= zone0.end_x && positionY >= zone0.start_y
				&& positionY <= zone0.end_y) {
			zoneModel = zone0;
			zoneModel.setZoneId(0);
			this.rpc_queue_tag = String.valueOf(zoneModel.getZoneId());
		} else if (positionX >= zone1.start_x && positionX <= zone1.end_x && positionY >= zone1.start_y
				&& positionY <= zone1.end_y) {
			zoneModel = zone1;
			zoneModel.setZoneId(1);
			this.rpc_queue_tag = String.valueOf(zoneModel.getZoneId());

		} else if (positionX >= zone2.start_x && positionX <= zone2.end_x && positionY >= zone2.start_y
				&& positionY <= zone2.end_y) {
			zoneModel = zone2;
			zoneModel.setZoneId(2);
			this.rpc_queue_tag = String.valueOf(zoneModel.getZoneId());
		} else if (positionX >= zone3.start_x && positionX <= zone3.end_x && positionY >= zone3.start_y
				&& positionY <= zone3.end_y) {
			zoneModel = zone3;
			zoneModel.setZoneId(3);
			this.rpc_queue_tag = String.valueOf(zoneModel.getZoneId());
		}
		return zoneModel;
	}

	@Override
	public void run() {

		try (Connection connection = this.connection; Channel channel = this.channel) {
			channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);
			channel.queuePurge(RPC_QUEUE_NAME);

			channel.basicQos(1);

			System.out.println(" [x] Awaiting RPC requests from RPC " + RPC_QUEUE_NAME);
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
					this.zoneModel = this.getZoneModel(Integer.parseInt(positionX), Integer.parseInt(positionY));
					response = zone.checkPlayerZone(map, Integer.parseInt(positionX), Integer.parseInt(positionY),
							this.zoneModel);
					new Thread(new RPCServer(rpc_queue_tag, map)).start();
				} catch (RuntimeException e) {
					System.out.println(" [.] " + e.toString());
				} catch (TimeoutException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					channel.basicPublish("", delivery.getProperties().getReplyTo(), replyProps,
							response.getBytes("UTF-8"));
					channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
					// RabbitMq consumer worker thread notifies the RPC server owner thread
					synchronized (monitor) {
						monitor.notify();
					}
				}
			};

			channel.basicConsume(RPC_QUEUE_NAME, false, deliverCallback, (consumerTag -> {
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
		} catch (IOException | TimeoutException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
