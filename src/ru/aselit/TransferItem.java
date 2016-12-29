package ru.aselit;

import static ru.aselit.TransferItemStateEnum.*;

public class TransferItem {

	private int index;
	private String sourceFile;
	private String destinationFile;
	private TransferItemStateEnum state = tisNew;
	private TCPClientThread thread = null;
	
	public TransferItem(int index, String sourceFile, String destinationFile) {
		
		this.index = index;
		this.sourceFile = sourceFile;
		this.destinationFile = destinationFile;
	}

	public String getSourceFile() {
		
		return sourceFile;
	}
	
	public String getDestinationFile() {
		
		return destinationFile;
	}
		
	public int getIndex() {
		
		return index;
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

	@Override
	public boolean equals(Object obj) {
		
		if (obj instanceof TransferItem) {
			
			TransferItem item = (TransferItem) obj;
			return (sourceFile.equals(item.getSourceFile()) && destinationFile.equals(item.getDestinationFile()));
		} else
			return false;
	}
}
