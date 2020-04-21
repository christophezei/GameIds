package com.game.classes.server;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RPCServerDriver {

	public static void main(String[] args) throws IOException, TimeoutException {
		new Thread(new RPCServer()).start();
	}
}
