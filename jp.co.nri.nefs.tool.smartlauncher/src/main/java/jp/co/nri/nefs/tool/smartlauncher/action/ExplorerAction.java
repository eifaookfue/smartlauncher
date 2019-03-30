package jp.co.nri.nefs.tool.smartlauncher.action;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JTable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ExplorerAction extends AbstractAction {

	private JFrame frame;
	private JTable table;
	private Logger logger = LoggerFactory.getLogger(getClass());

	public ExplorerAction(JFrame frame, JTable table) {
		this.frame = frame;
		this.table = table;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		int rowIndex = table.getSelectedRow();
		int columnIndex = table.getSelectedColumn();
		File f = (File) table.getModel().getValueAt(rowIndex, columnIndex);

		ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "explorer", f.getParent());
		String called = pb.command().stream().collect(Collectors.joining(" "));
		logger.info("called {}", called);

		try {
			pb.start();
		} catch (IOException ie) {
			logger.warn("Explorer起動失敗", ie);
		}
		frame.setVisible(false);
	}



}
