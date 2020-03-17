package error;

// 错误类
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
