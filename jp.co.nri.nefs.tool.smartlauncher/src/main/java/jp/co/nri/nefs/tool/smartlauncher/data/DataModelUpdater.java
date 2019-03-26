package jp.co.nri.nefs.tool.smartlauncher.data;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.lang.StringUtils;

public class DataModelUpdater {

	private JTable table;
	private DefaultTableModel tableModel;
	private JTextField textField;
	private List<File> fileList;

	public DataModelUpdater(JTable table, DefaultTableModel tableModel, JTextField textField) {
		this.table = table;
		this.tableModel = tableModel;
		this.textField = textField;
	}

	public void replaceList(List<File> newList) {
		SwingUtilities.invokeLater(() -> {
			this.fileList = newList;
		});
	}


	public void update() {
		// いったん全部TableModelから削除
		if (tableModel.getRowCount() > 0) {
			for (int i = tableModel.getRowCount() - 1; i > -1; i--) {
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
			return Arrays.stream(keys).allMatch(key -> StringUtils.containsIgnoreCase(file.getName(), key));
		};

		/*
		 * fileList.stream().filter(filter) .map(f -> { String[] starray =
		 * new String[1]; starray[0] = f.getName(); return starray;
		 * }).forEach(tableModel::addRow);
		 */

		fileList.stream().filter(filter).map(f -> {
			File[] fileArray = new File[] { f };
			return fileArray;
		}).forEach(tableModel::addRow);

		// table.requestFocus();
		table.changeSelection(0, 0, false, false);

	}

}
