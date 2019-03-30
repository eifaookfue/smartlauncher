package jp.co.nri.nefs.tool.smartlauncher.data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataModelUpdater {

	private JTable table;
	private DefaultTableModel tableModel;
	private JTextField textField;
	private List<File> fileList;
	private Map<String, String> alias = null;
	private Logger logger = LoggerFactory.getLogger(getClass());

	public DataModelUpdater(JTable table, DefaultTableModel tableModel, JTextField textField, Path aliasFile) {
		this.table = table;
		this.tableModel = tableModel;
		this.textField = textField;

		if (aliasFile != null) {
			try {
				alias = Files.readAllLines(aliasFile).stream().map(line -> line.split(","))
					.collect(Collectors.toMap(
						lines -> lines[0],
						lines -> lines[1]
								));
			} catch (IOException e) {
				logger.warn("", e);
			}
		}
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

		// エイリアスに登録されていれば変換
		String text = textField.getText();
		if (alias != null){
			String org = text;
			text = alias.getOrDefault(text, text);
			logger.debug("{} has converted to {}", org, text);
		}

		// 半角スペースもしくは全角スペースで分割
		String[] keys = text.split("\\s|　");

		// スペースで区切られたkeyのすべてにマッチした場合
		Predicate<File> filter = file -> {
			return Arrays.stream(keys).allMatch(key -> StringUtils.containsIgnoreCase(file.getName(), key));
		};

		fileList.stream().filter(filter).map(f -> {
			File[] fileArray = new File[] { f };
			return fileArray;
		}).forEach(tableModel::addRow);

		table.changeSelection(0, 0, false, false);

	}

}
