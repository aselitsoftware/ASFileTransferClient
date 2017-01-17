package ru.aselit;

public class UpdateState {

	private boolean updated;
	
	public UpdateState(boolean updated) {
		
		this.updated = updated;
	}
	
	public void setUpdated() {
		
		updated = true;
	}
	
	/**
	 * 
	 * @return true or false
	 */
	public boolean isUpdated() {
		
		return updated;
	}
	
	public void resetUpdate() {
		
		updated = false;
	}
}
