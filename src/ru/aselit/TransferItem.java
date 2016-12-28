package ru.aselit;

public class TransferItem {

	private int index;
	private String sourceFile;
	private String destinationFile;
	
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
	
	@Override
	public boolean equals(Object obj) {
		
		if (obj instanceof TransferItem) {
			
			TransferItem item = (TransferItem) obj;
			return (sourceFile.equals(item.getSourceFile()) && destinationFile.equals(item.getDestinationFile()));
		} else
			return false;
	}
}
