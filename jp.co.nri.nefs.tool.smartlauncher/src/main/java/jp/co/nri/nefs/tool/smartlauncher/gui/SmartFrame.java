package jp.co.nri.nefs.tool.smartlauncher.gui;

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
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;

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
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.JIntellitype;

import jp.co.nri.nefs.tool.smartlauncher.action.ExecuteAction;
import jp.co.nri.nefs.tool.smartlauncher.action.ShiftTabAction;
import jp.co.nri.nefs.tool.smartlauncher.data.DataModelUpdater;
import jp.co.nri.nefs.tool.smartlauncher.data.FileListCreator;

public class SmartFrame extends JFrame {
	private Image image;
	private FileListCreator creator;
	private Path directoryFile;

	public SmartFrame() {
		directoryFile = Paths.get(
				"C:\\Users\\s2-nakamura\\git\\smartlauncher\\jp.co.nri.nefs.tool.smartlauncher\\conf\\searchdir.txt");
	}

	private void init() {
		initIcon();
		initFont();
		initCloseOperation();
		initTitle();
		JTable table = initPane(getContentPane());
		initSizeAndLocation();
		initSelection(table);
		initTaskTray();
	}

	private void initIcon() {
		try {
			image = ImageIO.read(SmartFrame.class.getClassLoader().getResourceAsStream("launcher-icon.png"));
			setIconImage(image);
		} catch (IOException e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	private void initFont() {
		FontUIResource font = new FontUIResource("メイリオ", Font.PLAIN, 18);

		Enumeration<Object> keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if (value instanceof javax.swing.plaf.FontUIResource)
				UIManager.put(key, font);
		}
	}

	private void initCloseOperation() {
		// 登録されている任意の WindowListener オブジェクトを呼び出したあとで、自動的にフレームを隠す
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
	}

	private void initTitle() {
		setTitle("SmartLauncher");
	}

	private void initSizeAndLocation() {
		setSize(1000, 500);
		setLocationRelativeTo(null);
	}

	private class MyRenderer extends DefaultTableCellRenderer {
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {

			JLabel rendComp = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
					column);
			File file = (File) value;
			String text = file.getName() + " - " + file.getParent();
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

	private JTable initPane(Container c) {
		// 1行目
		JTextField textField = new JTextField(10);

		// 2行目
		JLabel c2 = new JLabel(" ");

		// 3行目
		MyTableModel tableModel = new MyTableModel(new String[] { "file" }, 0);


		/*fileList.stream().map(f -> {
			File[] fileArray = new File[1];
			fileArray[0] = f;
			return fileArray;
		}).forEach(tableModel::addRow);*/

		JTable table = new JTable(tableModel);
		table.setDefaultRenderer(File.class, new MyRenderer());
		table.setDefaultEditor(Object.class, null);
		final String solve = "Solve";
		KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		table.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(enter, solve);
		table.getActionMap().put(solve, new ExecuteAction(table));
		KeyStroke tab = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_MASK);
		table.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(tab, "TTT");
		table.getActionMap().put("TTT", new ShiftTabAction(textField));

		table.setRowHeight(25);
		table.setShowHorizontalLines(false);

		JScrollPane sp = new JScrollPane(table);
		sp.setPreferredSize(new Dimension(1000, 500));

		DataModelUpdater dataModelUpdater = new DataModelUpdater(table, tableModel, textField);
		MyDocumentListener documentListener = new MyDocumentListener(dataModelUpdater);
		textField.getDocument().addDocumentListener(documentListener);

		creator = new FileListCreator(dataModelUpdater, directoryFile);
		try {
			//初期化
			dataModelUpdater.replaceList(creator.createList());
			dataModelUpdater.update();
		} catch (IOException e1) {
			// TODO 自動生成された catch ブロック
			e1.printStackTrace();
		}

		creator.start();


		KeyStroke down = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0);
		textField.getInputMap(JTextField.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(down, "ABC");
		textField.getActionMap().put("ABC", new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Down pressed.");
				table.requestFocus();
			}
		});
		//Arrays.stream(textField.getActionMap().allKeys()).forEach(System.out::println);
		//textField.getActionMap().get("insert-content").actionPerformed(new ActionEvent(null, null, null));

		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					table.getActionMap().get(solve)
							.actionPerformed(new ActionEvent(e.getSource(), e.getID(), e.paramString()));
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

	private void initSelection(JTable table) {
		// table.requestFocus();
		table.changeSelection(0, 0, false, false);

	}

	private void initTaskTray() {
		SystemTray tray = SystemTray.getSystemTray();
		PopupMenu popup = new PopupMenu();

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
				JIntellitype.getInstance().cleanUp();
				setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				dispose();
			}
		});
		popup.add(item1);
		popup.add(item2);

		try {
			tray.add(icon);
		} catch (AWTException e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		SmartFrame frame = new SmartFrame();
		frame.init();
		frame.pack();


		//JIntellitype.getInstance().registerHotKey(1, JIntellitype.MOD_CONTROL + JIntellitype.MOD_SHIFT, (int)'B');
		JIntellitype.getInstance().registerHotKey(1, JIntellitype.MOD_CONTROL + JIntellitype.MOD_SHIFT, KeyEvent.VK_5);
		JIntellitype.getInstance().addHotKeyListener(new HotkeyListener() {

			@Override
			public void onHotKey(int paramInt) {
				if (paramInt == 1){
					frame.setVisible(true);
					System.out.println("called");
				}

			}
		});

		//frame.setVisible(true);

	}
}
