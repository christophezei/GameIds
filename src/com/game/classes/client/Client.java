package com.game.classes.client;

import com.game.classes.Player;
import com.game.models.PlayerModel;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;

public class Client implements Runnable, AutoCloseable {
	private ConnectionFactory factory;
	private Player player;
	private Connection connection;
	private Channel channel;
	private String requestQueueName = "rpc_queue";
	private PlayerModel playerModel = null;

	protected Client(PlayerModel playerModel) throws IOException, TimeoutException {
		playerModel.getUserName();
		player = new Player();
		this.playerModel = playerModel;
		factory = player.connectToServer(factory);
		connection = factory.newConnection();
		channel = connection.createChannel();
	}

	@Override
	public void run() {
		try (Client client = new Client(playerModel)) {
			System.out.println(" [x] Requesting hello()");
			String response = client.call("hello");
			System.out.println(" [.] Got '" + response + "'");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public String call(String message) throws IOException, InterruptedException {
		final String corrId = UUID.randomUUID().toString();

		String replyQueueName = channel.queueDeclare().getQueue();
		AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().correlationId(corrId).replyTo(replyQueueName)
				.build();

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
