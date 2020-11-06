package com.hdw.view;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

import javax.swing.*;

import org.apache.commons.io.FileUtils;

import net.bramp.ffmpeg.*;
import net.bramp.ffmpeg.job.*;
import net.bramp.ffmpeg.probe.*;
import net.bramp.ffmpeg.builder.*;
import net.bramp.ffmpeg.progress.*;

import com.phill.libs.*;
import com.phill.libs.files.PhillFileUtils;
import com.phill.libs.i18n.PropertyBundle;
import com.phill.libs.i18n.PropertyBundleFiller;
import com.phill.libs.ui.*;
import com.phill.libs.sys.ClipboardUtils;
import com.hdw.model.*;
import com.hdw.controller.*;

/** Implements the main User Interface and all its functionalities.
 *  @author Felipe André - felipeandresouza@hotmail.com
 *  @version 2.12 - 05/11/2020 */
public class HDWMainGui extends JFrame {

	// Serial
	private static final long serialVersionUID = 610724819466691396L;
	
	// Graphical attributes
	
	private final GraphicsHelper helper = GraphicsHelper.getInstance();
	
	private final JTextField textURL;
	private final JTextField textOutputFile;
	private final JComboBox<String> comboResolution;
	private final JButton buttonURLPaste, buttonURLClear, buttonURLParse;
	private final JButton buttonOutputSelect, buttonOutputClear;
	private final JButton buttonDownload, buttonCancel, buttonExit;
	private final JPanel panelLanguages, panelURL, panelMedia, panelResolution, panelOutput, panelConsole;
	private final JButton buttonBrazil, buttonUSA, buttonJapan;
	private final JLabel labelDuration, labelLog;
	private final JTextArea textConsole;
	private final JProgressBar progressDownload;
	private JMenuItem itemSave, itemClear;
	
	private final ImageIcon loading = new ImageIcon(ResourceManager.getResource("icon/loading.gif"));
	
	// Creating custom colors
	private final Color gr_dk = new Color(0x0D6B12);
	private final Color rd_dk = new Color(0xBC1742);
	private final Color blue  = new Color(0x1F60CB);
	private final Color bl_lt = new Color(0x3291A8);
	private final Color yl_dk = new Color(0xD2E86D);
	
	// Dynamic attributes
	private ArrayList<Chunklist> playlist;
	private File lastSelectedDir, outputFile;
	private Thread downloaderThread;
	private FFmpeg ffmpeg;
	
	private final PropertyBundle bundle;
	private final PropertyBundleFiller filler;

