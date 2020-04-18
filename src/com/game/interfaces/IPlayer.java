package com.game.interfaces;

import com.rabbitmq.client.ConnectionFactory;

public interface IPlayer {
	ConnectionFactory connectToServer(ConnectionFactory factory);
}
