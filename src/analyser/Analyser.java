package analyser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import error.Error;

// 语义分析
public class Analyser {

	private Map<String, FuncInfo> funcsInfoMap; // 函数信息表
	private Map<String, Set<String>> typeMap; 	// 类型转换表
	private List<Error> errors;
	private Node root;

	public Analyser() {
		// 初始化类型转换表
		typeMap = new HashMap<>();
		Set<String> set = new HashSet<String>();
		set.add("char");
		set.add("int");
		typeMap.put("char", new HashSet<>(set));
		typeMap.put("int", new HashSet<>(set));
		set.add("float");
		typeMap.put("float", new HashSet<>(set));
		set.add("string");
		typeMap.put("string", new HashSet<>(set));
	}

	public void analyse(Node root) {
		// 语义分析
		this.root = root;
		funcsInfoMap = new HashMap<>();
		
		FuncInfo printInfo = new FuncInfo("void");
		printInfo.vars.add("arg");
		printInfo.args.put("arg", new VarInfo("string"));
		printInfo.arglist.add("arg");
		funcsInfoMap.put("print", printInfo);
		
		FuncInfo scanInfo = new FuncInfo("void");
		scanInfo.vars.add("arg");
		scanInfo.args.put("arg", new VarInfo("float"));
		scanInfo.arglist.add("arg");
		funcsInfoMap.put("scan", scanInfo);
		
		errors = new ArrayList<>();
		setFuncsInfoMap(root); // 设置函数信息
		checkType(); // 检查类型
		checkReturn(); // 检查 return 语句
	}

	private void setFuncsInfoMap(Node root) {
		if (root.getSons().size() == 1)
			return;
		Node func = root.getSon(0);
		Node funcs = root.getSon(1);
		setFuncInfo(func);
		setFuncsInfoMap(funcs);
	}

	private void setFuncInfo(Node func) {
		String type = func.getSon(0).getAttribute("type");
		String id = func.getSon(1).getAttribute("val");
		Node arglist = func.getSon(3);
		Node definelist = func.getSon(6);
		FuncInfo info = new FuncInfo(type);
		
		if (funcsInfoMap.containsKey(id)) {
			errors.add(new Error("函数 " + id + " 重复定义"));
		} else {
			funcsInfoMap.put(id, info);
			handleArgList(arglist, id, info);
			handleDefinelist(definelist, id, info);
		}
	}

	private void handleArgList(Node arglist, String id, FuncInfo info) {
		if (arglist.getSons().size() == 1)
			return;
		String type = arglist.getSon(0).getAttribute("type");
		String name = arglist.getSon(1).getAttribute("val");
		Node args = arglist.getSon(2);

		if (info.args.containsKey(name) || info.locals.containsKey(name)) {
			errors.add(new Error("函数 " + id + " 参数名 " + name + " 重复"));
		} else {
			info.arglist.add(name);
			info.args.put(name, new VarInfo(type));
			info.vars.add(name);
		}

		if (type.equals("void")) {
			errors.add(new Error("函数 " + id + " 参数 " + name + " 类型不能为 void"));
		}

		handleArgs(args, id, info);
	}

	private void handleArgs(Node args, String id, FuncInfo info) {
		if (args.getSons().size() == 1)
			return;
		String type = args.getSon(1).getAttribute("type");
		String name = args.getSon(2).getAttribute("val");
		Node args2 = args.getSon(3);

		if (info.args.containsKey(name) || info.locals.containsKey(name)) {
			errors.add(new Error("函数 " + id + " 参数名 " + name + " 重复"));
		} else {
			info.arglist.add(name);
			info.args.put(name, new VarInfo(type));
			info.vars.add(name);
		}

		if (type.equals("void")) {
			errors.add(new Error("函数 " + id + " 参数 " + name + " 类型不能为 void"));
		}

		handleArgs(args2, id, info);
	}

	private void handleDefinelist(Node definelist, String id, FuncInfo info) {
		if (definelist.getSons().size() == 1)
			return;
		Node definestatement = definelist.getSon(0);
		Node definelist2 = definelist.getSon(1);

		handleDefinestatement(definestatement, id, info);
		handleDefinelist(definelist2, id, info);
	}

