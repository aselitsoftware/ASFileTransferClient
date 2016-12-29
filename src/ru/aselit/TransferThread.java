package ru.aselit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ru.aselit.TCPClientThread.TCPClientThreadInterface;

public class TransferThread extends Thread {

//	interface for display thread info
	public interface TransferThreadInterface {

		public void showTransferList(TransferList list);
	}
	
	private TransferList transferList;
	private TransferThreadInterface impl1;
	private TCPClientThreadInterface impl2;
	private static final Logger log = LogManager.getLogger(TransferThread.class);
	
	/**
	 * Simple constructor without parameters.
	 */
	public TransferThread(TransferThreadInterface impl1, TCPClientThreadInterface impl2) {
		
		transferList = new TransferList();
		
		this.impl1 = impl1;
		this.impl2 = impl2;
		
//		setDaemon(true);
		start();
	}
	
	/**
	 * Add file to the transfer list.
	 * @param sourceFile
	 * @param destinationFile
	 */
	public void addFile(String sourceFile, String destinationFile) {
		
		if (null == transferList.add(sourceFile, destinationFile, true))
			return;
		transferList.save();
	}


	@Override
	public void run() {
		
		while (!Thread.interrupted()) {
			
			try {
				
//				update list of files
				transferList.load();
				
//				select a file from list for upload
				TransferItem item = transferList.select();
				
				if (null != item) {
					
					new TCPClientThread(impl2, item);
				}
				
				if (transferList.isUpdated()) {
					
					impl1.showTransferList(transferList);
					transferList.resetUpdate();
				}
				
				Thread.sleep(1000);
				
			} catch(InterruptedException e) {
				
				break;
			}
		};
		
		transferList.clear();
	}
}