	/** Builds the graphical interface and its functionalities */
	public HDWMainGui() {
		super("HDW - build 20201105");
		
		// Loading available locales
		HashMap<String, Locale> locales = new HashMap<String, Locale>(2);
		locales.put("en_US",Locale.ENGLISH);
		locales.put("pt_BR", new Locale("pt","BR"));
		locales.put("ja_JP", new Locale("ja","JP"));
		
		// 
		this.bundle = new PropertyBundle("i18n/titles", null);
		this.filler = new PropertyBundleFiller(this.bundle);
		
		// Retrieving graphical elements from 'res' directory
		GraphicsHelper.setFrameIcon(this,"icon/icon.png");
		Font   font = helper.getFont ();
		Color color = helper.getColor();
		
		Icon cancelIcon   = ResourceManager.getIcon("icon/cancel.png",20,20);
		Icon clearIcon    = ResourceManager.getIcon("icon/clear.png",20,20);
		Icon downloadIcon = ResourceManager.getIcon("icon/save.png",20,20);
		Icon exitIcon     = ResourceManager.getIcon("icon/shutdown.png",20,20);
		Icon parseIcon    = ResourceManager.getIcon("icon/cog.png",20,20);
		Icon pasteIcon    = ResourceManager.getIcon("icon/clipboard_past.png",20,20);
		Icon selectIcon   = ResourceManager.getIcon("icon/zoom.png",20,20);
		Icon openDirIcon  = ResourceManager.getIcon("icon/folder_open.png",20,20);
		
		// Building UI
		Dimension dimension = new Dimension(1045,660);
		
		JPanel mainFrame = new JPaintedPanel("img/background.png",dimension);
		mainFrame.setLayout(null);
		setContentPane(mainFrame);
		
		Icon brazilFlag = ResourceManager.getIcon("icon/brazil-flag.png",35,35);
		Icon usaFlag    = ResourceManager.getIcon("icon/usa-flag.png",35,35);
		Icon japanFlag  = ResourceManager.getIcon("icon/japan-flag.png",40,40);
		
		panelLanguages = new JPanel();
		panelLanguages.setOpaque(false);
		panelLanguages.setBounds(12, 12, 185, 75);
		panelLanguages.setLayout(null);
		filler.setBorder(panelLanguages, "panel-languages");
		mainFrame.add(panelLanguages);
		
		buttonBrazil = new JButton(brazilFlag);
		buttonBrazil.addActionListener((event) -> this.bundle.changeLocale(locales.get("pt_BR")));
		buttonBrazil.setContentAreaFilled(false);
		buttonBrazil.setBounds(12, 25, 45, 40);
		filler.setToolTipText(buttonBrazil, "button-brazil");
		panelLanguages.add(buttonBrazil);
		
		buttonUSA = new JButton(usaFlag);
		buttonUSA.addActionListener((event) -> this.bundle.changeLocale(locales.get("en_US")));
		buttonUSA.setContentAreaFilled(false);
		buttonUSA.setBounds(69, 25, 45, 40);
		filler.setToolTipText(buttonUSA, "button-usa");
		panelLanguages.add(buttonUSA);
		
		buttonJapan = new JButton(japanFlag);
		buttonJapan.addActionListener((event) -> this.bundle.changeLocale(locales.get("ja_JP")));
		buttonJapan.setContentAreaFilled(false);
		buttonJapan.setBounds(126, 25, 45, 40);
		filler.setToolTipText(buttonJapan, "button-japan");
		panelLanguages.add(buttonJapan);
		
		panelURL = new JPanel();
		panelURL.setLayout(null);
		panelURL.setOpaque(false);
		panelURL.setBounds(209, 12, 803, 75);
		filler.setBorder(panelURL, "panel-url");
		mainFrame.add(panelURL);
		
		textURL = new JTextField();
		textURL.setForeground(color);
		textURL.setFont(font);
		textURL.setColumns(10);
		textURL.setBounds(12, 30, 653, 25);
		filler.setToolTipText(textURL, "text-url");
		panelURL.add(textURL);
		
		buttonURLPaste = new JButton(pasteIcon);
		buttonURLPaste.addActionListener((event) -> textURL.setText(ClipboardUtils.copy()));
		buttonURLPaste.setBounds(677, 30, 30, 25);
		filler.setToolTipText(buttonURLPaste, "button-url-paste");
		panelURL.add(buttonURLPaste);
		
		buttonURLClear = new JButton(clearIcon);
		buttonURLClear.addActionListener((event) -> actionPlaylistClear());
		buttonURLClear.setBounds(719, 30, 30, 25);
		filler.setToolTipText(buttonURLClear, "button-url-clear");
		panelURL.add(buttonURLClear);
		
		buttonURLParse = new JButton(parseIcon);
		buttonURLParse.addActionListener((event) -> actionPlaylistParse());
		buttonURLParse.setBounds(761, 30, 30, 25);
		filler.setToolTipText(buttonURLParse, "button-url-parse");
		panelURL.add(buttonURLParse);
		
		panelMedia = new JPanel();
		panelMedia.setOpaque(false);
		panelMedia.setBounds(12, 90, 1000, 110);
		panelMedia.setLayout(null);
		filler.setBorder(panelMedia, "panel-media");
		mainFrame.add(panelMedia);
		
		panelResolution = new JPanel();
		panelResolution.setOpaque(false);
		panelResolution.setBounds(12, 25, 239, 70);
		filler.setBorder(panelResolution, "panel-resolution");
		panelResolution.setLayout(null);
		panelMedia.add(panelResolution);
		
		comboResolution = new JComboBox<String>();
		comboResolution.addActionListener((event) -> listenerCombo());
		comboResolution.setBounds(12, 25, 125, 25);
		comboResolution.setFont(font);
		comboResolution.setForeground(color);
		panelResolution.add(comboResolution);
		
		labelDuration = new JLabel();
		labelDuration.setForeground(color);
		labelDuration.setHorizontalAlignment(JLabel.CENTER);
		labelDuration.setFont(font);
		labelDuration.setBounds(155, 25, 70, 25);
		panelResolution.add(labelDuration);
		
		panelOutput = new JPanel();
		panelOutput.setOpaque(false);
		panelOutput.setBounds(263, 25, 725, 70);
		panelOutput.setLayout(null);
		filler.setBorder(panelOutput,"panel-output");
		panelMedia.add(panelOutput);
		
		textOutputFile = new JTextField();
		textOutputFile.setEditable(false);
		textOutputFile.setFont(font);
		textOutputFile.setForeground(color);
		textOutputFile.setBounds(12, 25, 579, 25);
		textOutputFile.setColumns(10);
		panelOutput.add(textOutputFile);
		
		buttonOutputSelect = new JButton(selectIcon);
		buttonOutputSelect.addActionListener((event) -> actionOutputSelect());
		buttonOutputSelect.setBounds(601, 25, 30, 25);
		filler.setToolTipText(buttonOutputSelect, "button-output-select");
		panelOutput.add(buttonOutputSelect);
		
		JButton buttonOutputOpen = new JButton(openDirIcon);
		buttonOutputOpen.addActionListener((event) -> actionOutputOpen());
		buttonOutputOpen.setBounds(641, 25, 30, 25);
		filler.setToolTipText(buttonOutputOpen, "button-output-open");
		panelOutput.add(buttonOutputOpen);
		
		buttonOutputClear = new JButton(clearIcon);
		buttonOutputClear.addActionListener((event) -> actionOutputClear());
		buttonOutputClear.setBounds(683, 25, 30, 25);
		filler.setToolTipText(buttonOutputClear, "button-output-clear");
		panelOutput.add(buttonOutputClear);
		
		buttonExit = new JButton(exitIcon);
		buttonExit.addActionListener((event) -> dispose());
		buttonExit.setBounds(942, 580, 30, 25);
		filler.setToolTipText(buttonExit, "button-exit");
		mainFrame.add(buttonExit);
		
		buttonDownload = new JButton(downloadIcon);
		buttonDownload.addActionListener((event) -> actionDownload());
		buttonDownload.setBounds(982, 580, 30, 25);
		filler.setToolTipText(buttonDownload, "button-download");
		mainFrame.add(buttonDownload);
		
		buttonCancel = new JButton(cancelIcon);
		buttonCancel.addActionListener((event) -> actionDownloadStop());
		buttonCancel.setBounds(982, 580, 30, 25);
		filler.setToolTipText(buttonCancel, "button-cancel");
		mainFrame.add(buttonCancel);
		
		labelLog = new JLabel();
		labelLog.setFont(font);
		labelLog.setBounds(12, 580, 721, 25);
		mainFrame.add(labelLog);
		
		panelConsole = new JPanel();
		panelConsole.setOpaque(false);
		panelConsole.setBounds(12, 205, 1000, 361);
		panelConsole.setLayout(null);
		filler.setBorder(panelConsole, "panel-console");
		mainFrame.add(panelConsole);
		
		JScrollPane scrollConsole = new JScrollPane();
		scrollConsole.setBounds(12, 30, 976, 282);
		panelConsole.add(scrollConsole);
		
		textConsole = new JTextArea();
		textConsole.setEditable(false);
		textConsole.setForeground(Color.WHITE);
		textConsole.setBackground(Color.BLACK);
		textConsole.setFont(font);
		scrollConsole.setViewportView(textConsole);
		
		progressDownload = new JProgressBar();
		progressDownload.setStringPainted(true);
		progressDownload.setForeground(bl_lt);
		progressDownload.setFont(font);
		progressDownload.setBounds(12, 324, 976, 25);
		progressDownload.setVisible(false);
		panelConsole.add(progressDownload);
		
		// Redirecting window closing event to a custom dispose() method, to prevent system instability
		addWindowListener(new WindowAdapter() {
		   public void windowClosing(WindowEvent event) {
		       dispose();
		}});
		
		// Building JTextArea popup menu
		onCreateOptionsPopupMenu();
		
		// Loading texts, tooltip texts and borders to UI
		this.bundle.updateUI();
		
		setSize(dimension);
		setResizable(false);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
		setVisible(true);
		
	}

