package error;

// ´íÎóĞÅÏ¢Àà
public class Error {

	String message;
	
	public Error(String error) {
		this.message = error;
	}

	@Override
	public String toString() {
		return message;
	}
}
