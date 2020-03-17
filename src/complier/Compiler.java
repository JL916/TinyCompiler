package complier;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

import analyser.Analyser;
import analyser.FourItem;
import analyser.Generator;
import error.Error;
import interpreter.Interpreter;
import parser.Parser;
import tokenizer.Token;
import tokenizer.Tokenizer;

// 编译程序的入口 负责界面和调用其他类
public class Compiler extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;

	// 菜单
	private JMenuItem openMenu;
	private JMenuItem saveMenu;
	private JMenuItem tokenizerMenu;
	private JMenuItem parserMenu;
	private JMenuItem analyserMenu;
	private JMenuItem runMenu;
	private JMenuItem compileAndRunMenu;

	// 文本
	private JTextArea textArea;
	private JTable tokenTable;
	private DefaultTableModel tokenTableModel;
	private JTextArea codeArea;
	private JTextArea resultArea;

	// 编译
	private Tokenizer tokenizer;
	private Parser parser;
	private Analyser analyser;
	private Generator generator;
	private Interpreter interpreter;

	public Compiler() {
		Font f = null;
		try {
			f = Font.createFont(Font.TRUETYPE_FONT, new File("resources/NotoSansMonoCJKsc-Regular.otf"));
		} catch (FontFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		f = f.deriveFont(Font.PLAIN, 16);

		openMenu = new JMenuItem("打开");
		saveMenu = new JMenuItem("保存");
		tokenizerMenu = new JMenuItem("词法分析");
		parserMenu = new JMenuItem("语法分析");
		analyserMenu = new JMenuItem("语义分析");
		runMenu = new JMenuItem("执行");
		compileAndRunMenu = new JMenuItem("编译并运行");

		textArea = new JTextArea();
		tokenTableModel = new DefaultTableModel(null, new String[] { "值", "类别" });
		tokenTable = new JTable(tokenTableModel);
		codeArea = new JTextArea();
		resultArea = new JTextArea();

		textArea.setFont(f);
		tokenTable.setFont(f);
		tokenTable.setRowHeight(25);
		codeArea.setFont(f);
		resultArea.setFont(f);

		tokenizer = new Tokenizer();
		parser = new Parser(new File("grammar.txt"));
		analyser = new Analyser();
		generator = new Generator();
		interpreter = new Interpreter(resultArea);

		JMenuBar menuBar = new JMenuBar();
		JMenu file = new JMenu("文件");
		JMenu compile = new JMenu("编译");
		JMenu run = new JMenu("运行");

		openMenu.addActionListener(this);
		saveMenu.addActionListener(this);
		tokenizerMenu.addActionListener(this);
		parserMenu.addActionListener(this);
		analyserMenu.addActionListener(this);
		runMenu.addActionListener(this);
		compileAndRunMenu.addActionListener(this);

		file.add(openMenu);
		file.add(saveMenu);
		compile.add(tokenizerMenu);
		compile.add(parserMenu);
		compile.add(analyserMenu);
		run.add(runMenu);
		run.add(compileAndRunMenu);
		menuBar.add(file);
		menuBar.add(compile);
		menuBar.add(run);

		JPanel textPanel = new JPanel();
		JPanel tokensPanel = new JPanel();
		JPanel codePanel = new JPanel();
		JPanel resultPanel = new JPanel();

		textPanel.setLocation(50, 30);
		textPanel.setSize(360, 480);
		tokensPanel.setLocation(430, 30);
		tokensPanel.setSize(300, 480);
		codePanel.setLocation(750, 30);
		codePanel.setSize(330, 650);
		resultPanel.setLocation(50, 530);
		resultPanel.setSize(680, 150);

		JScrollPane textScroll = new JScrollPane(textArea);
		textArea.setTabSize(2);
		textArea.setLineWrap(true);
		JLabel textLabel = new JLabel("文本编辑");
		textPanel.setLayout(new BorderLayout());
		textPanel.add(textLabel, BorderLayout.NORTH);
		textPanel.add(textScroll);

		JScrollPane tokensScroll = new JScrollPane(tokenTable);
		tokenTable.setEnabled(false);
		tokenTable.setPreferredScrollableViewportSize(new Dimension(340, 460));
		JLabel tokensLabel = new JLabel("词法分析");
		tokensPanel.setLayout(new BorderLayout());
		tokensPanel.add(tokensLabel, BorderLayout.NORTH);
		tokensPanel.add(tokensScroll);

		JScrollPane codeScroll = new JScrollPane(codeArea);
		codeArea.setLineWrap(true);
		codeArea.setEditable(false);
		JLabel codeLabel = new JLabel("中间代码");
		codePanel.setLayout(new BorderLayout());
		codePanel.add(codeLabel, BorderLayout.NORTH);
		codePanel.add(codeScroll);

		JScrollPane resultScroll = new JScrollPane(resultArea);
		resultArea.setLineWrap(true);
		resultPanel.setLayout(new BorderLayout());
		resultPanel.add(resultScroll, BorderLayout.CENTER);

		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension screen = toolkit.getScreenSize();
		this.setTitle("Tiny Compiler");
		this.setSize(1130, 800);
		this.setLocation(screen.width / 2 - this.getWidth() / 2, screen.height / 2 - this.getHeight() / 2);
		this.setResizable(false);
		this.setLayout(null);
		this.setJMenuBar(menuBar);
		this.add(textPanel);
		this.add(tokensPanel);
		this.add(codePanel);
		this.add(resultPanel);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == tokenizerMenu) {
			// 词法分析
			tokenTableModel.setRowCount(0);
			resultArea.setText("");
			tokenizer.analysis(textArea.getText());
			List<Token> tokens = tokenizer.getTokens();
			for (int i = 0; i < tokens.size() - 1; i++) {
				Token token = tokens.get(i);
				tokenTableModel.addRow(new String[] { token.getValue(), token.getType().print() });
			}

			List<Error> errors = tokenizer.getErrors();
			for (Error error : errors) {
				resultArea.append(error + "\n");
			}
		} else if (e.getSource() == parserMenu) {
			// 语法分析
			if (tokenizer.getTokens() == null || tokenizer.getTokens().size() == 1) {
				resultArea.append("未输入字符或未进行词法分析\n");
				return;
			} else if (!resultArea.getText().isBlank() && !resultArea.getText().contains("无语法错误")) {
				resultArea.append("存在错误 无法进行语法分析\n");
				return;
			}
			parser.parse(tokenizer);
			List<Error> errors = parser.getErrors();
			for (Error error : errors) {
				resultArea.append(error + "\n");
			}
			if (errors.size() == 0) {
				resultArea.append("语法检查完成 无语法错误\n");
			}
		} else if (e.getSource() == analyserMenu) {
			// 语义分析和中间代码生成
			if (parser.getRoot() == null) {
				resultArea.append("未进行语法分析\n");
				return;
			}
			String text = resultArea.getText();
			if (!text.isBlank() && !text.contains("无语法错误")) {
				resultArea.append("存在错误 无法进行语义分析\n");
				return;
			}

			resultArea.setText("");
			analyser.analyse(parser.getRoot());
			codeArea.setText("");
			List<Error> errors = analyser.getErrors();
			for (Error error : errors) {
				resultArea.append(error + "\n");
			}
			if (errors.size() == 0) {
				generator.generate(analyser.getFuncsInfoMap(), analyser.getRoot());
				List<FourItem> fourItems = generator.getFourItemList();
				for (int i = 0; i < fourItems.size(); i++) {
					FourItem item = fourItems.get(i);
					codeArea.append(String.format("%-3d:", i + 1));
					codeArea.append(item + "\n");
				}
			}
		} else if (e.getSource() == runMenu) {
			// 解释运行
			if (generator.getFourItemList() == null) {
				resultArea.append("未进行语义分析\n");
				return;
			}
			String text = resultArea.getText();
			if (!text.isBlank() && !text.contains("运行结束")) {
				resultArea.append("存在错误 无法执行\n");
				return;
			}
			resultArea.setText("");
			resultArea.requestFocus();
			new Thread() {
				@Override
				public void run() {
					interpreter.interpreter(analyser.getFuncsInfoMap(), generator.getFourItemList());
				}
			}.start();
		} else if (e.getSource() == compileAndRunMenu) {
			// 编译并运行
			tokenTableModel.setRowCount(0);
			codeArea.setText("");
			resultArea.setText("");

			tokenizer.analysis(textArea.getText());
			List<Token> tokens = tokenizer.getTokens();
			for (int i = 0; i < tokens.size() - 1; i++) {
				Token token = tokens.get(i);
				tokenTableModel.addRow(new String[] { token.getValue(), token.getType().print() });
			}
			if (tokenizer.getErrors().size() > 0) {
				for (Error error : tokenizer.getErrors()) {
					resultArea.append(error + "\n");
				}
				return;
			}

			parser.parse(tokenizer);
			if (parser.getErrors().size() > 0) {
				for (Error error : parser.getErrors()) {
					resultArea.append(error + "\n");
				}
				return;
			}

			analyser.analyse(parser.getRoot());
			if (analyser.getErrors().size() > 0) {
				for (Error error : analyser.getErrors()) {
					resultArea.append(error + "\n");
				}
				return;
			}

			generator.generate(analyser.getFuncsInfoMap(), analyser.getRoot());
			List<FourItem> fourItems = generator.getFourItemList();
			for (int i = 0; i < fourItems.size(); i++) {
				FourItem item = fourItems.get(i);
				codeArea.append(String.format("%-3d:", i + 1));
				codeArea.append(item + "\n");
			}

			resultArea.requestFocus();
			new Thread() {
				@Override
				public void run() {
					interpreter.interpreter(analyser.getFuncsInfoMap(), fourItems);
				}
			}.start();
		} else if (e.getSource() == openMenu) {
			// 打开文件
			JFileChooser chooser = new JFileChooser();
			chooser.setCurrentDirectory(new File("."));
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.showOpenDialog(this);
			File file = new File(chooser.getSelectedFile().getPath());
			textArea.setText("");
			try (BufferedReader reader = new BufferedReader(new FileReader(file, Charset.forName("UTF-8")))) {
				String line;
				while ((line = reader.readLine()) != null) {
					textArea.append(line + "\n");
				}
			} catch (IOException e2) {
				e2.printStackTrace();
			}
		} else if (e.getSource() == saveMenu) {
			// 保存文件
			JFileChooser chooser = new JFileChooser();
			chooser.setCurrentDirectory(new File("."));
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.showOpenDialog(this);
			File file = new File(chooser.getSelectedFile().getPath());
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
				writer.write(textArea.getText());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		Compiler frame = new Compiler();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
