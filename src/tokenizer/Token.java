package tokenizer;

// 记录分词的结果
public class Token {

	private TokenType type;	// 类型
	private String value;	// 值
	
	public Token(TokenType type, String value) {
		super();
		this.type = type;
		this.value = value;
	}

	public TokenType getType() {
		return type;
	}
	
	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return String.format("%s:%s", value, type);
	}
}
