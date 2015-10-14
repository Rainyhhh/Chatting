package au.edu.unimelb.tcp.server;

import java.util.Stack;

public class UnusedNames {

	private static Stack<Integer> names = new Stack<Integer>();

	public static Stack<Integer> getNames() {
		return names;
	}

	public static void setNames(Stack<Integer> names) {
		UnusedNames.names = names;
	}
	
	public static void push(int num) {
		names.push(num);
		sort();
	}

	public static void sort() {
		int x = 0;
		if (!names.isEmpty()) {
			x = names.pop();
			sort();
			insert(x);
		}
	}

	// At each step check if stack.peek < x, and insert below top recursively
	public static void insert(int x) {
		if (!names.isEmpty() && names.peek() <= x) {
			int y = names.pop();
			insert(x);
			names.push(y);
		} else {
			names.push(x);
		}
	}
//	public static void main(String []args) {
//		push(2);
//		push(5);
//		push(3);
//		System.out.print(names.pop());
//		System.out.print(names.pop());
//	}
}
