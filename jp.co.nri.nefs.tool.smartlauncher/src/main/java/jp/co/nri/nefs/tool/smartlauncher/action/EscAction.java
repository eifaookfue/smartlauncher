package jp.co.nri.nefs.tool.smartlauncher.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EscAction extends AbstractAction {

	private JFrame frame;
	private Logger logger = LoggerFactory.getLogger(getClass());

	public EscAction(JFrame frame) {
		this.frame = frame;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		logger.info("Esc pressed");
		frame.setVisible(false);
	}

}
