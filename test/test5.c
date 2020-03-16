void main () {
	char c;
	print("input c:");
	scan(c);

	if ((c >= '0') && (c <= '9')) {
		print("数字");
	} else {
		if (((c >= 'a') && (c <= 'z')) ||
				((c >= 'A') && (c <= 'Z'))) {
			print("字母");
		} else {
			print("符号");
		}
	}
}
