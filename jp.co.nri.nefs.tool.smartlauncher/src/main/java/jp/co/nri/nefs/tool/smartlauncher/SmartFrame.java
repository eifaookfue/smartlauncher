package jp.co.nri.nefs.tool.smartlauncher;

import java.awt.AWTException;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Predicate;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.lang.StringUtils;

/**
 * Hello world!
 *
 */
public class SmartFrame extends JFrame
{
	private FileListCreator creator;
	private List<File> fileList;

	public SmartFrame() {
		this.fileList = new ArrayList<>();
		//files = Arrays.stream(dir.listFiles(f -> f.isFile())).collect(Collectors.toList());
        Path directoryFile = Paths.get("C:\\pleiades\\workspace\\jp.co.nri.nefs.tool.launcher\\conf\\searchdir.txt");
        creator = new FileListCreator(this, directoryFile);
        creator.start();
	}

	public void replaceList(List<File> newList){
		SwingUtilities.invokeLater(() -> {
			this.fileList = newList;
		});
	}

	private class MyDocmentListener implements DocumentListener{

		private JTable table;
		private DefaultTableModel tableModel;
		private JTextField textField;

		public MyDocmentListener(JTable table, DefaultTableModel tableModel, JTextField textField) {
			this.table = table;
			this.tableModel = tableModel;
			this.textField = textField;
		}


		private void update() {
			// いったん全部TableModelから削除
			if (tableModel.getRowCount() > 0){
				for (int i = tableModel.getRowCount() - 1; i > -1; i--){
					tableModel.removeRow(i);
				}
			}

			// 半角スペースもしくは全角スペースで分割
			System.out.println("textField.getText()= " + textField.getText().length());
			if (textField.getText() == null)
				System.out.println("textField.getText()=null");
			String[] keys = textField.getText().split("\\s|　");

			// スペースで区切られたkeyのすべてにマッチした場合
			Predicate<File> filter = file -> {
				return Arrays.stream(keys).allMatch(key ->
					StringUtils.containsIgnoreCase(file.getName(), key)
				);
			};

			/*fileList.stream().filter(filter)
				.map(f -> {
					String[] starray = new String[1];
					starray[0] = f.getName();
					return starray;
				}).forEach(tableModel::addRow);*/

			fileList.stream().filter(filter)
			.map(f -> {
				File[] fileArray = new File[]{f};
				return fileArray;
			}).forEach(tableModel::addRow);

			//table.requestFocus();
			table.changeSelection(0, 0, false, false);

		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			update();
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			update();

		}

		@Override
		public void changedUpdate(DocumentEvent e) {
		}

	}

	private class EnterAction extends AbstractAction {

		JTable table;

		public EnterAction(JTable table) {
			this.table = table;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			int rowIndex = table.getSelectedRow();
			int columnIndex = table.getSelectedColumn();
			File f = (File)table.getModel().getValueAt(rowIndex, columnIndex);

	    	ProcessBuilder pb = new ProcessBuilder("cscript", "C:\\pleiades\\workspace\\jp.co.nri.nefs.tool.launcher\\conf\\activate.vbs", f.getPath());
	    	pb.command().stream().map(s -> s + " ").forEach(System.out::print);
	    	System.out.println();
	    	try {
				pb.start();
			} catch (IOException ie) {
				// TODO 自動生成された catch ブロック
				ie.printStackTrace();
			}
		}
	}

	private void init(){
		initFont();
		initCloseOperation();
		initTitle();
		JTable table = initPane(getContentPane());
		initSizeAndLocation();
		initSelection(table);
		initTaskTray();
	}

