package com.game.classes.server;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

import javax.swing.JFrame;

import com.game.classes.Zone;
import com.game.helper.Util;
import com.game.models.ZoneModel;
import com.game.ui.MapGidLayout;

public class RPCServerDriver {
	public static void main(String[] args) throws IOException, TimeoutException {
		// map = new MapGidLayout();
		new Thread(new RPCServer()).start();
	}
	
}