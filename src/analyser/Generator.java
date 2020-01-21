package analyser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// 四元式生成 递归处理非终极符
public class Generator {
	
	private List<FourItem> fourItemList;
	private Map<String, FuncInfo> funcsInfoMap;
	private FuncInfo funcInfo;
	
	public void generate (Map<String, FuncInfo> funcsInfoMap, Node root) {
		this.funcsInfoMap = funcsInfoMap;
		fourItemList = new ArrayList<>();
		handleS(root);
	}
	
	private void handleS(Node s) {
		handleFunc(s.getSon(0));
		handleFuncs(s.getSon(1));
	}

	private void handleFunc(Node func) {
		String id = func.getSon(1).getAttribute("val");
		funcsInfoMap.get(id).position = fourItemList.size() + 1;
		funcInfo = funcsInfoMap.get(func.getSon(1).getAttribute("val"));
		handleDefinelist(func.getSon(6));
		handleStatements(func.getSon(7));
	}

	private void handleDefinelist(Node definelist) {
		if (definelist.getSons().size() == 1)
			return ;
		handleDefinestatement(definelist.getSon(0));
		handleDefinelist(definelist.getSon(1));
	}

	private void handleDefinestatement(Node definestatement) {
		Node init = definestatement.getSon(2);
		if (init.getSons().size() > 1) {
			Node expression = init.getSon(1);
			handleExpression(expression);
			String val = expression.getAttribute("val");
			FourItem fourItem = new FourItem("=", val, "", definestatement.getSon(1).getAttribute("val"));
			
			if (expression.getAttribute("flag").equals("true")) {
				fourItem.arg1 = fourItemList.get(Integer.parseInt(val) - 1).result;
			}
			fourItemList.add(fourItem);
		}
		
		handleOther(definestatement.getSon(3));
	}
	
	private void handleOther(Node other) {
		if (other.getSons().size() == 1)
			return ;
		Node init = other.getSon(2);
		if (init.getSons().size() > 1) {
			Node expression = init.getSon(1);
			handleExpression(expression);
			String val = expression.getAttribute("val");
			FourItem fourItem = new FourItem("=", val, "", other.getSon(1).getAttribute("val"));
			
			if (expression.getAttribute("flag").equals("true")) {
				fourItem.arg1 = fourItemList.get(Integer.parseInt(val) - 1).result;
			}
			fourItemList.add(fourItem);
		}
		handleOther(other.getSon(3));
	}

	private void handleFuncs(Node funcs) {
		String name = funcs.getSon(0).getName();
		if (name.equals("EMPTY")) {
			return;
		} else if (name.equals("func")) {
			handleFunc(funcs.getSon(0));
			handleFuncs(funcs.getSon(1));
		}
	}

	private void handleStatements(Node statements) {
		String name = statements.getSon(0).getName();
		if (name.equals("EMPTY")) {
			return;
		} else if (name.equals("statement")) {
			handleStatement(statements.getSon(0));
			handleStatements(statements.getSon(1));
		}
	}

	private void handleStatement(Node statement) {
		Node son = statement.getSon(0);
		String name = son.getName();
		if (name.equals("expression")) {
			handleExpression(son);
		} else if (name.equals("ifstatement")) {
			handleIfstatement(son);
		} else if (name.equals("whilestatement")) {
			handleWhilestatement(son);
		} else if (name.equals("forstatement")) {
			handleForstatement(son);
		} else if (name.equals("dowhilestatement")) {
			handleDowhilestatement(son);
		} else if (name.equals("returnstatement")) {
			handleReturnstatement(son);
		}
	}

	private void handleExpression(Node expression) {
		Node item = expression.getSon(0);
		Node expr = expression.getSon(1);
		handleItem(item);
		handleExpr(expr);
		if (expr.getAttribute("op2") == null) {
			expression.setAttribute("val", item.getAttribute("val"));
			expression.setAttribute("flag", item.getAttribute("flag"));
		} else {
			String op2 = expr.getAttribute("op2");
			String id = "$" + (fourItemList.size() + 1);
			FourItem fourItem = new FourItem(op2, "", "", id);
			String itemVal = item.getAttribute("val");
			String exprVal = expr.getAttribute("val");
			String arg1 = item.getAttribute("flag").equals("true")
					? fourItemList.get(Integer.parseInt(itemVal) - 1).result
					: itemVal;
			String arg2 = expr.getAttribute("flag").equals("true")
					? fourItemList.get(Integer.parseInt(exprVal) - 1).result
					: exprVal;
			fourItem.arg1 = arg1;
			fourItem.arg2 = arg2;

			String type = expression.getAttribute("type");
			funcInfo.vars.add(id);
			funcInfo.tmps.put(id, new VarInfo(type));
			
			if (op2.equals("=")) {
				funcInfo.vars.remove(id);
				funcInfo.tmps.remove(id);
				fourItem.result = fourItem.arg1;
				fourItem.arg1 = fourItem.arg2;
				fourItem.arg2 = "";
			}
			fourItemList.add(fourItem);
			expression.setAttribute("val", fourItemList.size() + "");
			expression.setAttribute("flag", "true");
		}
	}

