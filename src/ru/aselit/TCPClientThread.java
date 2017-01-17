package ru.aselit;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.io.InputStream;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static ru.aselit.FileTransferCommandEnum.*;
import static ru.aselit.TransferItemStateEnum.*;

public class TCPClientThread extends Thread {
	
	private TransferItem item;
	private int progress = 0;
	private Socket socket = null;
	private FileTransferCommandEnum state = ftcNone;
	private FileTransferCommandEnum command = ftcNone;
	private FileTransferInputBuffer buffer = new FileTransferInputBuffer();
	
	private static final Logger log = LogManager.getLogger(TCPClientThread.class);
	
	
	public TCPClientThread(TransferItem item) {
		
		this.item = item;
		item.setThread(this);
		
		start();
	}
	
	private void setState(FileTransferCommandEnum state) {
		
		this.state = state;
		if (null != item)
			item.setUpdated();
	}
	
	/**
	 * 
	 * @return
	 */
	private boolean isConnected() {
		
		return ((null != socket) && socket.isConnected());
	}
	
	public FileTransferCommandEnum getMyState() {
		
		return state;
	}
	
	/**
	 * Check the command code for compliance.
	 * @param block
	 */
	private boolean isValidResponse(byte []block) {
		
		JSONParser parser = new JSONParser();
		try {
			
			JSONObject obj = (JSONObject) parser.parse(new String(block));
			Long value = new Long((long) obj.get("command"));
			return FileTransferCommandEnum.fromInt(value.intValue()).equals(command);
		} catch (ParseException ex) {
			
			if (log.isDebugEnabled())
				log.debug(ex);
			return false;
			
		} 
	}
	
	/**
	 * 
	 * @param block
	 * @return
	 */
	private boolean isFileUploaded(byte []block) {
		
		JSONParser parser = new JSONParser();
		try {
			
			JSONObject obj = (JSONObject) parser.parse(new String(block));
			boolean fileExists = (boolean) obj.get("fileExists");
			if (fileExists) {
				
				long srvFileSize = (long) obj.get("fileSize");
				String srvMD5 = (String) obj.get("fileMD5");
				
				File file = new File(item.getSourceFile());
				if (null != file) {
					
					if (file.length() == srvFileSize) {
						
						String locMD5 = MD5.getFileHash(file);
						return ((file.length() == 0) || srvMD5.equalsIgnoreCase(locMD5));
					}
				}
			}
			return false;
			
		} catch (ParseException ex) {
			
			if (log.isDebugEnabled())
				log.debug(ex);
			return false;
			
		} 
	}
	
	
	@Override
	public void run() {
		
		JSONObject obj;
		byte inBuf[] = new byte[1024];
		
		while (!Thread.interrupted()) {
			
			try {
				
				try {
					
//					try to make connection to server
					if (null == socket) {
						
						socket = new Socket("localhost", 3128);
						setState(ftcAuthorize);
					}

					switch (state) {
					
					case ftcAuthorize:
						
						command = ftcAuthorize;
						
						obj = new JSONObject();
						obj.put("command", command.toInt());
						obj.put("login", "1");
						obj.put("password", "1");
						FileTransferOutputBuffer.sendBlock(socket, obj.toString());
						
						setState(ftcWaitResponse);
						break;
						
					case ftcFileInfo:
						
						command = ftcFileInfo;
						
						obj = new JSONObject();
						obj.put("command", command.toInt());
						obj.put("fileName", item.getDestinationFile());
						FileTransferOutputBuffer.sendBlock(socket, obj.toString());
						
						setState(ftcWaitResponse);
						break;
						
					case ftcWaitResponse:
						
						int size = socket.getInputStream().read(inBuf);
						buffer.write(inBuf, size);
						byte[] block = buffer.readBlock();
						if (isValidResponse(block)) {
							
							switch (command) {
							case ftcAuthorize:
								
								setState(ftcFileInfo);
								break;
							case ftcFileInfo:
								
								if (!isFileUploaded(block))
									setState(ftcUploadStart);
								else {
									
									item.setState(tisDone);
									setState(ftcNone);
								}
								break;
							}
						} else
							throw new Exception("Wrong response.");
						break;
					}
					
					
				} catch (UnknownHostException ex) {
					
					if (log.isDebugEnabled())
						log.debug(ex);
					
				} catch (IOException ex) {
					
					if (log.isDebugEnabled())
						log.debug(ex);
					
				} catch (Exception ex) {
					
					log.error(ex);
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
