package ru.aselit;

import static ru.aselit.TransferItemStateEnum.*;
import static ru.aselit.FileTransferCommandEnum.*;

public class TransferItem extends UpdateState {

	private long id;
	private String sourceFile;
	private String destinationFile;
	private TransferItemStateEnum state = tisNew;
	private TCPClientThread thread = null;
	
	public TransferItem(long id, String sourceFile, String destinationFile) {
		
		super(true);
		this.id = id;
		this.sourceFile = sourceFile;
		this.destinationFile = destinationFile;
	}

	public String getSourceFile() {
		
		return sourceFile;
	}
	
	public String getDestinationFile() {
		
		return destinationFile;
	}
		
	public long getId() {
		
		return id;
	}
	
	public TransferItemStateEnum getState() {
		
		return state;
	}

	public void setState(TransferItemStateEnum state) {
		
		this.state = state;
	}
	
	public void interruptThread() {
		
		if (null == thread)
			return;
		thread.interrupt();
	}

	public void setThread(TCPClientThread thread) {
		
		this.thread = thread;
	}
	
	/**
	 * 
	 * @return
	 */
	public FileTransferCommandEnum getThreadState() {
		
		return ((null == thread) ? ftcNone : thread.getMyState());
	}

	@Override
	public boolean equals(Object obj) {
		
		if (obj instanceof TransferItem) {
			
			TransferItem item = (TransferItem) obj;
			return (sourceFile.equals(item.getSourceFile()) && destinationFile.equals(item.getDestinationFile()));
		} else
			return false;
	}
}