	private void handleDefinestatement(Node definestatement, String id, FuncInfo info) {
		String type = definestatement.getSon(0).getAttribute("type");
		String name = definestatement.getSon(1).getAttribute("val");
		Node other = definestatement.getSon(3);

		if (info.args.containsKey(name) || info.locals.containsKey(name)) {
			errors.add(new Error("函数 " + id + " 本地变量名 " + name + " 重复"));
		} else {
			info.locals.put(name, new VarInfo(type));
			info.vars.add(name);
		}

		if (type.equals("void")) {
			errors.add(new Error("函数 " + id + " 本地变量 " + name + " 类型不能为 void"));
		}

		handleOther(other, id, info, type);
	}

	private void handleOther(analyser.Node other, String id, FuncInfo info, String type) {
		if (other.getSons().size() == 1)
			return;
		String name = other.getSon(1).getAttribute("val");
		Node other2 = other.getSon(3);

		if (info.args.containsKey(name) || info.locals.containsKey(name)) {
			errors.add(new Error("函数 " + id + " 本地变量名 " + name + " 重复"));
		} else {
			info.locals.put(name, new VarInfo(type));
			info.vars.add(name);
		}

		other.getSon(1).setAttribute("type", type);
		handleOther(other2, id, info, type);
	}

	private void checkType() {
		Queue<Node> queue = new LinkedList<>();
		queue.add(root);
		while (!queue.isEmpty()) {
			Node node = queue.poll();
			if (node.getName().equals("expression")) {
				if (node.getAttribute("checked") == null)
					setType(node);
			}
			List<Node> nodes = node.getSons();
			for (Node n : nodes)
				queue.add(n);
		}

		queue = new LinkedList<>();
		queue.add(root);
		while (!queue.isEmpty()) {
			Node node = queue.poll();
			if (node.getName().equals("definestatement")) {
				String typel = node.getSon(0).getAttribute("type");
				String id = node.getSon(1).getAttribute("val");
				Node init = node.getSon(2);
				if (init.getSons().size() == 1)
					break;
				String typer = init.getSon(1).getAttribute("type");
				if (typeMap.get(typel) == null) {
					errors.add(new Error(id + " 无法确定类型"));
				} else if (!typeMap.get(typel).contains(typer)) {
					errors.add(new Error(id + "无法进行类型转换"));
				}
			}
			List<Node> nodes = node.getSons();
			for (Node n : nodes)
				queue.add(n);
		}

	}

