package ru.aselit;

import ru.aselit.BaseAppRun;

public class AppRun extends BaseAppRun {

	public static void main(String[] args) {
		
		/*
		byte[] data;
		
		try {
			
			Socket socket = new Socket("localhost", 3128);
			
			JSONObject obj = new JSONObject();
			FileConnectCommandEnum command = FileConnectCommandEnum.fccAuthorize;
			obj.put("command", command.toInt());
			obj.put("login", "1");
			obj.put("password", "1");
			
			
			String json = obj.toString();
			
			data = new byte[Integer.BYTES + json.length()];
			data[0] = (byte) (json.length() & 0xFF);
			data[1] = (byte) (json.length() & 0xFF00);
			data[2] = (byte) (json.length() & 0xFF0000);
			data[3] = (byte) (json.length() & 0xFF000000);
			System.arraycopy(json.getBytes(), 0, data, Integer.BYTES, json.length());
			
			socket.getOutputStream().write(data);
			socket.close();
			
		} catch (UnknownHostException e) {
		
		} catch (IOException e) {
		
		}
		*/
		
		addJarToClasspath(getArchFileName("lib/swt"));
		
		try {	
			MainWindow window = new MainWindow();
			window.open();
		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}

}
