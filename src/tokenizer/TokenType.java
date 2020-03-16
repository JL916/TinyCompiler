package tokenizer;

// 类型
public enum TokenType {
	
	END,
	KEYWORD,
	TYPE,
	SYMBOL,
	INTEGER,
	FLOAT,
	CHAR,
	STRING,
	IDENTIFIER;
	
	public String print() {
		switch(this) {
		case END : return "终止符";
		case KEYWORD : return "关键字";
		case TYPE : return "类型名";
		case SYMBOL : return "符号";
		case INTEGER : return "整数";
		case FLOAT : return "浮点数";
		case CHAR : return "字符";
		case STRING : return "字符串";
		case IDENTIFIER : return "标识符";
		default : return "";
		}
	}
}
