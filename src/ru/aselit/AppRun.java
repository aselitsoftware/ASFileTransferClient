package ru.aselit;

import ru.aselit.BaseAppRun;

public class AppRun extends BaseAppRun {

	public static void main(String[] args) {
		
		addJarToClasspath(getArchFileName("lib/swt"));
		
		try {	
			MainWindow window = new MainWindow();
			window.open();
		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}

}
