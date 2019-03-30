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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Objects;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.JIntellitype;

import jp.co.nri.nefs.tool.smartlauncher.action.EscAction;
import jp.co.nri.nefs.tool.smartlauncher.action.ExecuteAction;
import jp.co.nri.nefs.tool.smartlauncher.action.ExplorerAction;
import jp.co.nri.nefs.tool.smartlauncher.action.ShiftTabAction;
import jp.co.nri.nefs.tool.smartlauncher.data.DataModelUpdater;
import jp.co.nri.nefs.tool.smartlauncher.data.ScheduledCreator;

public class SmartFrame extends JFrame {
	private Image image;
	private ScheduledCreator creator;
	private Path directoryPath;
	// 当初はOptionalを利用しようと考えたが、ラムダ式のなかでExceptionをハンドリング
	// するとネストが深くなりすぎて汚くなるのでやめる。
	private Path aliasPath = null;
	private static Logger logger = LoggerFactory.getLogger(SmartFrame.class);

	public SmartFrame(String directoryFile, String aliasFile) {
		Objects.requireNonNull(directoryFile, "directoryPath is required.");
		directoryPath = Paths.get(directoryFile);
		if (aliasFile != null)
			aliasPath = Paths.get(aliasFile);
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

		// EscでフレームをInvisible
		String sfEsc = "SF_ESC";
		KeyStroke esc = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		getRootPane().getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(esc, sfEsc);
		getRootPane().getActionMap().put(sfEsc, new EscAction(this));

	}

	private void initTitle() {
		setTitle("SmartLauncher");
	}

	private void initSizeAndLocation() {
		//setPreferredSize(new Dimension(1000, 500));
		setSize(1000, 500);
		setLocationRelativeTo(null);
	}

	private JTable initPane(Container c) {
		// 1行目
		JTextField textField = new JTextField(10);
		// Frameを表示したときに全選択
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				textField.selectAll();
			}
		});

		// 2行目
		JLabel c2 = new JLabel(" ");

		// 3行目
		MyTableModel tableModel = new MyTableModel(new String[] { "file" }, 0);
		JTable table = new JTable(tableModel);
		table.setDefaultRenderer(File.class, new MyRenderer());
		table.setDefaultEditor(Object.class, null);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// Enter押下で実行
		final String sfEnter = "SF_ENTER";
		KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		table.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(enter, sfEnter);
		table.getActionMap().put(sfEnter, new ExecuteAction(this, table));
		textField.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(enter, sfEnter);
		textField.getActionMap().put(sfEnter, new ExecuteAction(this, table));

		// Shift+Enter押下でExplorer起動
		final String sfShiftEnter = "SF_SHIFT_ENTER";
		KeyStroke shiftEnter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_MASK);
		table.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(shiftEnter, sfShiftEnter);
		table.getActionMap().put(sfShiftEnter, new ExplorerAction(this, table));
		textField.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(shiftEnter, sfShiftEnter);
		textField.getActionMap().put(sfShiftEnter, new ExplorerAction(this, table));


		// Shift+TabでTextFieldに戻る
		KeyStroke tab = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_MASK);
		final String sfShiftTab = "SF_SHIFT_TAB";
		table.getInputMap(JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(tab, sfShiftTab);
		table.getActionMap().put(sfShiftTab, new ShiftTabAction(textField));

		table.setRowHeight(25);
		// 枠線非表示
		table.setShowHorizontalLines(false);
		// ヘッダ非表示
		table.setTableHeader(null);

		JScrollPane sp = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		//table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		//table.setPreferredSize(new Dimension(1000, 500));
		sp.setPreferredSize(new Dimension(1000, 500));

		DataModelUpdater dataModelUpdater = new DataModelUpdater(table, tableModel, textField);
		MyDocumentListener documentListener = new MyDocumentListener(dataModelUpdater);
		textField.getDocument().addDocumentListener(documentListener);

		creator = new ScheduledCreator(dataModelUpdater, directoryPath, aliasPath);
		try {
			//初期化
			dataModelUpdater.replaceAlias(creator.createAlias());
			dataModelUpdater.replaceList(creator.createList());
			dataModelUpdater.update();
		} catch (IOException e1) {
			// TODO 自動生成された catch ブロック
			e1.printStackTrace();
		}

		creator.start();

		KeyStroke down = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0);
		String sfDown = "SF_DOWN";
		textField.getInputMap(JTextField.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(down, sfDown);
		textField.getActionMap().put(sfDown, new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent e) {
				table.requestFocus();
			}
		});

		// ダブルクリックのときはEnterと同じ動作
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					if ( (e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0 )
						table.getActionMap().get(sfShiftEnter)
							.actionPerformed(new ActionEvent(e.getSource(), e.getID(), e.paramString()));
					else
						table.getActionMap().get(sfEnter)
						.actionPerformed(new ActionEvent(e.getSource(), e.getID(), e.paramString()));
				}
			}
		});

		// 4行目
		// https://ateraimemo.com/Swing/ButtonWidth.html
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ExecuteAction(this, table));
		okButton.setPreferredSize(new Dimension(120, 30));
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new EscAction(this));
		cancelButton.setPreferredSize(new Dimension(120, 30));
		/*okButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
		cancelButton.setAlignmentX(Component.LEFT_ALIGNMENT);*/

		// BoxLayout.Y_AXIS 上から下にコンポーネントを配置
		// BoxLayout.LINE_AXISの場合が「左から右」
		// BoxLayout.PAGE_AXISの場合が「上から下」
		/*JPanel p4 = new JPanel();
		p4.setLayout(new BoxLayout(p4, BoxLayout.Y_AXIS));
		p4.add(okButton);*/

		Box box = Box.createHorizontalBox();
		box.add(Box.createHorizontalGlue());
		box.add(okButton);
		box.add(Box.createHorizontalStrut(20));
		box.add(cancelButton);
		box.setBorder(BorderFactory.createEmptyBorder(5,0,5,30));


		// 縦のBoxLayout
		JPanel panelV = new JPanel();
		panelV.setLayout(new BoxLayout(panelV, BoxLayout.PAGE_AXIS));
		panelV.add(textField);
		panelV.add(c2);
		panelV.add(sp);
		//panelV.add(p4);
		panelV.add(box);

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

	public static void main(String[] args) {

		String directoryFile = null;
		String aliasFile = null;
		for (int i = 0; i < args.length; ++i){
			if ("-directoryFile".equals(args[i])){
				directoryFile = args[++i];
			} else if ("-aliasFile".equals(args[i])) {
				aliasFile = args[++i];
			}
		}

		SmartFrame frame = new SmartFrame(directoryFile, aliasFile);
		frame.init();
		frame.pack();


		//JIntellitype.getInstance().registerHotKey(1, JIntellitype.MOD_CONTROL + JIntellitype.MOD_SHIFT, (int)'B');
		JIntellitype.getInstance().registerHotKey(1, JIntellitype.MOD_CONTROL + JIntellitype.MOD_SHIFT, KeyEvent.VK_5);
		JIntellitype.getInstance().addHotKeyListener(new HotkeyListener() {

			@Override
			public void onHotKey(int paramInt) {
				if (paramInt == 1){
					frame.setVisible(true);
					logger.info("HotKey called.");
				}

			}
		});

		//frame.setVisible(true);

	}
}
