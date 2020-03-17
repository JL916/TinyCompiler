package analyser;

// 四元式
public class FourItem {

	public String op;
	public String arg1;
	public String arg2;
	public String result;

	public FourItem(String op, String arg1, String arg2, String result) {
		super();
		this.op = op;
		this.arg1 = arg1;
		this.arg2 = arg2;
		this.result = result;
	}

	@Override
	public String toString() {
		return "(" + op + ", " + arg1 + ", " + arg2 + ", " + result + ")";
	}
}
