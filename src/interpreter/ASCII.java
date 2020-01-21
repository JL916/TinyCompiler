package interpreter;

import java.util.HashMap;
import java.util.Map;

// 含有转义字符的 ASCII 码表
public class ASCII {

	static Map<String, Integer> map;
	
	public ASCII () {
		map = new HashMap<>();
		map.put("\\n", 10);
		map.put("\\r", 13);
		map.put("\\t", 9);
		map.put("\\b", 8);
		map.put("\\f", 12);
		map.put("\\'", 39);
		map.put("\"", 34);
		map.put("\\", 92);
	}
}
