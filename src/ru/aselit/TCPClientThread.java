package ru.aselit;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import org.json.simple.JSONObject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static ru.aselit.FileConnectCommandEnum.*;

public class TCPClientThread extends Thread {

//	interface for display thread info
	public interface TCPClientThreadInterface {

		public void showProgress(int itemIndex, int progress);
	}
	
	private TCPClientThreadInterface impl;
	private TransferItem item;
	private int progress = 0;
	private Socket socket = null;
	
	private static final Logger log = LogManager.getLogger(TCPClientThread.class);
	
	
	public TCPClientThread(TCPClientThreadInterface impl, TransferItem item) {
		
		this.impl = impl;
		this.item = item;
		item.setThread(this);
		
		start();
	}
	
	private boolean isConnected() {
		
		return ((null != socket) && socket.isConnected());
	}
	
	/**
	 * Send block of data to the server. 
	 * @param block
	 */
	private void sendBlock(String block) {
		
		if (!isConnected())
			return;
		
		byte[] data = new byte[Integer.BYTES + block.length()];
		data[0] = (byte) (block.length() & 0xFF);
		data[1] = (byte) (block.length() & 0xFF00);
		data[2] = (byte) (block.length() & 0xFF0000);
		data[3] = (byte) (block.length() & 0xFF000000);
		System.arraycopy(block.getBytes(), 0, data, Integer.BYTES, block.length());
		
		try {
			
			socket.getOutputStream().write(data);
		
		} catch (IOException ex) {
			
			if (log.isDebugEnabled())
				log.debug(ex);
		}
	}

	@Override
	public void run() {
		
		while (!Thread.interrupted()) {
			
			try {
				
				try {
					
//					try to make connection to server
					if (null == socket)
						socket = new Socket("localhost", 3128);
					
					JSONObject obj = new JSONObject();
					FileConnectCommandEnum command = fccAuthorize;
					obj.put("command", command.toInt());
					obj.put("login", "1");
					obj.put("password", "1");
					sendBlock(obj.toString());
										
				} catch (UnknownHostException ex) {
					
					if (log.isDebugEnabled())
						log.debug(ex);
					
				} catch (IOException ex) {
					
					if (log.isDebugEnabled())
						log.debug(ex);
				}
				
				Thread.sleep(300);
				
			} catch(InterruptedException ex) {
				
				log.error("Interrupted exception.");
				break;
			}
		};
		
		if (log.isDebugEnabled())
			log.debug("TCP thread was interrupted.");
		
		try {
			
			if (null != socket)
				socket.close();
		} catch (IOException ex) {
		
			if (log.isDebugEnabled())
				log.debug(ex);
		}
	}
}
