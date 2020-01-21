package analyser;

// 变量信息
public class VarInfo {

	public String type;
	public String val;
	
	public VarInfo (String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "[type=" + type + ", val=" + val + "]";
	}
}
