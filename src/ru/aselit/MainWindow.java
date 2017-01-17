package ru.aselit;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import ru.aselit.TransferThread.TransferThreadInterface;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;

public class MainWindow implements TransferThreadInterface {

	protected Shell shell;
	private Table tableTransferList;
	
	private TransferThread transferThread = null;
	
	/**
	 * Open the window.
	 */
	public void open() {
		
		Display display = Display.getDefault();
		
		createContents();
		
//		start a file transfer thread
		transferThread = new TransferThread(this);
		
		shell.open();
		shell.layout();
		
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 * @wbp.parser.entryPoint
	 */
	protected void createContents() {
		
		FormLayout layout = new FormLayout();
		
		shell = new Shell();
		shell.addShellListener(new ShellAdapter() {
			@Override
			public void shellClosed(ShellEvent arg0) {
			
				if (null != transferThread)
					transferThread.interrupt();
			}
		});
		
		shell.setSize(750, 450);
		shell.setText("File transfer client");
		shell.setLayout(layout);
		
		tableTransferList = new Table(shell, SWT.BORDER | SWT.FULL_SELECTION);
		tableTransferList.setHeaderVisible(true);
		tableTransferList.setLinesVisible(true);
		
		TableColumn tblclmnIndex = new TableColumn(tableTransferList, SWT.NONE);
		tblclmnIndex.setWidth(100);
		tblclmnIndex.setText("Index");
		
		TableColumn tblclmnFile = new TableColumn(tableTransferList, SWT.NONE);
		tblclmnFile.setWidth(300);
		tblclmnFile.setText("File");
		
		TableColumn tblclmnFileState = new TableColumn(tableTransferList, SWT.NONE);
		tblclmnFileState.setWidth(100);
		tblclmnFileState.setText("File state");
		
		TableColumn tblclmnProgress = new TableColumn(tableTransferList, SWT.CENTER);
		tblclmnProgress.setWidth(60);
		tblclmnProgress.setText("Progress");
		
		TableColumn tblclmnTCPState = new TableColumn(tableTransferList, SWT.NONE);
		tblclmnTCPState.setWidth(100);
		tblclmnTCPState.setText("TCP state");
		
		Button btnClose = new Button(shell, SWT.NONE);
		btnClose.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
			
				shell.close();
			}
		});
		btnClose.setText("Close");
		
		Button btnSettings = new Button(shell, SWT.NONE);
		btnSettings.setText("Settings");

		Button btnAddFile = new Button(shell, SWT.NONE);
		btnAddFile.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
			
				FileDialog fd = new FileDialog(shell, SWT.OPEN);
		        fd.setText("Select a file");
		        fd.setFilterPath("C:\\");
		        String[] filterExt = {"*.*"};
		        fd.setFilterExtensions(filterExt);
		        String sourceFile = fd.open();
		        if (null == sourceFile)
		        	return;
		        transferThread.addFile(sourceFile, "");
			}
		});
		btnAddFile.setText("Add file");
		
		FormData fd;
		
		fd = new FormData();
		fd.left = new FormAttachment(0, 6);
		fd.bottom = new FormAttachment(100, -6);
		btnSettings.setLayoutData(fd);
		
		fd = new FormData();
		fd.left = new FormAttachment(btnSettings, 6);
		fd.bottom = new FormAttachment(100, -6);
		btnAddFile.setLayoutData(fd);
		
		fd = new FormData();
		fd.right = new FormAttachment(100, -6);
		fd.bottom = new FormAttachment(100, -6);
		btnClose.setLayoutData(fd);
		
		fd = new FormData();
		fd.left = new FormAttachment(0, 6);
		fd.right = new FormAttachment(100, -6);
		fd.top = new FormAttachment(0, 6);
		fd.bottom = new FormAttachment(btnSettings, -6);
		tableTransferList.setLayoutData(fd);
	}

	/**
	 * 
	 * @param index
	 * @return
	 */
	private TableItem getTableItem(long id) {
		
		for (int i = 0; i < tableTransferList.getItemCount(); i++) {
			
			TableItem item = tableTransferList.getItem(i);
			if (new Long(item.getText(0)).equals(id))
				return item;
		}
		return null;
	}
	
	@Override
	public void showTransferList(TransferList list) {
		
		Display.getDefault().asyncExec(new Runnable() {
		    
			public void run() {
				
				int i;
				TableItem tableItem;
				List<TableItem> usedItems = new ArrayList<TableItem>();
				
				for (i = 0; i < list.size(); i++) {
					
					TransferItem item = list.get(i);
					
					tableItem = getTableItem(item.getId());
					if (null == tableItem) {
						
						tableItem = new TableItem(tableTransferList, SWT.NONE);
						tableItem.setText(0, String.format("%d", item.getId()));
						tableItem.setText(1, item.getSourceFile());
						tableItem.setText(3, "0%");
					}
					usedItems.add(tableItem);
					
					tableItem.setText(2, TransferItemStateEnum.toString(item.getState()));
					tableItem.setText(4, FileTransferCommandEnum.toString(item.getThreadState()));
				}
				
//				delete unused items from table
				for (i = tableTransferList.getItemCount() - 1; i >= 0; i--) {
					
					tableItem = tableTransferList.getItem(i);
					if (-1 != usedItems.indexOf(tableItem))
						continue;
					tableTransferList.remove(i);
				}
				
				usedItems = null;
			}
		});
	}
}
