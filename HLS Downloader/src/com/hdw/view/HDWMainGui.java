package com.hdw.view;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import com.phill.libs.*;
import com.hdw.model.*;
import com.hdw.controller.*;

public class HDWMainGui extends JFrame {

	// Serial
	private static final long serialVersionUID = 610724819466691396L;
	
	// Graphical attributes
	private final JTextField textURL;
	private final JTextField textOutputFile;
	private final JComboBox<String> comboResolution;
	private final ImageIcon loading = new ImageIcon(ResourceManager.getResource("icon/loading.gif"));
	
	// Creating custom colors
	private final Color rd_dk = new Color(0xBC1742);
	private final Color blue  = new Color(0x1F60CB);
	
	// Dynamic attributes
	private ArrayList<Chunklist> playlist;
	private File lastSelectedDir, outputFile;
	private JButton buttonURLPaste;
	private JButton buttonURLClear;
	private JButton buttonURLParse;
	private JLabel textLog;
	

	public static void main(String[] args) {
		new HDWMainGui();
	}

	public HDWMainGui() {
		super("HDW - build 20200914");
		
		// Retrieving graphical elements from 'res' directory
		//GraphicsHelper.setFrameIcon(this,"icon/icon.png");
		GraphicsHelper helper = GraphicsHelper.getInstance();
		Font   font = helper.getFont ();
		Color color = helper.getColor();
		
		Icon clearIcon    = ResourceManager.getResizedIcon("icon/clear.png",20,20);
		Icon downloadIcon = ResourceManager.getResizedIcon("icon/save.png",20,20);
		Icon exitIcon     = ResourceManager.getResizedIcon("icon/shutdown.png",20,20);
		Icon parseIcon    = ResourceManager.getResizedIcon("icon/cog.png",20,20);
		Icon pasteIcon    = ResourceManager.getResizedIcon("icon/clipboard_past.png",20,20);
		Icon selectIcon   = ResourceManager.getResizedIcon("icon/zoom.png",20,20);
		
		// Building UI
		Dimension dimension = new Dimension(1024,640);
		
		JPanel mainFrame = new JPaintedPanel("img/background.png",dimension);
		mainFrame.setLayout(null);
		setContentPane(mainFrame);
		
		JPanel panelURL = new JPanel();
		panelURL.setBorder(helper.getTitledBorder("HLS Playlist URL"));
		panelURL.setLayout(null);
		panelURL.setOpaque(false);
		panelURL.setBounds(12, 12, 1000, 75);
		mainFrame.add(panelURL);
		
		textURL = new JTextField();
		textURL.setToolTipText("Here goes the M3U playlist URL");
		textURL.setForeground(color);
		textURL.setFont(font);
		textURL.setColumns(10);
		textURL.setBounds(12, 30, 850, 25);
		panelURL.add(textURL);
		
		buttonURLPaste = new JButton(pasteIcon);
		buttonURLPaste.setToolTipText("Get link from clipboard");
		buttonURLPaste.addActionListener((event) -> textURL.setText(AlertDialog.copyFromClipboard()));
		buttonURLPaste.setBounds(875, 30, 30, 25);
		panelURL.add(buttonURLPaste);
		
		buttonURLClear = new JButton(clearIcon);
		buttonURLClear.setToolTipText("Clear");
		buttonURLClear.addActionListener((event) -> actionPlaylistClear());
		buttonURLClear.setBounds(915, 30, 30, 25);
		panelURL.add(buttonURLClear);
		
		buttonURLParse = new JButton(parseIcon);
		buttonURLParse.setToolTipText("Parse");
		buttonURLParse.addActionListener((event) -> actionPlaylistParse());
		buttonURLParse.setBounds(955, 30, 30, 25);
		panelURL.add(buttonURLParse);
		
		JPanel panelMedia = new JPanel();
		panelMedia.setOpaque(false);
		panelMedia.setBorder(helper.getTitledBorder("Media Selection"));
		panelMedia.setBounds(12, 90, 1000, 110);
		mainFrame.add(panelMedia);
		panelMedia.setLayout(null);
		
		JPanel panelResolution = new JPanel();
		panelResolution.setBorder(helper.getTitledBorder("Resolution"));
		panelResolution.setOpaque(false);
		panelResolution.setBounds(12, 25, 145, 70);
		panelMedia.add(panelResolution);
		panelResolution.setLayout(null);
		
		comboResolution = new JComboBox<String>();
		comboResolution.setBounds(12, 25, 115, 25);
		comboResolution.setFont(font);
		comboResolution.setForeground(color);
		panelResolution.add(comboResolution);
		
		JPanel panelOutput = new JPanel();
		panelOutput.setBorder(helper.getTitledBorder("Output File"));
		panelOutput.setOpaque(false);
		panelOutput.setBounds(169, 25, 819, 70);
		panelMedia.add(panelOutput);
		panelOutput.setLayout(null);
		
		textOutputFile = new JTextField();
		textOutputFile.setEditable(false);
		textOutputFile.setFont(font);
		textOutputFile.setForeground(color);
		textOutputFile.setBounds(12, 25, 713, 25);
		textOutputFile.setColumns(10);
		panelOutput.add(textOutputFile);
		
		JButton buttonOutputSelect = new JButton(selectIcon);
		buttonOutputSelect.addActionListener((event) -> actionOutputSelect());
		buttonOutputSelect.setToolTipText("Select file");
		buttonOutputSelect.setBounds(737, 25, 30, 25);
		panelOutput.add(buttonOutputSelect);
		
		JButton buttonOutputClear = new JButton(clearIcon);
		buttonOutputClear.addActionListener((event) -> actionOutputClear());
		buttonOutputClear.setToolTipText("Clear");
		buttonOutputClear.setBounds(777, 25, 30, 25);
		panelOutput.add(buttonOutputClear);
		
		JPanel panelConsole = new JPanel();
		panelConsole.setBorder(helper.getTitledBorder("Console"));
		panelConsole.setOpaque(false);
		panelConsole.setBounds(12, 205, 1000, 361);
		panelConsole.setLayout(null);
		mainFrame.add(panelConsole);
		
		JScrollPane scrollConsole = new JScrollPane();
		scrollConsole.setBounds(12, 25, 976, 324);
		panelConsole.add(scrollConsole);
		
		JTextArea textConsole = new JTextArea();
		textConsole.setBackground(Color.BLACK);
		textConsole.setForeground(Color.WHITE);
		textConsole.setFont(font);
		scrollConsole.setViewportView(textConsole);
		
		JButton buttonExit = new JButton(exitIcon);
		buttonExit.addActionListener((event) -> dispose());
		buttonExit.setToolTipText("Exit");
		buttonExit.setBounds(942, 578, 30, 25);
		mainFrame.add(buttonExit);
		
		JButton buttonDownload = new JButton(downloadIcon);
		buttonDownload.setToolTipText("Download media");
		buttonDownload.setBounds(982, 578, 30, 25);
		mainFrame.add(buttonDownload);
		
		textLog = new JLabel();
		textLog.setFont(font);
		textLog.setBounds(12, 578, 912, 25);
		mainFrame.add(textLog);
		
		setSize(dimension);
		setResizable(false);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		// Redirecting window closing event to a custom dispose() method, to prevent system instability
		addWindowListener(new WindowAdapter() {
		   public void windowClosing(WindowEvent event) {
		       dispose();
		}});
		
		setVisible(true);
		
	}
	
