package ru.aselit;

public enum TransferItemStateEnum {
	
	tisNew, tisUpload, tisDone, tisError;
	
	public static String toString(TransferItemStateEnum state) {
		
		switch (state) {
		case tisNew:	return "New";
		case tisUpload:	return "Upload";
		case tisDone:	return "Done";
		case tisError:	return "Error";
		default:		return "Uncknown";
		}
	}
}