	private void handleItem(Node item) {
		Node node = item.getSon(0);
		item.setAttribute("flag", "false");
		if (node.getName().equals("IDENTIFIER")) {
			String id = node.getAttribute("val");
			if (item.getSon(1).getSons().size() == 1) {
				item.setAttribute("val", id);
			} else if (id.equals("print")) {
				FourItem fourItem = new FourItem("print", "", "", "");
				Node expression = item.getSon(1).getSon(1).getSon(0);
				handleExpression(expression);
				String val = expression.getAttribute("val");
				if (expression.getAttribute("flag").equals("true"))
					val = fourItemList.get(Integer.parseInt(val) - 1).result;
				fourItem.result = val;
				fourItemList.add(fourItem);
			} else if (id.equals("scan")) {
				FourItem fourItem = new FourItem("scan", "", "", "");
				Node expression = item.getSon(1).getSon(1).getSon(0);
				handleExpression(expression);
				String val = expression.getAttribute("val");
				if (expression.getAttribute("flag").equals("true"))
					val = fourItemList.get(Integer.parseInt(val) - 1).result;
				fourItem.result = val;
				fourItemList.add(fourItem);
			} else {
				FourItem fourItem = new FourItem("call", id, "", "");
				List<String> list = new ArrayList<>();
				Node parameters = item.getSon(1).getSon(1);
				if (parameters.getSons().size() > 1) {
					Node expression = parameters.getSon(0);
					handleExpression(expression);
					String val = expression.getAttribute("val");
					if (expression.getAttribute("flag").equals("true"))
						val = fourItemList.get(Integer.parseInt(val) - 1).result;
					list.add(val);
					handleParam(list, parameters.getSon(1));
					fourItem.arg2 = list.toString();
				}
				fourItem.result = "$" + (fourItemList.size() + 1);
				
				String type = item.getAttribute("type");
				funcInfo.vars.add(fourItem.result);
				funcInfo.tmps.put(fourItem.result, new VarInfo(type));
				
				fourItemList.add(fourItem);
				item.setAttribute("val", fourItemList.size() + "");
				item.setAttribute("flag", "true");
			}
		} else if (node.getName().equals("INTEGER")) {
			item.setAttribute("val", node.getAttribute("val"));
		} else if (node.getName().equals("FLOAT")) { 
			item.setAttribute("val", node.getAttribute("val"));
		} else if (node.getName().equals("'")) {
			item.setAttribute("val", "'" + item.getSon(1).getAttribute("val") + "'");
		} else if (node.getName().equals("\"")) {
			item.setAttribute("val", "\"" + item.getSon(1).getAttribute("val") + "\"");
		} else if (node.getName().equals("(")) {
			Node expression = item.getSon(1);
			handleExpression(expression);
			item.setAttribute("val", expression.getAttribute("val"));
			item.setAttribute("flag", expression.getAttribute("flag"));
		} else if (node.getName().equals("op1")) {
			Node item2 = item.getSon(1);
			handleItem(item2);
			String op1 = node.getAttribute("val");
			String id = "$" + (fourItemList.size() + 1);
			FourItem fourItem = new FourItem(op1, item2.getAttribute("val"), "", id);
			if (item2.getAttribute("flag").equals("true"))
				fourItem.arg1 = fourItemList.get(Integer.parseInt(fourItem.arg1) - 1).result;

			String type = item.getAttribute("type");
			funcInfo.vars.add(id);
			funcInfo.tmps.put(id, new VarInfo(type));
			
			if (op1.equals("++") || op1.equals("--")) {
				funcInfo.vars.remove(id);
				funcInfo.tmps.remove(id);
				fourItem.op = fourItem.op.substring(0, 1);
				fourItem.result = fourItem.arg1;
				fourItem.arg2 = "1";
			} else if (op1.equals("+") || op1.equals("-")) {
				fourItem.arg2 = fourItem.arg1;
				fourItem.arg1 = "0";
			}
			
			fourItemList.add(fourItem);
			item.setAttribute("val", fourItemList.size() + "");
			item.setAttribute("flag", "true");
		}
	}

