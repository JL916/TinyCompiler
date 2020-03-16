package parser;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

// 产生式类
public class Producer {
	
	// S -> func funcs
	private String left;		// S
	private List<String> right; // func funcs
	private Set<String> select; // 产生式的 select 集
	
	public Producer(String left, List<String> right) {
		super();
		this.left = left;
		this.right = right;
		select = new HashSet<>();
	}

	public String getLeft() {
		return left;
	}

	public List<String> getRight() {
		return right;
	}

	public Set<String> getSelect() {
		return select;
	}

	public void setSelect(Set<String> select) {
		this.select = select;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(left + " -> ");
		for (String s : right) {
			sb.append(s + " ");
		}
		return sb.toString();
	}
}