	/** Creating JTextArea popup menu */
	private void onCreateOptionsPopupMenu() {
		
		JPopupMenu popup = new JPopupMenu();
		
		itemSave = new JMenuItem();
		itemSave.addActionListener((event) -> actionMenuSave());
		filler.setText(itemSave, "item-save");
		popup.add(itemSave);
		
		itemClear = new JMenuItem();
		itemClear.addActionListener((event) -> textConsole.setText(null));
		filler.setText(itemClear, "item-clear");
		popup.add(itemClear);
		
		textConsole.setComponentPopupMenu(popup);
		
	}
	
	/************************** Listener Methods Section **********************************/
	
	/** Keeps the console updated with the current media resolution selected. */
	private void listenerCombo() {
		
		// If there is a valid playlist
		if (this.playlist != null)
			consoleln("console-select-res", comboResolution.getSelectedItem().toString());
		
	}
	
	/************************ Button Event Methods Section ********************************/
	
	/** Checks pre-requisites and, if everything's fine, procceed with the download of selected media. */
	private void actionDownload() {
		
		final String title = this.bundle.getString("action-download-dialog-title");
		
		/*********** Checking pre-requisites ************/
		if (this.playlist == null) {
			AlertDialog.error(title,this.bundle.getString("action-download-parse-error"));
			return;
		}
		
		if (this.outputFile == null) {
			AlertDialog.error(title,this.bundle.getString("action-download-file-error"));
			return;
		}
		
		/********* Showing a confirm dialog *************/
		String resolution = this.comboResolution.getSelectedItem().toString();
		String overwrite = (this.outputFile.exists()) ? this.bundle.getString("action-download-overwrite") : "";
		
		String message = this.bundle.getFormattedString("action-download-confirm", resolution,this.outputFile.getAbsolutePath(),overwrite);
		int choice = AlertDialog.dialog(title,message);
		
		if (choice != AlertDialog.OK_OPTION)
			return;

		// Locking some fields to prevent the user to change values while downloading
		utilLockDownloading(true);
		utilToggleButtons(true);
		
		// Doing the hard work...
		this.downloaderThread = new Thread(() -> downloader());
		this.downloaderThread.setName("Downloader thread");
		this.downloaderThread.start();
		
	}
	
