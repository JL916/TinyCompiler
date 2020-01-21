int gcd (int m, int n) {
	int r;
	r = m % n;
	if (r == 0) {
		return n;
	} else {
		return gcd(n, r);
	}
}

int main () {
	int m, n, t;
	print("m=");
	scan(m);
	print("n=");
	scan(n);	

	if (m < n) {
		t = m;
		m = n;
		n = t;
	}

	print(gcd(m, n));
	return 0;
}
