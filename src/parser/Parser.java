package parser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import analyser.Node;
import error.Error;
import tokenizer.Token;
import tokenizer.TokenType;
import tokenizer.Tokenizer;

// ll(1) 语法分析 生成语法树
public class Parser {

	private Map<String, Set<Producer>> producerMap;
	private Set<String> terminals;
	private Set<String> nonterminals;
	private List<Token> tokens;
	private Tokenizer tokenizer;
	private int index;
	private List<Producer> producers;
	private List<Error> errors;
	
	private Node root;
	private Node curNode;

	public Parser(File file) {
		// 加载语法
		Grammar grammar = new Grammar(file);
		producerMap = grammar.getProducerMap();
		terminals = grammar.getTerminals();
		nonterminals = grammar.getNonterminals();
	}

	// 语法分析
	public void parse(Tokenizer tokenizer) {
		this.tokenizer = tokenizer;
		this.tokens = tokenizer.getTokens();
		producers = new ArrayList<>();
		index = 0;
		root = new Node("S", null);
		curNode = root;
		errors = new ArrayList<>();
		
		parse("S");
	}

	private boolean parse(String state) {
		Token token = tokens.get(index);
		TokenType tokenType = token.getType();
		String str = token.getValue();
		if (!tokenType.equals(TokenType.KEYWORD) && !tokenType.equals(TokenType.SYMBOL))
			str = tokenType.toString();

		Set<Producer> set = producerMap.get(state);
		Producer producer = null;
		for (Producer p : set) {
			if (p.getSelect().contains(str)) {
				producer = p;
				break;
			}
		}

		if (producer == null) {
			errors.add(new Error("line " + tokenizer.getLineNumber(index) + " : " + token.getValue() + " 不符合语法规范"));
			return false;
		}

		producers.add(producer);
		
		List<String> right = producer.getRight();
		for (int i = 0; i < right.size(); i++) {
			Token t = tokens.get(index);
			TokenType type = t.getType();
			String s = t.getValue();
			if (!type.equals(TokenType.KEYWORD) && !type.equals(TokenType.SYMBOL))
				s = type.toString();

			String cur = right.get(i);
			
			Node son = new Node(cur, curNode);
			curNode.setSon(son);
			
			if (terminals.contains(cur)) {
				// 终极符判断是否相等
				if (cur.equals("EMPTY")) {
					continue;
				} else if (s.equals(cur)) {
					son.setAttribute("val", t.getValue());
					if (type.equals(TokenType.TYPE)) {
						son.setAttribute("type", t.getValue());
					} else if (type.equals(TokenType.INTEGER)) {
						son.setAttribute("type", "int");
					} else if (type.equals(TokenType.FLOAT)) {
						son.setAttribute("type", "float");
					} else if (type.equals(TokenType.CHAR)) {
						son.setAttribute("type", "char");
					} else if (type.equals(TokenType.STRING)) {
						son.setAttribute("type", "string");
					}
					index++;
					continue;
				} else {
					errors.add(new Error("line " + tokenizer.getLineNumber(index) + " : " + t.getValue() + " 应改为 " + cur));
					return false;
				}
			} else if (nonterminals.contains(cur)) {
				// 非终极符递归
				curNode = son;
				boolean flag = parse(cur);
				if (flag) {
					if (cur.equals("op2") || cur.equals("op1")) {
						curNode.setAttribute("val", curNode.getSon(0).getAttribute("val"));
					}
					curNode = curNode.getFather();
					continue;
				} else {
					return false;
				}
			} else if (cur.equals("END")) {
				return true;
			}
		}

		return true;
	}

	public Node getRoot() {
		return root;
	}

	public List<Producer> getProducers() {
		return producers;
	}

	public List<Error> getErrors() {
		return errors;
	}
}
