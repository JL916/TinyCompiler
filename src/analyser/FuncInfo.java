package analyser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 函数信息
public class FuncInfo {

	public int position;		// 起始位置
	public String returnType;	// 返回类型
	public List<String> vars;	// 变量列表
	public List<String> arglist;// 盗斜
	public Map<String, VarInfo> args;	// 参数表
	public Map<String, VarInfo> locals;// 本地变量表
	public Map<String, VarInfo> tmps;	// 临时变量表
	
	public FuncInfo (String returnType) {
		this.returnType = returnType;
		vars = new ArrayList<String>();
		arglist = new ArrayList<String>();
		args = new HashMap<>();
		locals = new HashMap<>();
		tmps = new HashMap<>();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (String var : vars)
			sb.append(var + ":" + getVarInfo(var) + " ");
		return "position=" + position + " vars=" + sb;
	}
	
	public VarInfo getVarInfo (String id) {
		if (args.containsKey(id))
			return args.get(id);
		if (locals.containsKey(id))
			return locals.get(id);
		if (tmps.containsKey(id))
			return tmps.get(id);
		return null;
	}
}
