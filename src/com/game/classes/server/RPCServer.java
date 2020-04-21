package com.game.classes.server;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.*;

public class RPCServer implements Runnable {
	private static String uri = "amqp://rdraipzf:RX4SqBs7Zrgr9_jPmJLurG-i6znoF3ow@kangaroo.rmq.cloudamqp.com/rdraipzf";
	private ConnectionFactory factory;
	private static final String RPC_QUEUE_NAME = "rpc_queue";
	private Connection connection;
	private Channel channel;

	private static String helloRPC() {
		return "Hello RPC server ";
	}

	public RPCServer() throws IOException, TimeoutException {
		factory = this.connectToServer(factory);
		connection = factory.newConnection();
		channel = connection.createChannel();
	}

	@Override
	public void run() {
		try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
			channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);
			channel.queuePurge(RPC_QUEUE_NAME);

			channel.basicQos(1);

			System.out.println(" [x] Awaiting RPC requests");

			Object monitor = new Object();
			DeliverCallback deliverCallback = (consumerTag, delivery) -> {
				AMQP.BasicProperties replyProps = new AMQP.BasicProperties.Builder()
						.correlationId(delivery.getProperties().getCorrelationId()).build();

				String response = "";

				try {
					String message = new String(delivery.getBody(), "UTF-8");
					System.out.println(" [.] fib(" + message + ")");
					response = helloRPC();
				} catch (RuntimeException e) {
					System.out.println(" [.] " + e.toString());
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
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (TimeoutException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	}

	private ConnectionFactory connectToServer(ConnectionFactory factory) {
		factory = new ConnectionFactory();
		try {
			factory.setUri(uri);
		} catch (KeyManagementException | NoSuchAlgorithmException | URISyntaxException e) {
			e.printStackTrace();
		}
		return factory;
	}
}
