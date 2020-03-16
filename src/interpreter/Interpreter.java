package interpreter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JTextArea;

import analyser.FourItem;
import analyser.FuncInfo;
import analyser.VarInfo;

// 解释程序
public class Interpreter {

	private FuncInfo funcInfo;				// 函数信息表
	private Activity curActivity;			// 当前活动
	private List<VarInfo> varStack;			// 函数变量栈
	private List<Activity> activityStack;	// 活动记录栈
	private String retVal;					// 返回值
	private JTextArea textArea;		
	private StringBuilder sb;
	
	public Interpreter (JTextArea text) {
		new ASCII();
		this.textArea = text;
	}
	
	public void interpreter(Map<String, FuncInfo> funcsInfoMap, List<FourItem> fourItemList) {
		sb = new StringBuilder();
		
		activityStack = new ArrayList<>();
		varStack = new ArrayList<>();
		
		activityStack.add(new Activity("main", 0, funcsInfoMap.get("main").position - 1));
		curActivity = activityStack.get(0);
		
		funcInfo = funcsInfoMap.get("main");
		
		// 将 main 函数所有变量压栈
		for (String var : funcInfo.vars) {
			String type = funcInfo.getVarInfo(var).type;
			varStack.add(new VarInfo(type));
		}
		
		while (!activityStack.isEmpty() && curActivity.now < fourItemList.size()) {
			FourItem fourItem = fourItemList.get(curActivity.now++);
			String op = fourItem.op;
			String arg1Val = getVal(fourItem.arg1);
			String arg2Val = getVal(fourItem.arg2);
			String result = fourItem.result;
			String resultType = getType(fourItem.result);
			String resultVal = getVal(result);
			String f = "0";
			String t = "1";
			
			switch(op) {
			case "+" :
				if (resultType.equals("int")) {
					setVal(result, (Integer.parseInt(arg1Val) + Integer.parseInt(arg2Val)) + "");
				} else if (resultType.equals("float")) {
					setVal(result, (Float.parseFloat(arg1Val) + Float.parseFloat(arg2Val)) + "");
				} else if (resultType.equals("char")) {
					setVal(result, (Integer.parseInt(arg1Val) + Integer.parseInt(arg2Val)) + "");
				}
				break;
			case "-" :
				if (resultType.equals("int")) {
					setVal(result, (Integer.parseInt(arg1Val) - Integer.parseInt(arg2Val)) + "");
				} else if (resultType.equals("float")) {
					setVal(result, (Float.parseFloat(arg1Val) - Float.parseFloat(arg2Val)) + "");
				} else if (resultType.equals("char")) {
					setVal(result, (Integer.parseInt(arg1Val) - Integer.parseInt(arg2Val)) + "");
				}
				break;
			case "*" :
				if (resultType.equals("int")) {
					setVal(result, (Integer.parseInt(arg1Val) * Integer.parseInt(arg2Val)) + "");
				} else if (resultType.equals("float")) {
					setVal(result, (Float.parseFloat(arg1Val) * Float.parseFloat(arg2Val)) + "");
				} else if (resultType.equals("char")) {
					setVal(result, (Integer.parseInt(arg1Val) * Integer.parseInt(arg2Val)) + "");
				}
				break;
			case "/" :
				if (resultType.equals("int")) {
					setVal(result, (Integer.parseInt(arg1Val) / Integer.parseInt(arg2Val)) + "");
				} else if (resultType.equals("float")) {
					setVal(result, (Float.parseFloat(arg1Val) / Float.parseFloat(arg2Val)) + "");
				} else if (resultType.equals("char")) {
					setVal(result, (Integer.parseInt(arg1Val) / Integer.parseInt(arg2Val)) + "");
				}
				break;
			case "%" :
				setVal(result, (Integer.parseInt(arg1Val) % Integer.parseInt(arg2Val)) + "");
				break;
			case "&" :
				setVal(result, (Integer.parseInt(arg1Val) & Integer.parseInt(arg2Val)) + "");
				break;
			case "|" :
				setVal(result, (Integer.parseInt(arg1Val) | Integer.parseInt(arg2Val)) + "");
				break;
			case "^" :
				setVal(result, (Integer.parseInt(arg1Val) ^ Integer.parseInt(arg2Val)) + "");
				break;
			case "<" :
				if (Float.parseFloat(arg1Val) < Float.parseFloat(arg2Val))
					setVal(result, t);
				else
					setVal(result, f);
				break;
			case ">" :
				if (Float.parseFloat(arg1Val) > Float.parseFloat(arg2Val))
					setVal(result, t);
				else
					setVal(result, f);
				break;
			case "=" :
				setVal(result, arg1Val);
				break;
			case "<=" :
				if (Float.parseFloat(arg1Val) <= Float.parseFloat(arg2Val))
					setVal(result, t);
				else
					setVal(result, f);
				break;
			case ">=" :
				if (Float.parseFloat(arg1Val) >= Float.parseFloat(arg2Val))
					setVal(result, t);
				else
					setVal(result, f);
				break;
			case "!=" :
				if (Float.parseFloat(arg1Val) != Float.parseFloat(arg2Val))
					setVal(result, t);
				else
					setVal(result, f);
				break;
			case "==" :
				if (Float.parseFloat(arg1Val) == Float.parseFloat(arg2Val))
					setVal(result, t);
				else
					setVal(result, f);
				break;
			case "&&" :
				if (Float.parseFloat(arg1Val) != 0 && Float.parseFloat(arg2Val) != 0)
					setVal(result, t);
				else
					setVal(result, f);
				break;
			case "||" :
				if (Float.parseFloat(arg1Val) != 0 || Float.parseFloat(arg2Val) != 0)
					setVal(result, t);
				else
					setVal(result, f);
				break;
			case "~" :
				setVal(result, (~Integer.parseInt(arg1Val)) + "");
				break;
			case "!" :
				if (Float.parseFloat(arg1Val) == 0)
					setVal(result, t);
				else
					setVal(result, f);
				break;
			case "j" :
				curActivity.now = Integer.parseInt(result) - 1;
				break;
			case "jnz" :
				if (arg1Val.equals("1"))
					curActivity.now = Integer.parseInt(result) - 1;
				break;
			case "jz" :
				if (arg1Val.equals("0"))
					curActivity.now = Integer.parseInt(result) - 1;
				break;
			case "print" :
				if (resultType.equals("char"))
					resultVal = (char)Integer.parseInt(resultVal) + "";
				else if (resultType.equals("string"))
					resultVal = resultVal.substring(1, resultVal.length()-1);
				textArea.append(resultVal + "\n");
				sb = new StringBuilder(textArea.getText());
				textArea.setCaretPosition(sb.length());
				break;
			case "scan" :
				String input = textArea.getText().substring(sb.length());
				while (input.isBlank() || !input.contains("\n")) {
					input = textArea.getText().substring(sb.length());
				}
				input = input.replace("\n", "");
				if (resultType.equals("char")) {
					if (ASCII.map.containsKey(input))
						setVal(result, ASCII.map.get(input) + "");
					else
						setVal(result, (int)input.charAt(0) + "");
				} else if (resultType.equals("string")) {
					setVal(result, "\"" + input + "\"");
				} else {
					setVal(result, input);
				}
				sb = new StringBuilder(textArea.getText());
				break;
			case "ret" :
				retVal = getVal(result);
				for (int i = 0; i < funcInfo.vars.size(); i++)
					varStack.remove(varStack.size()-1);
				activityStack.remove(activityStack.size()-1); 
				if (!activityStack.isEmpty()) {
					curActivity = activityStack.get(activityStack.size()-1);
					funcInfo = funcsInfoMap.get(curActivity.func);
				}
				break;
			case "call":
				if (!curActivity.flag) {
					curActivity.now--;
					curActivity.flag = true;
					
					if (!arg2Val.isBlank())
						arg2Val = arg2Val.substring(1, arg2Val.length()-1);
					String[] args = arg2Val.replace(" ", "").split(",");
								
					for (int i = 0; i < args.length; i++) {
						args[i] = getVal(args[i]);
					}
					
					activityStack.add(new Activity(arg1Val, varStack.size(), funcsInfoMap.get(arg1Val).position - 1));
					curActivity = activityStack.get(activityStack.size() - 1);
					curActivity.start = varStack.size();
					funcInfo = funcsInfoMap.get(arg1Val);
					for (String var : funcInfo.vars) {
						String type = funcInfo.getVarInfo(var).type;
						varStack.add(new VarInfo(type));
					}
					
					for (int i = 0; i < funcInfo.arglist.size(); i++) {
						setVal(funcInfo.arglist.get(i), args[i]);
					}
					
				} else {
					curActivity.flag = false;
					setVal(result, retVal);
				}
				break;
			}
		}
		
		textArea.append("运行结束\n");
	}
	
	private String getVal(String id) {
		if (id.isBlank())
			return id;
		if (funcInfo.vars.contains(id)) {
			int index = funcInfo.vars.indexOf(id);
			return varStack.get(curActivity.start + index).val;
		} else if (id.charAt(0) == '\'') {
			id = id.substring(1, id.length()-1);
			if (ASCII.map.containsKey(id))
				return ASCII.map.get(id) + "";
			else
				return (int)id.charAt(0) + "";
		} 
		return id;
	}
	
	private String getType(String id) {
		if (id.isBlank())
			return id;
		if (funcInfo.vars.contains(id)) {
			int index = funcInfo.vars.indexOf(id);
			return varStack.get(curActivity.start + index).type;
		} else if (id.charAt(0) == '\'') {
			return "char";
		} else if (id.charAt(0) == '\"') {
			return "string";
		} else {
			return null;
		}
	}
	
	private void setVal(String id, String val) {
		int index = funcInfo.vars.indexOf(id);
		varStack.get(curActivity.start + index).val = val;
	}
}
