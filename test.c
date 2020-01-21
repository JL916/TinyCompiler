int f (int n) {
	// ½×³Ë
	if (n <= 1) {
		return 1;
	} else {
		return n * f(n - 1);
	}
}

void main () {
	int i;
	print("input i:");
	scan(i);
	print(f(i));
}