	/** Stops the current running download. */
	private void actionDownloadStop() {
		
		int choice = AlertDialog.dialog(this.bundle.getString("action-download-stop"));
		
		if (choice == AlertDialog.OK_OPTION)
			this.ffmpeg.interrupt();
		
	}
	
	/** Saves the console text to a plain txt file using UTF-8 encoding. */
	private void actionMenuSave() {
		
		final String title = this.bundle.getString("action-menu-save-title");
		
		// File selection dialog
		final File file = PhillFileUtils.loadFile(this.bundle.getString("action-menu-save-file-dialog"),
				                                  Constants.Format.TXT,
				                                  PhillFileUtils.SAVE_DIALOG,
				                                  lastSelectedDir);
		
		if (file != null)
			
			if (file.getParentFile().canWrite())
				
				try {
					
					// Getting current timestamp
					String format = this.bundle.getString("action-menu-save-timestamp");
					String timeStamp = new SimpleDateFormat(format).format(new Date());
					
					// Mounting log string
					StringBuilder builder = new StringBuilder(this.bundle.getString("action-menu-save-log"));
					              builder.append(" - ");
								  builder.append(timeStamp);
								  builder.append("\n");
								  builder.append(textConsole.getText());
					
					// Writing string to file (UTF-8)
					FileUtils.writeStringToFile(file,builder.toString(),"UTF-8");
					
					AlertDialog.info(title, this.bundle.getString("action-menu-save-success"));
					
				} catch (IOException exception) {
					exception.printStackTrace();
					AlertDialog.error(title, this.bundle.getString("action-menu-save-fail"));
				}
		
			else
				AlertDialog.error(title, this.bundle.getString("action-menu-save-ro"));
		
	}
	
	/** Clears the output file internal references. */
	private void actionOutputClear() {
		
		if (this.outputFile != null) {
			
			textOutputFile.setText(null);
			this.outputFile = null;
			
			consoleln("console-file-clear", null);
			
		}
		
	}
	
	/** Opens the current selected dir path */
	private void actionOutputOpen() {
		
		try {
			
			File dir = new File(this.outputFile.getParent());
			Desktop.getDesktop().open(dir);
			
		}
		catch (IOException | NullPointerException exception) {
			
		}
		
	}
	