	private void setType(Node root) {
		String name = root.getName();
		if (name.equals("expression")) {
			Node item = root.getSon(0);
			Node expr = root.getSon(1);
			setType(item);
			setType(expr);
			if (expr.getAttribute("type") == null) {
				root.setAttribute("type", item.getAttribute("type"));
			} else {
				String typel = item.getAttribute("type");
				String typer = expr.getAttribute("type");
				String op2 = expr.getSon(0).getAttribute("val");
				if (op2.equals("=")) {
					if (typeMap.get(typel) == null) {
						errors.add(new Error("无法确定类型"));
					} else if (!typeMap.get(typel).contains(typer)) {
						errors.add(new Error("无法进行类型转换"));
					} else {
						root.setAttribute("type", typel);
					}
				} else {
					if (typeMap.get(typel).contains(typer)) {
						root.setAttribute("type", typel);
					} else if (typeMap.get(typer).contains(typel)) {
						root.setAttribute("type", typer);
					}
				}
			}
			root.setAttribute("checked", "");
		} else if (name.equals("expr")) {
			Node node = root.getSon(0);
			if (node.getName().equals("EMPTY")) {
				return;
			} else if (node.getName().equals("op2")) {
				Node expression = root.getSon(1);
				setType(expression);
				root.setAttribute("type", expression.getAttribute("type"));
			}
		} else if (name.equals("item")) {
			Node node = root.getSon(0);
			if (node.getName().equals("IDENTIFIER")) {
				if (root.getSon(1).getSons().size() == 1) {
					Node t = node.getFather();
					while (!t.getName().equals("func")) {
						t = t.getFather();
					}
					String funcName = t.getSon(1).getAttribute("val");
					FuncInfo info = funcsInfoMap.get(funcName);
					String type = null;
					String id = node.getAttribute("val");
					if (info.args.containsKey(id)) {
						type = info.args.get(id).type;
					} else if (info.locals.containsKey(id)) {
						type = info.locals.get(id).type;
					} else {
						errors.add(new Error("函数 " + funcName + " 中 " + id + " 未定义"));
						return;
					}
					root.setAttribute("type", type);
				} else {
					String id = node.getAttribute("val");
					FuncInfo info = funcsInfoMap.get(id);
					if (info == null) {
						errors.add(new Error("函数 " + id + " 未定义"));
						return ;
					} else {
						root.setAttribute("type", funcsInfoMap.get(id).returnType);
					}
					
					Node parameters = root.getSon(1).getSon(1);
					List<String> typeList = new ArrayList<>();
					List<String> arglist = info.arglist;
					if (parameters.getSons().size() > 1) {
						Node expression = parameters.getSon(0);
						setType(expression);
						String type = expression.getAttribute("type");
						typeList.add(type);
						setParamType(typeList, parameters.getSon(1));
						if (arglist.size() != typeList.size()) {
							errors.add(new Error("函数 " + id + " 参数个数不匹配"));
						} else {
							for (int i = 0; i < arglist.size(); i++) {
								Set<String> types = typeMap.get(info.getVarInfo(arglist.get(i)).type);
								if (!types.contains(typeList.get(i))) {
									errors.add(new Error("函数 " + id + " 参数类型不匹配"));
								}
							}
						}
					} else {
						if (arglist.size() > 0)
							errors.add(new Error("函数 " + id + " 参数个数不匹配"));
					}
				}
			} else if (node.getName().equals("INTEGER")) {
				root.setAttribute("type", "int");
			} else if (node.getName().equals("FLOAT")) {
				root.setAttribute("type", "float");
			} else if (node.getName().equals("'")) {
				root.setAttribute("type", "char");
			} else if (node.getName().equals("\"")) {
				root.setAttribute("type", "string");
			} else if (node.getName().equals("(")) {
				Node expression = root.getSon(1);
				setType(expression);
				root.setAttribute("type", expression.getAttribute("type"));
			} else if (node.getName().equals("op1")) {
				String op1 = root.getSon(0).getAttribute("val");
				Node item = root.getSon(1);
				setType(item);

				if (op1.equals("++") || op1.equals("--")) {
					if (!item.getAttribute("type").equals("int") && !item.getAttribute("type").equals("char")) {
						errors.add(new Error(item.getAttribute("type") + " 不能进行自增或自减运算"));
					}
				}
				root.setAttribute("type", item.getAttribute("type"));
			}
		}
	}

	private void setParamType(List<String> typeList, Node param) {
		if (param.getSons().size() == 1)
			return ;
		Node expression = param.getSon(1);
		setType(expression);
		String type = expression.getAttribute("type");
		typeList.add(type);
		setParamType(typeList, param.getSon(2));
	}

	private void checkReturn() {
		Queue<Node> queue = new LinkedList<>();
		queue.add(root);
		while (!queue.isEmpty()) {
			Node node = queue.poll();
			if (node.getName().equals("func")) {
				if (node.getAttribute("checked") == null)
					checkReturn(node);
			}
			List<Node> nodes = node.getSons();
			for (Node n : nodes)
				queue.add(n);
		}
	}

	private void checkReturn(Node func) {
		String name = func.getSon(1).getAttribute("val");
		String type = func.getSon(0).getAttribute("type");
		Node statements = func.getSon(7);
		Set<Node> set = new HashSet<Node>();

		Queue<Node> queue = new LinkedList<Node>();
		queue.add(statements);
		while (!queue.isEmpty()) {
			Node node = queue.poll();
			if (node.getName().equals("returnstatement"))
				set.add(node);
			for (Node n : node.getSons())
				queue.add(n);
		}

		for (Node node : set) {
			Node ret = node.getSon(1);
			Node son = ret.getSon(0);
			if (son.getName().equals("EMPTY")) {
				if (!type.equals("void"))
					errors.add(new Error("函数 " + name + " 返回类型不匹配"));
			} else {
				String t = son.getAttribute("type");
				Set<String> s = typeMap.get(type);
				if ((s == null) || (s != null && !s.contains(t))) {
					errors.add(new Error("函数 " + name + " 返回类型不匹配"));
				}
			}
		}

		if (set.size() == 0 && !type.equals("void")) {
			errors.add(new Error("函数 " + name + " 返回类型不匹配"));
		}
	}

	public List<Error> getErrors() {
		return errors;
	}

	public Node getRoot() {
		return root;
	}

	public Map<String, FuncInfo> getFuncsInfoMap() {
		return funcsInfoMap;
	}
}