	/** Resets the entire screen and its internal references. */
	private void actionPlaylistClear() {
		
		// If a playlist was previously downloaded, a clear dialog is shown
		if (this.playlist != null) {
			
			String message = ResourceManager.getText(this,"playlist-clear-confirm.msg",0);
			int choice     = AlertDialog.dialog(message);
			
			// Breaks here when EXIT or CANCEL is selected
			if (choice != AlertDialog.OK_OPTION)
				return;
			
		}
		
		// Resetting parameters and unlocking panels, buttons, etc... 
		this.playlist = null;
		this.outputFile = null;
		
		this.textLog.setVisible(false);
		
		textOutputFile.setText(null);
		
		comboResolution.removeAllItems();
		utilLockMasterPanel(false);
		
		textURL.setText(null);
		textURL.requestFocus();
		
	}
	
	/** Downloads the EXTM3U playlist and parse its data. */
	private void actionPlaylistParse() {
		
		// Getting URL from text field
		final String  website = textURL.getText().trim();
		
		// This job needs to be run inside a thread, since it connects to the Internet
		Runnable job = () -> {
		
			try {
				
				// Updating UI
				utilLockMasterPanel(true);
				utilMessage("Downloading playlist...", blue, true);
				
				// Trying to download and parse the playlist object
				final URL playlistURL = new URL(website);
				final ArrayList<Chunklist> playlist = PlaylistParser.getConfig(playlistURL);
				
				// if I have a proper playlist...
				if (playlist != null) {
					
					utilMessage("Parsing playlist...", blue, true);
					
					// ...then I save it, ...
					this.playlist = playlist;
					
					// ...and fill the combobox.
					utilFillCombo();
					
					// When everything finishes, the label is hidden and the clear button shown. 
					utilHideMessage();
					SwingUtilities.invokeLater(() -> buttonURLClear.setEnabled(true));
					
				}
				
			}
			catch (MalformedURLException exception) {
				utilLockMasterPanel(false);
				utilMessage("Invalid playlist URL", rd_dk, false, 5);
			}
			catch (ConnectException exception) {
				utilLockMasterPanel(false);
				utilMessage("The server is refusing connections", rd_dk, false, 5);
			}
			catch (IOException exception) {
				utilLockMasterPanel(false);
				utilMessage(exception.getMessage(), rd_dk, false, 5);
			}
			catch (Exception exception) {
				exception.printStackTrace();
				utilLockMasterPanel(false);
				utilMessage("Unknown error occurred, please check the console", rd_dk, false, 10);
			}
		
		};
		
		// Doing the hard work
		Thread jsonParseThread = new Thread(job);
		jsonParseThread.setName("Playlist Parse Thread");
		jsonParseThread.start();
		
	}
	