	/** Shows a selection dialog for the output media file. */
	private void actionOutputSelect() {
		
		final String title = this.bundle.getString("action-output-select-title");
		
		// Recovering the selected file
		File file = PhillFileUtils.loadFile(this.bundle.getString("action-output-select-dialog"),
											Constants.Format.MP4,
											PhillFileUtils.SAVE_DIALOG,
											lastSelectedDir);
		
		// If something was selected...
		if (file != null) {
			
			// Removing invalid characters from filename
			if (file.getName().contains(":"))
				file = new File(file.getParent(),file.getName().replace(":"," -"));
			
			// ((saving current directory info, to be used as suggestion by the JFileChooser later))
			this.lastSelectedDir = file.getParentFile();
			
			// ... and the file cannot be written, the code ends here
			if (!file.getParentFile().canWrite()) {
				
				String message = this.bundle.getString("action-output-select-readonly");
				AlertDialog.error(title, message);
				return;
				
			}
			
			// ... and if the file already exists, an overwrite dialog is shown.
			if (file.exists()) {
				
				String message = this.bundle.getString("action-output-select-override");
				int choice = AlertDialog.dialog(title,message);
				
				// If the user doesn't want to overwrite the selected file, the code ends here
				if (choice != AlertDialog.OK_OPTION)
					return;
				
			}
			
			// ... otherwise internal references and UI are updated
			this.outputFile = file;
			textOutputFile.setText(file.getAbsolutePath());
			
			consoleln("console-file-select", this.outputFile.getAbsolutePath() + "'");
			
		}
		
	}
	