	private void handleExpr(Node expr) {
		Node node = expr.getSon(0);
		if (node.getName().equals("EMPTY")) {
			return;
		} else if (node.getName().equals("op2")) {
			expr.setAttribute("op2", node.getAttribute("val"));
			Node expression = expr.getSon(1);
			handleExpression(expression);
			expr.setAttribute("val", expression.getAttribute("val"));
			expr.setAttribute("flag", expression.getAttribute("flag"));
		}
	}
	
	private void handleParam(List<String> list, Node param) {
		if (param.getSons().size() == 1)
			return ;
		Node expression = param.getSon(1);
		handleExpression(expression);
		String val = expression.getAttribute("val");
		if (expression.getAttribute("flag").equals("true"))
			val = fourItemList.get(Integer.parseInt(val) - 1).result;
		list.add(val);
		handleParam(list, param.getSon(2));
	}

	private void handleIfstatement(Node ifstatement) {
		Node expression = ifstatement.getSon(2);
		Node statements = ifstatement.getSon(5);
		Node elsestate = ifstatement.getSon(7);

		handleExpression(expression);
		String val = expression.getAttribute("val");
		FourItem jnzItem = new FourItem("jnz", val, "", "");
		if (expression.getAttribute("flag").equals("true")) {
			jnzItem.arg1 = fourItemList.get(Integer.parseInt(val) - 1).result;
		}
		fourItemList.add(jnzItem);

		handleElsestate(elsestate);

		jnzItem.result = (fourItemList.size() + 2) + "";

		FourItem jItem = new FourItem("j", "", "", "");
		fourItemList.add(jItem);

		handleStatements(statements);

		jItem.result = (fourItemList.size() + 1) + "";
	}

	private void handleElsestate(Node elsestate) {
		String name = elsestate.getSon(0).getName();
		if (name.equals("else")) {
			handleStatements(elsestate.getSon(2));
		} else if (name.equals("EMPTY")) {
			return;
		}
	}
	
	private void handleWhilestatement(Node whilestatement) {
		Node expression = whilestatement.getSon(2);
		Node statements = whilestatement.getSon(5);

		int label = fourItemList.size() + 1;
		handleExpression(expression);
		String val = expression.getAttribute("val");
		FourItem jzItem = new FourItem("jz", val, "", "");
		if (expression.getAttribute("flag").equals("true")) {
			jzItem.arg1 = fourItemList.get(Integer.parseInt(val) - 1).result;
		}
		fourItemList.add(jzItem);

		handleStatements(statements);
		
		jzItem.result = (fourItemList.size() + 2) + "";

		FourItem jItem = new FourItem("j", "", "", label + "");
		fourItemList.add(jItem);
	}
	
	private void handleForstatement(Node forstatement) {
		Node expression1 = forstatement.getSon(2);
		Node expression2 = forstatement.getSon(4);
		Node expression3 = forstatement.getSon(6);
		Node statements = forstatement.getSon(9);
		
		handleExpression(expression1);
		
		int label = fourItemList.size() + 1;
		handleExpression(expression2);
		String val = expression2.getAttribute("val");
		FourItem jzItem = new FourItem("jz", val, "", "");
		if (expression2.getAttribute("flag").equals("true")) {
			jzItem.arg1 = fourItemList.get(Integer.parseInt(val) - 1).result;
		}
		fourItemList.add(jzItem);
		
		handleStatements(statements);
		handleExpression(expression3);
		
		jzItem.result = (fourItemList.size() + 2) + "";

		FourItem jItem = new FourItem("j", "", "", label + "");
		fourItemList.add(jItem);
	}
	
	private void handleDowhilestatement(Node dowhilestatement) {
		Node statements = dowhilestatement.getSon(2);
		Node expression = dowhilestatement.getSon(6);
		
		int label = fourItemList.size() + 1;
		handleStatements(statements);
		
		handleExpression(expression);
		String val = expression.getAttribute("val");
		FourItem jnzItem = new FourItem("jnz", val, "", label + "");
		if (expression.getAttribute("flag").equals("true")) {
			jnzItem.arg1 = fourItemList.get(Integer.parseInt(val) - 1).result;
		}
		fourItemList.add(jnzItem);
	}
	
	private void handleReturnstatement(Node returnstatement) {
		Node node = returnstatement.getSon(1).getSon(0);
		String name = node.getName();
		if (name.equals("EMPTY")) {
			FourItem fourItem = new FourItem("ret", "", "", "");
			fourItemList.add(fourItem);
		} else if (name.equals("expression")) {
			handleExpression(node);
			String val = node.getAttribute("val");
			FourItem fourItem = new FourItem("ret", "", "", val);
			if (node.getAttribute("flag").equals("true")) {
				fourItem.result = fourItemList.get(Integer.parseInt(val) - 1).result;
			}
			fourItemList.add(fourItem);
		}
	}

	public List<FourItem> getFourItemList() {
		return fourItemList;
	}
}
