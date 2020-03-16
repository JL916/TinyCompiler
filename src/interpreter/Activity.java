package interpreter;

// 活动记录
public class Activity {

	String func;	// 函数名
	int start;		// 变量栈中的变量起始位置
	int now;		// 当前执行四元式的下标
	boolean flag;	// 判断子程序是否执行
	
	public Activity(String func, int start, int now) {
		this.func = func;
		this.start = start;
		this.now = now;
	}
}
