package jp.co.nri.nefs.tool.smartlauncher.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JTextField;

public class ShiftTabAction extends AbstractAction {

	JTextField text;

	public ShiftTabAction(JTextField text) {
		this.text = text;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		text.requestFocus();
	}

}
