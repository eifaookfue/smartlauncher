package jp.co.nri.nefs.tool.smartlauncher.gui;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import jp.co.nri.nefs.tool.smartlauncher.data.DataModelUpdater;

public class MyDocumentListener implements DocumentListener {

	DataModelUpdater dataModelUpdater;

	public MyDocumentListener(DataModelUpdater dataModelUpdater) {
		this.dataModelUpdater = dataModelUpdater;
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		dataModelUpdater.update();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		dataModelUpdater.update();
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
	}

}