	private void initFont(){
		FontUIResource font = new FontUIResource("メイリオ", Font.PLAIN, 18);

		Enumeration<Object> keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if (value instanceof javax.swing.plaf.FontUIResource)
				UIManager.put(key, font);
		}
	}

	private void initCloseOperation(){
		// 登録されている任意の WindowListener オブジェクトを呼び出したあとで、自動的にフレームを隠す
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
	}

	private void initTitle(){
		setTitle("SmartLauncher");
	}

	private void initSizeAndLocation(){
		setSize(1000, 500);
		setLocationRelativeTo(null);
	}

	private class MyRenderer extends DefaultTableCellRenderer{
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {

			JLabel rendComp = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			File file = (File)value;
			String text = file.getName() + " - "  + file.getParent();
			rendComp.setText(text);
			return rendComp;
		}
	}

	private class MyTableModel extends DefaultTableModel {

		public MyTableModel(String[] columnNames, int rowCount) {
			super(columnNames, rowCount);
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return File.class;
		}
	}


	private JTable initPane(Container c){
		// 1行目
		//KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		//table.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(enter, solve);
		//table.getActionMap().put(solve, new EnterAction(table));

		JTextField textField = new JTextField(10);



		textField.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				// TODO 自動生成されたメソッド・スタブ

			}

			@Override
			public void keyReleased(KeyEvent e) {
				// TODO 自動生成されたメソッド・スタブ

			}

			@Override
			public void keyPressed(KeyEvent e) {
				// TODO 自動生成されたメソッド・スタブ

			}
		});

		// 2行目
		JLabel c2 = new JLabel(" ");

		// 3行目
		//DefaultTableModel tableModel = new DefaultTableModel(tabledata, columnNames);
		//tableModel = new DefaultTableModel(strs2, columnNames);
		//DefaultTableModel tableModel = new DefaultTableModel(new String[] {"file"}, 0);
		MyTableModel tableModel = new MyTableModel(new String[] {"file"}, 0);

		//tableModel.addRow(new String[]{"aaa"});
		/*fileList.stream().map(f -> {
			String[] starray = new String[1];
			starray[0] = f.getName();
			return starray;
		}).forEach(tableModel::addRow);*/

		fileList.stream().map(f -> {
			File[] fileArray = new File[1];
			fileArray[0] = f;
			return fileArray;
		}).forEach(tableModel::addRow);



		JTable table = new JTable(tableModel);
		table.setDefaultRenderer(File.class, new MyRenderer());
		table.setDefaultEditor(Object.class, null);
		final String solve = "Solve";
		KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		table.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(enter, solve);
		table.getActionMap().put(solve, new EnterAction(table));

		table.setRowHeight(25);
		table.setShowHorizontalLines(false);

		JScrollPane sp = new JScrollPane(table);
		sp.setPreferredSize(new Dimension(1000, 500));

		textField.getDocument().addDocumentListener(new MyDocmentListener(table, tableModel, textField));

		KeyStroke down = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0);
		textField.getInputMap(JTextField.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(down, "ABC");
		textField.getActionMap().put("ABC", new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Down pressed.");
				table.requestFocus();
			}
		});

		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					table.getActionMap().get(solve).actionPerformed(new ActionEvent(e.getSource(), e.getID(), e.paramString()));
				}
			}
		});




		// 4行目
		JButton okButton = new JButton("OK");
		JButton cancelButton = new JButton("Cancel");
		okButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
		cancelButton.setAlignmentX(Component.RIGHT_ALIGNMENT);

		JPanel p4 = new JPanel();
		p4.setLayout(new BoxLayout(p4, BoxLayout.LINE_AXIS));
		p4.add(okButton);
		p4.add(cancelButton);


		// 縦のBoxLayout
		JPanel panelV = new JPanel();
		panelV.setLayout(new BoxLayout(panelV, BoxLayout.PAGE_AXIS));
		panelV.add(textField);
		panelV.add(c2);
		panelV.add(sp);
		panelV.add(p4);

		c.add(panelV);

		return table;

	}

	private void initSelection(JTable table){
		//table.requestFocus();
		table.changeSelection(0, 0, false, false);

	}

	private void initTaskTray(){
        SystemTray tray = SystemTray.getSystemTray();
        PopupMenu popup = new PopupMenu();

        Image image = null;
		try {
			image = ImageIO.read(SmartFrame.class.getResourceAsStream("icon.png"));
		} catch (IOException e1) {
			// TODO 自動生成された catch ブロック
			e1.printStackTrace();
		}
        TrayIcon icon = new TrayIcon(image, "SmartLauncher", popup);
        icon.setImageAutoSize(true);

        MenuItem item1 = new MenuItem("Open");
        item1.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(true);
			}
		});

        MenuItem item2 = new MenuItem("Exit");
        item2.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				creator.stop();
				tray.remove(icon);
				setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				dispose();
			}
		});
        popup.add(item1);
        popup.add(item2);

        try {
        	tray.add(icon);
        } catch (AWTException e){
        	e.printStackTrace();
        }

	}

    public static void main( String[] args )
    {


        SmartFrame frame = new SmartFrame();
        frame.init();
        frame.pack();
        frame.setVisible(true);
    }
}
