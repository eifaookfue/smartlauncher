package jp.co.nri.nefs.tool.smartlauncher.action;

import static java.util.Comparator.*;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jp.co.nri.nefs.tool.smartlauncher.data.DataModelUpdater;

public class ReloadAction extends AbstractAction {

	private static final String separator = System.getProperty("line.separator");
	private JFrame frame;
	private DataModelUpdater dataModelUpdater;
	private Path directoryPath;
	private Path aliasPath;
	private Logger logger = LoggerFactory.getLogger(getClass());

	public ReloadAction(JFrame frame,  DataModelUpdater dataModelUpdater, Path directoryPath, Path aliasPath) {
		this.frame = frame;
		this.dataModelUpdater = dataModelUpdater;
		this.directoryPath = directoryPath;
		this.aliasPath = aliasPath;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		doActionPerformed();
	}

	public void doActionPerformed() {
		String errorMessage = "";
		try {
			Pair<List<File>, List<String>> pair = createList();
			dataModelUpdater.replaceList(pair.getLeft());

			List<String> errorList =  pair.getRight();
			if (errorList.size() > 0){
				errorMessage = "下記のディレクトリは存在しないのでスキップしました。" + separator;
				errorMessage += errorList.stream()
						.collect(Collectors.joining(separator));
			}

		} catch (IOException e1) {
			errorMessage += createExceptionMessage(e1);
		}

		try {
			dataModelUpdater.replaceAlias(createAlias());
		} catch (IOException e1) {
			errorMessage += createExceptionMessage(e1);
		}

		if (StringUtils.isNotEmpty(errorMessage)){
			JOptionPane.showMessageDialog(frame, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private String createExceptionMessage(Exception e){
		StringBuilder sb = new StringBuilder();
		sb.append("Exceptionが発生しました。");
		sb.append(separator);
		sb.append(e.toString());
		sb.append(separator);
		String stackTrace = Arrays.stream(e.getStackTrace()).map(s -> s.toString())
				.collect(Collectors.joining(separator));
		sb.append(stackTrace);
		sb.append(separator);
		return sb.toString();
	}

	private Pair<List<File>, List<String>> createList() throws IOException{

		ArrayList<File> normalList = new ArrayList<>();
		ArrayList<String> errorList = new ArrayList<>();

		AtomicInteger lineNumber = new AtomicInteger(1);
		Files.readAllLines(directoryPath).stream().forEach(str -> {
			if (!str.startsWith("#")){
				File dir = new File(str);
				if (dir.exists()){
					// ディレクトリは除外。更新時刻が新しい順
					Arrays.stream(dir.listFiles(f -> f.isFile()))
						.sorted(comparing(File::lastModified).reversed())
						.forEach(normalList::add);
				} else {
					errorList.add(lineNumber.get() + "行目:" + str);
				}
			}
			lineNumber.incrementAndGet();
		});
		return Pair.of(normalList, errorList);
	}

	private Map<String, String> createAlias() throws IOException {
		if (aliasPath != null) {
			return Files.readAllLines(aliasPath).stream().map(line -> line.split(","))
				.collect(Collectors.toMap(
					lines -> lines[0],
					lines -> lines[1]
							));
		}
		return null;
	}

}
