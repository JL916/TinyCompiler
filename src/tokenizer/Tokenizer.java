package tokenizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import error.Error;

// 词法分析器
public class Tokenizer {

	private static final String[] TYPE = { "int", "char", "float", "void" };
	private static Set<String> typeset;
	private static final String[] KEYWORD = { "if", "while", "for", "do", "else", "return" };
	private static Set<String> keywordset;
	private static final String[] SYMBOL = { "{", "}", "(", ")", ",", ";", "+", "-", "*", "/", "%", "\"", "'", "&", "|",
			"^", "<", ">", "=", "<=", ">=", "!=", "==", "&&", "||", "~", "!", "++", "--" };
	private static Set<String> symbolset;

	private String text;
	private List<Token> tokens;
	private List<Integer> lineNumber;
	private List<Error> errors;

	public Tokenizer() {
		// 初始化各个集合
		typeset = new HashSet<>(Arrays.asList(TYPE));
		keywordset = new HashSet<>(Arrays.asList(KEYWORD));
		symbolset = new HashSet<>(Arrays.asList(SYMBOL));
	}

	// 词法分析
	public void analysis(String text) {
		tokens = new ArrayList<>();
		lineNumber = new ArrayList<>();
		errors = new ArrayList<>();
		// 去注释
		text = text.replaceAll("//[\\s\\S]*?\\n", "");
		text = text.replaceAll("/\\*(.|\\n)*\\*/", "");

		this.text = text + " ";
		int num = text.length();
		int index = 0; // 记录当前下标

		while (index < num) {
			char c = text.charAt(index);
			if (c == '\n') {
				// 记录行号
				lineNumber.add(tokens.size());
			}
			if (Character.isWhitespace(c)) { // 跳过空白符
				index++;
			} else if (Character.isUpperCase(c) || Character.isLowerCase(c)) { // 开头为字母
				index = handleAlpha(index);
			} else if (Character.isDigit(c)) { // 开头为数字
				index = handleDigit(index);
			} else if (symbolset.contains(c + "")) { // 符号
				if (c == '\"') {
					int i = index + 1;
					while (i < num && text.charAt(i) != '\"')
						i++;
					tokens.add(new Token(TokenType.SYMBOL, "\""));
					if (index + 1 <= i) {
						tokens.add(new Token(TokenType.STRING, text.substring(index + 1, i)));
					}
					if (i < num && text.charAt(i) == '\"') {
						tokens.add(new Token(TokenType.SYMBOL, "\""));
					} else {
						errors.add(new Error("line " + getLineNumber(index) + " : " + " 缺少 \" "));
					}
					index = i + 1;
				} else if (c == '\'') {
					index++;
					tokens.add(new Token(TokenType.SYMBOL, "\'"));
					if (index < num && text.charAt(index) == '\\') {
						tokens.add(new Token(TokenType.CHAR, text.substring(index, index + 2)));
						index += 2;
					} else if (index < num) {
						tokens.add(new Token(TokenType.CHAR, text.charAt(index) + ""));
						index += 1;
					}
					if (index < num && text.charAt(index) == '\'') {
						tokens.add(new Token(TokenType.SYMBOL, "\'"));
						index++;
					} else {
						errors.add(new Error("line " + getLineNumber(index) + " : " + " 缺少 ' "));
					}
				} else if (index + 1 < num && symbolset.contains(text.substring(index, index + 2))) {
					tokens.add(new Token(TokenType.SYMBOL, text.substring(index, index + 2)));
					index += 2;
				} else {
					tokens.add(new Token(TokenType.SYMBOL, c + ""));
					index++;
				}
			} else {
				errors.add(new Error("line " + getLineNumber(index) + " : " + "无法识别 " + c));
				index++;
			}
		}

		// 添加终止符
		tokens.add(new Token(TokenType.END, "END"));
	}

	// 开头为字母
	private int handleAlpha(int index) {
		int i = index;
		char c;
		do {
			i++;
			c = text.charAt(i);
		} while (Character.isUpperCase(c) || Character.isLowerCase(c) || Character.isDigit(c));

		String s = text.substring(index, i);

		// 判断 类型名 关键字 标识符
		if (typeset.contains(s)) {
			tokens.add(new Token(TokenType.TYPE, s));
		} else if (keywordset.contains(s)) {
			tokens.add(new Token(TokenType.KEYWORD, s));
		} else {
			tokens.add(new Token(TokenType.IDENTIFIER, s));
		}

		return i;
	}

	// 开头为数字
	private int handleDigit(int index) {
		int i = index;
		char c;
		do {
			i++;
			c = text.charAt(i);
		} while (Character.isDigit(c));

		// 判断 小数 整数
		if (c == '.') {
			do {
				i++;
				c = text.charAt(i);
			} while (Character.isDigit(c));
			String s = text.substring(index, i);
			if (s.charAt(s.length()-1) == '.') {
				errors.add(new Error("line " + getLineNumber(i) + " : " + "数字格式错误"));
			} else {
				tokens.add(new Token(TokenType.FLOAT, s));
			}
		} else {
			String s = text.substring(index, i);
			tokens.add(new Token(TokenType.INTEGER, s));
		}

		return i;
	}

	// 返回行号
	public int getLineNumber(int index) {
		for (int i = 0; i < lineNumber.size(); i++) {
			if (lineNumber.get(i) > index)
				return i + 1;
		}
		return lineNumber.size() + 1;
	}

	public List<Token> getTokens() {
		return tokens;
	}

	public List<Error> getErrors() {
		return errors;
	}
}
