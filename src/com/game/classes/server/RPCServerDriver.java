package com.game.classes.server;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

import com.game.classes.Zone;
import com.game.helper.Util;
import com.game.models.ZoneModel;

public class RPCServerDriver {
	public static void main(String[] args) throws IOException, TimeoutException {	
		new Thread(new RPCServer()).start();
	}
	/*private static int[][] initMap() {
		int counter = 1;
		int[][] map = new int[4][4];
		 for (int row = 0; row < map.length; row++) {
		        for (int col = 0; col < map.length; col++) {
		            map[row][col] = counter;
		            counter++;
		        }
		    }
		 return map;
	}*/
}