package parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

// 语法类
public class Grammar {

	private Set<String> terminals;
	private Set<String> nonterminals;
	private Set<Producer> producers;
	private Map<String, Set<Producer>> producerMap;
	private Map<String, Set<String>> firstMap;
	private Map<String, Set<String>> followMap;
	private File file;

	public Grammar(File file) {
		this.file = file;
		
		terminals = new HashSet<>();
		nonterminals = new HashSet<>();
		producers = new HashSet<>();
		producerMap = new HashMap<>();
		firstMap = new HashMap<>();
		followMap = new HashMap<>();

		initGrammar();
		setFirst();
		setFollow();
		setSeclect();

		check();
	}

	// 获取产生式 终极符 非终极符
	private void initGrammar() {
		Set<String> signs = new HashSet<String>();
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = reader.readLine()) != null) {
				String[] tmp = line.trim().split(" ");
				nonterminals.add(tmp[0]);
				for (String s : tmp) {
					if (!s.equals("->"))
						signs.add(s);
				}
				List<String> right = new ArrayList<>();
				for (int i = 2; i < tmp.length; i++)
					right.add(tmp[i]);
				Producer p = new Producer(tmp[0], right);
				producers.add(p);
				Set<Producer> set = producerMap.getOrDefault(tmp[0], new HashSet<Producer>());
				set.add(p);
				producerMap.put(tmp[0], set);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		signs.removeAll(nonterminals);
		terminals = signs;
	}

	// 求 first 集
	private void setFirst() {
		Set<Producer> producerSet = new HashSet<>();
		Map<String, Integer> numMap = new HashMap<>();
		for (String nonterminal : nonterminals) {
			firstMap.put(nonterminal, new HashSet<String>());
			numMap.put(nonterminal, 0);
		}

		for (Producer p : producers) {
			String left = p.getLeft();
			String first = p.getRight().get(0);
			producerSet.add(p);
			numMap.put(left, numMap.get(left) + 1);
			if (terminals.contains(first)) {
				producerSet.remove(p);
				numMap.put(left, numMap.get(left) - 1);
				firstMap.get(left).add(first);
			}
		}

		while (producerSet.size() > 0) {
			for (Iterator<Producer> it = producerSet.iterator(); it.hasNext();) {
				Producer p = it.next();
				String left = p.getLeft();
				Set<String> set1 = firstMap.get(left);
				List<String> right = p.getRight();

				int index = 0;
				String first = right.get(index++);
				boolean flag = false;
				while (numMap.get(first) == 0) {
					Set<String> set2 = new HashSet<>(firstMap.get(first));
					if (!set2.contains("EMPTY")) {
						set1.addAll(set2);
						flag = true;
						break;
					} else if (index < right.size()) {
						set2.remove("EMPTY");
						set1.containsAll(set2);
						first = right.get(index++);
					} else {
						set1.addAll(set2);
						flag = true;
						break;
					}
				}
				if (flag) {
					it.remove();
					numMap.put(left, numMap.get(left) - 1);
				}
			}
		}
	}

	// 求 follow 集
	private void setFollow() {
		for (String nonterminal : nonterminals) {
			followMap.put(nonterminal, new HashSet<String>());
		}

		if (nonterminals.contains("S"))
			followMap.get("S").add("END");

		boolean flag = true;
		while (flag) {
			flag = false;
			for (Producer p : producers) {
				String left = p.getLeft();
				List<String> right = p.getRight();
				for (int i = 0; i < right.size() - 1; i++) {
					String s = right.get(i);
					if (terminals.contains(s))
						continue;

					Set<String> set1 = followMap.get(s);
					int num = set1.size();

					int index = i + 1;
					if (terminals.contains(right.get(index))) {
						set1.add(right.get(index));
						continue;
					}
					Set<String> set2 = new HashSet<>(firstMap.get(right.get(index++)));
					if (set2.contains("EMPTY")) {
						while (set2.contains("EMPTY")) {
							set2.remove("EMPTY");
							set1.addAll(set2);
							if (index < right.size()) {
								String x = right.get(index++);
								if (terminals.contains(x)) {
									set1.add(x);
									break;
								}
								set2 = new HashSet<>(firstMap.get(x));
							} else {
								set2 = new HashSet<>(followMap.get(left));
								set1.addAll(set2);
							}
						}
					} else {
						set1.addAll(set2);
					}

					if (set1.size() > num)
						flag = true;
				}
				String s = right.get(right.size() - 1);
				if (nonterminals.contains(s)) {
					Set<String> set = followMap.get(s);
					int num = set.size();
					set.addAll(followMap.get(left));
					if (set.size() > num)
						flag = true;
				}
			}
		}
	}

	// 求 select 集
	private void setSeclect() {
		for (Producer p : producers) {
			Set<String> set = p.getSelect();
			String left = p.getLeft();
			List<String> right = p.getRight();

			int index = 0;
			String first = right.get(index++);

			if (first.equals("EMPTY")) {
				set.addAll(followMap.get(left));
				continue;
			} else if (terminals.contains(first)) {
				set.add(first);
				continue;
			}

			while (true) {
				Set<String> firstSet = firstMap.get(first);
				if (firstSet.contains("EMPTY")) {
					Set<String> tmp = new HashSet<>(firstSet);
					tmp.remove("EMPTY");
					set.addAll(tmp);
					if (index < right.size()) {
						first = right.get(index++);
					} else {
						set.addAll(followMap.get(left));
						break;
					}
				} else {
					set.addAll(firstSet);
					break;
				}
			}
		}
	}

	// 检查 select 集是否有冲突
	private void check() {
		Map<String, Set<String>> checkMap = new HashMap<>();
		for (String s : nonterminals) {
			checkMap.put(s, new HashSet<>());
		}
		for (Producer p : producers) {
			String left = p.getLeft();
			Set<String> set = checkMap.get(left);
			Set<String> selectSet = p.getSelect();
			Set<String> tmp = new HashSet<>(set);
			tmp.retainAll(selectSet);
			if (tmp.size() > 0) {
				System.out.println(left + " conflict!!!");
			}
			set.addAll(selectSet);
		}

		System.out.println("check grammar fin");
	}

	public Set<String> getTerminals() {
		return terminals;
	}

	public Set<String> getNonterminals() {
		return nonterminals;
	}

	public Map<String, Set<Producer>> getProducerMap() {
		return producerMap;
	}
}
