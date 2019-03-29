package jp.co.nri.nefs.tool.smartlauncher.action;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JTable;

public class ExecuteAction extends AbstractAction {

	JTable table;

	public ExecuteAction(JTable table) {
		this.table = table;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		int rowIndex = table.getSelectedRow();
		int columnIndex = table.getSelectedColumn();
		File f = (File) table.getModel().getValueAt(rowIndex, columnIndex);

		ProcessBuilder pb = new ProcessBuilder("cscript",
				"C:\\Users\\s2-nakamura\\git\\smartlauncher\\jp.co.nri.nefs.tool.smartlauncher\\conf\\activate.vbs",
				f.getPath());
		//pb.command().stream().collect(Collectors.joining(" "));
		System.out.println();
		try {
			pb.start();
		} catch (IOException ie) {
			// TODO 自動生成された catch ブロック
			ie.printStackTrace();
		}
	}



}
