package com.game.classes.client;

import com.game.helper.Util;
import com.game.models.PlayerModel;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.Random;
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
	private String dir;
	
	protected Client(PlayerModel playerModel) throws IOException, TimeoutException {
		playerModel.getUserName();
		this.dir = System.getProperty("user.dir");
		String absolutePath = dir + "/zoneNumber.txt";
		//requestQueueName = "rpc_queue_" + random.nextInt(Integer.parseInt(Util.getZoneNumber(absolutePath)));
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
		String response = "";
		try {
			response = this.call(this.playerModel.getPositionX(), this.playerModel.getPositionY());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(" [.] Got '" + response + "'");
	}

	public String call(int positionX, int positionY) throws IOException, InterruptedException {
		final String corrId = UUID.randomUUID().toString();
		System.out.println(requestQueueName);
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
