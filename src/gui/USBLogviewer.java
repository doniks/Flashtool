package gui;

import gui.models.TableLine;
import gui.models.TableSorter;
import gui.models.VectorContentProvider;
import gui.models.VectorLabelProvider;
import gui.tools.USBParseJob;
import gui.tools.WidgetsTool;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Table;
import org.simpleusblogger.S1Packet;
import org.system.DeviceEntry;
import org.system.Devices;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class USBLogviewer extends Dialog {

	protected Object result;
	protected Shell shlUSBLogviewer;
	private Table table;
	private TableViewer tableViewer;
	private Text textSinFolder;
	private Button btnCancel;
	private Composite compositeTable;
	private final FormToolkit formToolkit = new FormToolkit(Display.getDefault());
	private Label lblLogfile;
	private Text textLogFile;
	private Button btnParse;
	private Button btnLogFile;
	private Button btnSourceFolder;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public USBLogviewer(Shell parent, int style) {
		super(parent, style);
		setText("Device Selector");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		
		createContents();
		createTriggers();
		
		WidgetsTool.setSize(shlUSBLogviewer);
		
		
		shlUSBLogviewer.open();
		shlUSBLogviewer.layout();
		
		Display display = getParent().getDisplay();
		while (!shlUSBLogviewer.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		
		return result;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shlUSBLogviewer = new Shell(getParent(), getStyle());
		shlUSBLogviewer.setSize(710, 475);
		shlUSBLogviewer.setText("USB Log Viewer");
		shlUSBLogviewer.setLayout(new FormLayout());
		
		btnCancel = new Button(shlUSBLogviewer, SWT.NONE);
		FormData fd_btnCancel = new FormData();
		fd_btnCancel.bottom = new FormAttachment(100, -10);
		fd_btnCancel.right = new FormAttachment(100, -10);
		btnCancel.setLayoutData(fd_btnCancel);
		btnCancel.setText("Cancel");
		
		compositeTable = new Composite(shlUSBLogviewer, SWT.NONE);
		compositeTable.setLayout(new FillLayout(SWT.HORIZONTAL));
		FormData fd_compositeTable = new FormData();
		fd_compositeTable.bottom = new FormAttachment(btnCancel, -6);
		fd_compositeTable.right = new FormAttachment(btnCancel, 0, SWT.RIGHT);
		fd_compositeTable.left = new FormAttachment(0, 10);
		fd_compositeTable.top = new FormAttachment(0, 98);
		compositeTable.setLayoutData(fd_compositeTable);
		
		tableViewer = new TableViewer(compositeTable,SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.SINGLE);
		tableViewer.setContentProvider(new VectorContentProvider());
		tableViewer.setLabelProvider(new VectorLabelProvider());

		table = tableViewer.getTable();
		TableColumn[] columns = new TableColumn[3];
		columns[0] = new TableColumn(table, SWT.NONE);
		columns[0].setText("Direction");
		columns[1] = new TableColumn(table, SWT.NONE);
		columns[1].setText("Action");
		columns[2] = new TableColumn(table, SWT.NONE);
		columns[2].setText("Value");
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		TableSorter sort = new TableSorter(tableViewer);
		Composite composite = new Composite(shlUSBLogviewer, SWT.NONE);
		composite.setLayout(new GridLayout(3, false));
		FormData fd_composite = new FormData();
		fd_composite.bottom = new FormAttachment(compositeTable, -23);
		fd_composite.top = new FormAttachment(0, 10);
		fd_composite.left = new FormAttachment(0, 10);
		composite.setLayoutData(fd_composite);
		
		lblLogfile = formToolkit.createLabel(composite, "USB Log file :", SWT.NONE);
		
		textLogFile = new Text(composite, SWT.BORDER);
		textLogFile.setEditable(false);
		textLogFile.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		formToolkit.adapt(textLogFile, true, true);
		
		btnLogFile = new Button(composite, SWT.NONE);
		btnLogFile.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		formToolkit.adapt(btnLogFile, true, true);
		btnLogFile.setText("...");
		
		Label lblSinfolder = new Label(composite, SWT.NONE);
		GridData gd_lblSinfolder = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_lblSinfolder.widthHint = 93;
		lblSinfolder.setLayoutData(gd_lblSinfolder);
		lblSinfolder.setText("Source folder :");
		
		textSinFolder = new Text(composite, SWT.BORDER);
		textSinFolder.setEditable(false);
		GridData gd_textSinFolder = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_textSinFolder.widthHint = 513;
		textSinFolder.setLayoutData(gd_textSinFolder);
		
		btnSourceFolder = new Button(composite, SWT.NONE);
		GridData gd_btnSourceFolder = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_btnSourceFolder.widthHint = 46;
		btnSourceFolder.setLayoutData(gd_btnSourceFolder);
		btnSourceFolder.setText("...");
		btnParse = new Button(shlUSBLogviewer, SWT.NONE);
		FormData fd_btnParse = new FormData();
		fd_btnParse.bottom = new FormAttachment(btnCancel, 0, SWT.BOTTOM);
		fd_btnParse.right = new FormAttachment(btnCancel, -6);
		btnParse.setLayoutData(fd_btnParse);
		formToolkit.adapt(btnParse, true, true);
		btnParse.setText("Parse");

	}

	public void createTriggers() {
		
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				result = null;
				shlUSBLogviewer.dispose();
			}
		});

		table.addListener(SWT.DefaultSelection, new Listener() {
		      public void handleEvent(Event e) {
		        TableItem[] selection = table.getSelection();
		        String string = selection[0].getText(0);
		        result = string;
		        shlUSBLogviewer.dispose();
		      }
		    });

		btnSourceFolder.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dlg = new DirectoryDialog(shlUSBLogviewer);

		        // Set the initial filter path according
		        // to anything they've selected or typed in
		        dlg.setFilterPath(textSinFolder.getText());

		        // Change the title bar text
		        dlg.setText("Directory chooser");

		        // Customizable message displayed in the dialog
		        dlg.setMessage("Select a directory");

		        // Calling open() will open and run the dialog.
		        // It will return the selected directory, or
		        // null if user cancels
		        String dir = dlg.open();
		        if (dir != null) {
		          // Set the text box to the new selection
		        	if (!textSinFolder.getText().equals(dir)) {
		        		textSinFolder.setText(dir);
		        	}
		        }
			}
		});

		btnLogFile.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dlg = new FileDialog(shlUSBLogviewer);

		        // Set the initial filter path according
		        // to anything they've selected or typed in
		        dlg.setFilterPath(textLogFile.getText());

		        // Change the title bar text
		        dlg.setText("TMS File chooser");

		        dlg.setFilterExtensions(new String[] {"*.tms"});

		        // Calling open() will open and run the dialog.
		        // It will return the selected directory, or
		        // null if user cancels
		        String dir = dlg.open();
		        if (dir != null) {
		          // Set the text box to the new selection
		        	if (!textLogFile.getText().equals(dir)) {
		        		textLogFile.setText(dir);
		        	}
		        }
			}
		});

		btnParse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				USBParseJob pj = new USBParseJob("USB log parser");
				pj.setFilename(textLogFile.getText());
				pj.setSinDir(textSinFolder.getText());
				pj.addJobChangeListener(new IJobChangeListener() {
					public void aboutToRun(IJobChangeEvent event) {
					}

					public void awake(IJobChangeEvent event) {
					}

					public void done(IJobChangeEvent event) {
						Vector result = new Vector();
						Iterator<S1Packet> i = pj.getSession().iterator();
						while (i.hasNext()) {
							S1Packet p = i.next();
					    	TableLine line = new TableLine();
					    	line.add(p.getDirection());
					    	line.add(p.getCommandName());
					    	line.add(p.getInfo());
					    	result.add(line);
						}
						Display.getDefault().asyncExec(
								new Runnable() {
									public void run() {
										tableViewer.setInput(result);
									    for (int nbcols=0;nbcols<table.getColumnCount();nbcols++)
									    	table.getColumn(nbcols).pack();
									    tableViewer.refresh();
									}
								}
						);

					}

					public void running(IJobChangeEvent event) {
					}

					public void scheduled(IJobChangeEvent event) {
					}

					public void sleeping(IJobChangeEvent event) {
					}
				});

				pj.schedule();

			}
		});

	}
}