	/** Fills the given 'comboBox' with information of each individual {@link Chunklist} provided through 'playlist'. */
	private void utilFillCombo() {
		
		SwingUtilities.invokeLater(() -> {
			
			comboResolution.removeAllItems();
			
			for (Chunklist chunklist: this.playlist)
				comboResolution.addItem(chunklist.getResolution());
		
		});
		
	}

	/** Sets visibility of the first panel components (panelURL).
	 *  @param lock - if 'true' then then components are locked. Otherwise, unlocked */
	private void utilLockMasterPanel(final boolean lock) {
		
		final boolean visibility = !lock;
		
		SwingUtilities.invokeLater(() -> {
		
			textURL       .setEditable(visibility);
			buttonURLPaste.setEnabled (visibility);
			buttonURLClear.setEnabled (visibility);
			buttonURLParse.setEnabled (visibility);
		
		});
		
	}
	
	/** Shows a message in the label designed for logging during a certain period of time.
	 *  @param message - the message to be displayed
	 *  @param color - the font color of the message
	 *  @param loading - if 'true' a loading gif is added to the beginning of the label
	 *  @param seconds - the amount of time to display the given message, before hiding it */
	private void utilMessage(final String message, final Color color, final boolean loading, int seconds) {
		
		// Starts a new thread to prevent the caller to wait for this method to end
		Runnable job = () -> {
			
			utilMessage(message,color,loading);
			
			try {
				Thread.sleep(seconds * 1000);
			}
			catch (InterruptedException exception) {
				
			}
			finally {
				utilHideMessage();
			}
			
		};
		
		Thread messageThread = new Thread(job);
		messageThread.setName("utilMessage() Thread");
		messageThread.start();
		
	}
	
	/** Shows a message in the label designed for logging.
	 *  @param message - the message to be displayed
	 *  @param color - the font color of the message
	 *  @param loading - if 'true' a loading gif is added to the beginning of the label */
	private void utilMessage(final String message, final Color color, final boolean loading) {
		
		Runnable job = () -> {
			textLog.setText(message);
			textLog.setForeground(color);
			textLog.setIcon(loading ? this.loading : null);
		};
		
		SwingUtilities.invokeLater(job);
	}
	
	/** Hides the label designed for logging. */
	private void utilHideMessage() {
		
		Runnable job = () -> {
			textLog.setText(null);
			textLog.setIcon(null);
		};
		
		SwingUtilities.invokeLater(job);
	}
	
	
	
	
	/** Shows a selection dialog for the output media file. */
	private void actionOutputSelect() {
		
		// Recovering the selected file
		final File file = FileChooserHelper.loadFile(this,FileFilters.MP4,"Select an output file",false,lastSelectedDir);
		
		// If something was selected...
		if (file != null) {
			
			// ((saving current directory info, to be used as suggestion by the JFileChooser later))
			this.lastSelectedDir = file.getParentFile();
			
			// ... and the file cannot be written, the code ends here
			if (!file.getParentFile().canWrite()) {
				
				String message = ResourceManager.getText(this,"output-select-read-only.msg",0);
				AlertDialog.erro(message);
				return;
				
			}
			
			// ... and if the file already exists, an overwrite dialog is shown.
			if (file.exists()) {
				
				String message = ResourceManager.getText(this,"output-select-override.msg",0);
				int choice = JOptionPane.showConfirmDialog(this,message);
				
				// If the user doesn't want to overwrite the selected file, the code ends here
				if (choice != JOptionPane.OK_OPTION)
					return;
				
			}
			
			// ... otherwise internal references and UI are updated
			this.outputFile = file;
			textOutputFile.setText(file.getAbsolutePath());
			
		}
		
	}
	
	/** Clears the output file internal references. */
	private void actionOutputClear() {
		
		textOutputFile.setText(null);
		this.outputFile = null;
		
	}
}
