package jp.co.nri.nefs.tool.smartlauncher.action;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JTable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ExecuteAction extends AbstractAction {

	private JFrame frame;
	private JTable table;
	private Logger logger = LoggerFactory.getLogger(getClass());
	private String script;

	public ExecuteAction(JFrame frame, JTable table) {
		this.frame = frame;
		this.table = table;
		try {
			script = Paths.get(
					ExecuteAction.class.getClassLoader()
					.getResource("scripts/activate.vbs")
					.toURI()
					).toString();
		} catch (URISyntaxException e) {
			logger.warn("", e);
		}

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		int rowIndex = table.getSelectedRow();
		int columnIndex = table.getSelectedColumn();
		File f = (File) table.getModel().getValueAt(rowIndex, columnIndex);

		ProcessBuilder pb = new ProcessBuilder("cscript", script, '"' + f.getPath() + '"');
		String called = pb.command().stream().collect(Collectors.joining(" "));
		logger.info("called {}", called);

		try {
			pb.start();
		} catch (IOException ie) {
			logger.warn("process起動失敗", ie);
		}
		frame.setVisible(false);
	}



}