	/** Resets the entire screen and its internal references. */
	private void actionPlaylistClear() {
		
		// If a playlist was previously downloaded, a clear dialog is shown
		if (this.playlist != null) {
			
			String title = this.bundle.getString("playlist-clear-title");
			String message = this.bundle.getString("playlist-clear-dialog");
			
			int choice     = AlertDialog.dialog(title, message);
			
			// Breaks here when EXIT or CANCEL is selected
			if (choice != AlertDialog.OK_OPTION)
				return;
			
		}
		
		// Resetting parameters and unlocking panels, buttons, etc... 
		this.playlist = null;
		this.outputFile = null;
		
		this.labelLog.setVisible(false);
		this.labelDuration.setText(null);
		
		textOutputFile.setText(null);
		
		comboResolution.removeAllItems();
		utilLockMasterPanel(false);
		
		progressDownload.setValue(0);
		progressDownload.setVisible(false);
		
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
				utilMessage("label-parse-started", blue, true);
				console(this.bundle.getString("console-parse-started"));
				
				// Trying to download and parse the playlist object
				final URL playlistURL = new URL(website);
				final ArrayList<Chunklist> playlist = PlaylistParser.getConfig(playlistURL);
				
				// if I have a proper playlist...
				if (playlist != null) {
					
					utilMessage("label-parse-progress", blue, true);
					
					// ...then I save it, ...
					this.playlist = playlist;
					
					// ...get the media duration...
					utilMediaProbe();
					
					// ...and fill the combobox.
					utilFillCombo();
					
					// When everything finishes, the label is hidden and the clear button shown. 
					utilHideMessage();	consoleln("console-ok",null);
					SwingUtilities.invokeLater(() -> buttonURLClear.setEnabled(true));
					
				}
				
			}
			catch (MalformedURLException exception) {
				utilLockMasterPanel(false);	consoleln("console-fail",null);
				utilMessage("label-parse-url-fail", rd_dk, false, 5);
			}
			catch (ConnectException exception) {
				utilLockMasterPanel(false);	consoleln("console-fail",null);
				utilMessage("label-parse-501-fail", rd_dk, false, 5);
			}
			catch (IOException exception) {
				utilLockMasterPanel(false);	consoleln("console-fail",null);
				utilMessage("label-parse-io-fail", rd_dk, false, 5);
			}
			catch (Exception exception) {
				exception.printStackTrace();
				utilLockMasterPanel(false);	consoleln("console-fail",null);
				utilMessage("label-parse-unk-fail", rd_dk, false, 10);
			}
		
		};
		
		// Doing the hard work
		Thread playlistParseThread = new Thread(job);
		playlistParseThread.setName("Playlist Parser Thread");
		playlistParseThread.start();
		
	}
	
	/************************* Utility Methods Section ************************************/
	
	/** Append the given text to the internal console textArea.
	 *  @param text - text to be appended */
	private synchronized void console(final String text) {
		
		SwingUtilities.invokeLater(() -> {
			textConsole.append(text);
			textConsole.setCaretPosition(Math.max(textConsole.getText().lastIndexOf("\n"), 0));
		});
		
	}
	
	/** Append the given text (with a new line) to the internal console textArea.
	 *  @param text - text to be appended */
	private synchronized void consoleln(final String resourceTitle, final String text) {
		
		String message = String.format("%s %s\n", this.bundle.getString(resourceTitle),
												  ((text == null) ? "" : text));
		
		console(message);
	}
	
	/** Fills the given 'comboBox' with information of each individual {@link Chunklist} provided through 'playlist'. */
	private void utilFillCombo() {
		
		SwingUtilities.invokeLater(() -> {
			
			comboResolution.removeAllItems();
			
			for (Chunklist chunklist: this.playlist)
				comboResolution.addItem(chunklist.getResolution());
		
		});
		
	}
	
	/** Hides the label designed for logging. */
	private void utilHideMessage() {
		
		Runnable job = () -> {
			labelLog.setText(null);
			labelLog.setIcon(null);
		};
		
		SwingUtilities.invokeLater(job);
	}

	/** Blocks some fields when media download is in progress. 
	 *  @param lock - if 'true' then then components are locked. Otherwise, unlocked */
	private void utilLockDownloading(final boolean lock) {
		
		final boolean enable = !lock;
		
		SwingUtilities.invokeLater(() -> {
		
			buttonURLClear    .setEnabled(enable);
			comboResolution   .setEnabled(enable);
			buttonOutputSelect.setEnabled(enable);
			buttonOutputClear .setEnabled(enable);
		
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
	
	/** Runs ffprobe on the first chunklist (inside the playlist) to get media duration
	 *  and resolution (in case the user informed a direct media link instead playlist). */
	private void utilMediaProbe() {
		
		// Getting first chunklist from the downloaded playlist
		Chunklist chunklist = this.playlist.get(0);
		
		try {
			
			FFprobe ffprobe = new FFprobe();
			FFmpegProbeResult res = ffprobe.probe(chunklist.getURL());
			
			// Retrieving media duration
			String duration = FFmpegUtils.toTimecode((long) res.getFormat().duration, TimeUnit.SECONDS);
			
			// Retrieving media resolution in case it doesn't have
			if (chunklist.hasNoResolution())
				for (FFmpegStream stream: res.getStreams())
					if (stream.codec_type == FFmpegStream.CodecType.VIDEO) {
						chunklist.setResolution(stream.width,stream.height);
						break;
					}
			
			// Updating UI with media duratiuon (resolution is done by 'utilFillCombo' method)
			SwingUtilities.invokeLater(() -> labelDuration.setText(duration));
			
		} catch (IOException exception) {
			consoleln  ("console-media-probe",null);
			utilMessage("label-probe-fail", rd_dk, false, 10);
			exception.printStackTrace();
		}
		
		
	}
	
	/** Shows a message in the label designed for logging during a certain period of time.
	 *  @param message - the message to be displayed
	 *  @param color - the font color of the message
	 *  @param loading - if 'true' a loading gif is added to the beginning of the label
	 *  @param seconds - the amount of time to display the given message, before hiding it */
	private void utilMessage(final String key, final Color color, final boolean loading, int seconds) {
		
		// Starts a new thread to prevent the caller to wait for this method to end
		Runnable job = () -> {
			
			utilMessage(this.bundle.getString(key),color,loading);
			
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
	private void utilMessage(final String key, final Color color, final boolean loading) {
		
		Runnable job = () -> {
			labelLog.setText(this.bundle.getString(key));
			labelLog.setForeground(color);
			labelLog.setIcon(loading ? this.loading : null);
			labelLog.repaint();
		};
		
		SwingUtilities.invokeLater(job);
	}
	
	/** Toggle visibility of cancel and download buttons (that exist in the same
	 *   location) depending if a 'downloading' operation is being run. */
	private void utilToggleButtons(boolean downloading) {
		
		SwingUtilities.invokeLater(() -> {
			buttonDownload.setVisible(!downloading);
			buttonCancel  .setVisible( downloading);
		});
		
	}
	
	/***************************** Threaded Methods Section *******************************/
	
	/** Downloads the selected media using ffmpeg. More information can be found at this method comments. */
	private void downloader() {
		
		// Updating UI
		utilMessage("label-downloader-started", blue, true);
		SwingUtilities.invokeLater(() -> {
			
			progressDownload.setValue(0);
			progressDownload.setVisible(true);
			progressDownload.setForeground(bl_lt);
			
			consoleln("console-dw-started",null);
			
		});
		
		try {
			
			// Retrieving chunklist URL from the selected resolution
			final Chunklist selected = this.playlist.get(comboResolution.getSelectedIndex());
			final String playlistURL = selected.getURL();
			
			// Locating ffmpeg files
			this.ffmpeg = new FFmpeg ();	// when no parameter is passed, it retrieves the path from your system's variables
	        final FFprobe ffprobe = new FFprobe();
	        
	        // Creating executor
	        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
	        
	        // Probing current media
	        FFmpegProbeResult in = ffprobe.probe(playlistURL);
	        
	        // Creating ffmpeg command line:
	        // ffmpeg -i <chunklist> -c copy -bsf:a aac_adtstoasc <output.mp4>
	        FFmpegBuilder builder = new FFmpegBuilder()
	        								.addInput(playlistURL)
	        								.addOutput(this.outputFile.getAbsolutePath())
	        								.setAudioCodec("copy")
	        								.setVideoCodec("copy")
	        								.setAudioBitStreamFilter("aac_adtstoasc")
	        							.done();
	        
	        // Creating ffmpeg progress monitor
	        FFmpegJob job = executor.createJob(builder, new ProgressListener() {

	        	// Using the FFmpegProbeResult to determine the duration of the input
	        	final double duration_ns = (in.getFormat().duration * TimeUnit.SECONDS.toNanos(1));

	        	@Override
	        	public void progress(Progress progress) {
	        		
	        		// Obtaining the current progress
	        		final double percentage = Math.ceil((progress.out_time_ns / duration_ns) * 100f);
	        		final int progressValue = (int) percentage;
	        		
	        		// Formatting console output log
	        		String log = 
	        		String.format(bundle.getString("ffmpeg-debug-format"),
	        			  progressValue,
      					  progress.frame,
      					  FFmpegUtils.toTimecode(progress.out_time_ns, TimeUnit.NANOSECONDS).substring(0,8),
      					  progress.fps.doubleValue(),
      					  progress.speed,
      					  PhillFileUtils.humanReadableByteCount(progress.total_size)
      					  );
	        		
	        		// Updating UI
	        		SwingUtilities.invokeLater(() -> {
	        			
	        			progressDownload.setValue(progressValue);
	        			console(log);
	        			
	        		});
	        		
	        	}
	        	
	        });
	        
	        // Doing the actual hard work - this thread will be locked here until the ffmpeg jog finishes
	        job.run();
	        
	        // If the 'dispose()' method was called, I need to imediately finish the current Thread
	        if (disposing)
	        	return;
	        
	        // When the job finishes (or is interrupted) the UI is updated
	        if (progressDownload.getValue() == 100) {
	        	
	        	consoleln  ("console-dw-complete",null);
	        	utilMessage("label-downloader-success", gr_dk, false, 5);
	        	AlertDialog.info( this.bundle.getString("action-download-dialog-title"), this.bundle.getString("downloader-success-dialog") );
	        	
	        }
	        else {
	        	
	        	consoleln  ("console-dw-stopped",null);
	        	utilMessage("label-downloader-stopped", yl_dk, false, 5);
	        	SwingUtilities.invokeLater(() -> progressDownload.setForeground(yl_dk));
	        	
	        }
        	
		}
		catch (Exception exception) {
			consoleln  ("console-dw-failed",null);
			utilMessage("label-downloader-fail", rd_dk, false, 10);
			exception.printStackTrace();
		}
		finally {
			utilToggleButtons  (false);
			utilLockDownloading(false);
		}
		
	}
	
	// Flag to control safe disposing of threads 
	private volatile boolean disposing;
	
	@Override
	public void dispose() {
		
		// If the downloading media thread is being executed...
		if ((this.downloaderThread != null) && (this.downloaderThread.isAlive())) {
			
			String title   = this.bundle.getString("dispose-title");
			String message = this.bundle.getString("dispose-confirm");
			
			int choice = AlertDialog.dialog(title, message);
			
			// and the user really wants to exit, we cancel the current running thread before
			if (choice == AlertDialog.OK_OPTION) {
				this.disposing = true;
				this.ffmpeg.interrupt();
				super.dispose();
			}
			
		}
		else
			super.dispose();
		
	}
	
	/** Main function starting UI */
	public static void main(String[] args) {
		new HDWMainGui();
	}
}
