package ru.aselit;

public class TransferThread extends Thread {

//	interface for display thread state, error and result
	public interface TransferThreadInterface {

		public void showTransferList(TransferList list);
	}
	
	private TransferList transferList;
	private TransferThreadInterface impl;
	
	/**
	 * Simple constructor without parameters.
	 */
	public TransferThread(TransferThreadInterface impl) {
		
		transferList = new TransferList();
		
		this.impl = impl;
		
		setDaemon(true);
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
				if (transferList.isUpdated()) {
					
					impl.showTransferList(transferList);
					transferList.resetUpdate();
				}
				
				Thread.sleep(1000);
				
			} catch(InterruptedException e) {
				
				break;
			}
		};
	}
}
