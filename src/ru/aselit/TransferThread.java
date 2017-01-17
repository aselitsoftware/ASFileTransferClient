package ru.aselit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TransferThread extends Thread {

//	interface for display thread info
	public interface TransferThreadInterface {

		public void showTransferList(TransferList list);
	}
	
	private TransferList transferList;
	private TransferThreadInterface transferThreadInterfaceImpl;
	private static final Logger log = LogManager.getLogger(TransferThread.class);
	
	/**
	 * Simple constructor without parameters.
	 */
	public TransferThread(TransferThreadInterface transferThreadInterfaceImpl) {
		
		transferList = new TransferList();
		
		this.transferThreadInterfaceImpl = transferThreadInterfaceImpl;
		
//		setDaemon(true);
		start();
	}
	
	/**
	 * Add file to the transfer list.
	 * @param sourceFile
	 * @param destinationFile
	 */
	public void addFile(String sourceFile, String destinationFile) {
		
		if (null == transferList.add(sourceFile, destinationFile))
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
				
				if (null != item)
					new TCPClientThread(item);
				
				if (transferList.removeIsDone())
					transferList.save();
				
				if (transferList.isUpdated()) {
					
					transferThreadInterfaceImpl.showTransferList(transferList);
					transferList.resetUpdate();
				}
				
				Thread.sleep(1000);
				
			} catch(InterruptedException e) {
				
				break;
			}
		}
		
		transferList.clear();
	}
}
