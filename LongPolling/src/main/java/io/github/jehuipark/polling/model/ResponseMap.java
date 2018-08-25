package io.github.jehuipark.polling.model;

public class ResponseMap<T> {

	private boolean isUpdate;
	private String message;
	private int status;
	private T data;
	public boolean isUpdate() {
		return isUpdate;
	}
	public void setUpdate(boolean isUpdate) {
		this.isUpdate = isUpdate;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
		this.message = Message.getMessage(status);
	}
	public T getData() {
		return data;
	}
	public void setData(T data) {
		this.data = data;
	}
	@Override
	public String toString() {
		String s = String.format("{isUpdate=%b, message=%s, status=%d, data=%s}",
				isUpdate, message, status, data);
		return s;
	}
	
	private static enum Message {
		 
		ALREADY_WORKING (-3, "ALREADY_WORKING"),
		DEAD (-2, "DEAD"),
		NO_DATA (-1, "NO_DATA"),
		UNDEFINED (0, "UNDEFINED"),
		DESTROY_COMMAND (1, "DESTROY_COMMAND"),
		SYSTEM_COMMAND (2, "SYSTEM_COMMAND"),
		TRANSACTION_COMMAND (3, "TRANSACTION_COMMAND"),
		REST (11, "REST"),
		WORKING (12, "WORKING");
		
		private int status;
		private String message;
		
		Message(int status, String message) {
			this.status = status;
			this.message = message;
		}
		
		int getStatus() {
			return status;
		}
		
		String getMessage() {
			return message;
		}
		
		static String getMessage(int status) {
			Message type = null;
			for(Message temp : Message.values()) {
				if(temp.getStatus() == status) {
					type = temp;
					break;
				}
			}
			return type.getMessage();
		}
	}
}
