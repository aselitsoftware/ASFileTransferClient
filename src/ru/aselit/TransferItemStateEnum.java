package ru.aselit;

public enum TransferItemStateEnum {
	
	tisNew, tisUpload, tisError;
	
	public static String asString(TransferItemStateEnum state) {
		
		switch (state) {
		case tisNew:
			return "New";
		case tisUpload:
			return "Upload";
		case tisError:
			return "Error";
		default:
			return "Uncknown";
		}
	}
}
