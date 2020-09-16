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
import com.hdw.model.*;
import com.hdw.controller.*;

/** Implements the main User Interface and all its functionalities.
 *  @author Felipe André - felipeandresouza@hotmail.com
 *  @version 2.5 - 16/09/2020 */
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
	private final JButton buttonBrazil, buttonUSA, button_2;
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
	
	private ResourceBundle titles;

	/** Builds the graphical interface and its functionalities */
	public HDWMainGui() {
		super("HDW - build 20200916");
		
		// Loading available locales
		HashMap<String, Locale> locales = new HashMap<String, Locale>(2);
		locales.put("en_US",Locale.ENGLISH);
		locales.put("pt_BR", new Locale("pt","BR"));
		
		// Retrieving graphical elements from 'res' directory
		GraphicsHelper.setFrameIcon(this,"icon/icon.png");
		Font   font = helper.getFont ();
		Color color = helper.getColor();
		
		Icon cancelIcon   = ResourceManager.getResizedIcon("icon/cancel.png",20,20);
		Icon clearIcon    = ResourceManager.getResizedIcon("icon/clear.png",20,20);
		Icon downloadIcon = ResourceManager.getResizedIcon("icon/save.png",20,20);
		Icon exitIcon     = ResourceManager.getResizedIcon("icon/shutdown.png",20,20);
		Icon parseIcon    = ResourceManager.getResizedIcon("icon/cog.png",20,20);
		Icon pasteIcon    = ResourceManager.getResizedIcon("icon/clipboard_past.png",20,20);
		Icon selectIcon   = ResourceManager.getResizedIcon("icon/zoom.png",20,20);
		
		// Building UI
		Dimension dimension = new Dimension(1024,720);
		
		JPanel mainFrame = new JPaintedPanel("img/background.png",dimension);
		mainFrame.setLayout(null);
		setContentPane(mainFrame);
		
		Icon brazilFlag = ResourceManager.getResizedIcon("icon/brazil-flag.png",35,35);
		Icon usaFlag    = ResourceManager.getResizedIcon("icon/usa-flag.png",35,35);
		
		panelLanguages = new JPanel();
		panelLanguages.setOpaque(false);
		panelLanguages.setBounds(12, 12, 185, 75);
		panelLanguages.setLayout(null);
		mainFrame.add(panelLanguages);
		
		buttonBrazil = new JButton(brazilFlag);
		buttonBrazil.addActionListener((event) -> loadTitles(locales.get("pt_BR")));
		buttonBrazil.setContentAreaFilled(false);
		buttonBrazil.setBounds(12, 25, 45, 40);
		panelLanguages.add(buttonBrazil);
		
		buttonUSA = new JButton(usaFlag);
		buttonUSA.addActionListener((event) -> loadTitles(locales.get("en_US")));
		buttonUSA.setContentAreaFilled(false);
		buttonUSA.setBounds(69, 25, 45, 40);
		panelLanguages.add(buttonUSA);
		
		button_2 = new JButton((Icon) null);
		button_2.setContentAreaFilled(false);
		button_2.setBounds(126, 25, 45, 40);
		panelLanguages.add(button_2);
		
		panelURL = new JPanel();
		panelURL.setLayout(null);
		panelURL.setOpaque(false);
		panelURL.setBounds(209, 12, 803, 75);
		mainFrame.add(panelURL);
		
		textURL = new JTextField();
		textURL.setForeground(color);
		textURL.setFont(font);
		textURL.setColumns(10);
		textURL.setBounds(12, 30, 653, 25);
		panelURL.add(textURL);
		
		buttonURLPaste = new JButton(pasteIcon);
		buttonURLPaste.addActionListener((event) -> textURL.setText(AlertDialog.copyFromClipboard()));
		buttonURLPaste.setBounds(677, 30, 30, 25);
		panelURL.add(buttonURLPaste);
		
		buttonURLClear = new JButton(clearIcon);
		buttonURLClear.addActionListener((event) -> actionPlaylistClear());
		buttonURLClear.setBounds(719, 30, 30, 25);
		panelURL.add(buttonURLClear);
		
		buttonURLParse = new JButton(parseIcon);
		buttonURLParse.addActionListener((event) -> actionPlaylistParse());
		buttonURLParse.setBounds(761, 30, 30, 25);
		panelURL.add(buttonURLParse);
		
		panelMedia = new JPanel();
		panelMedia.setOpaque(false);
		panelMedia.setBounds(12, 90, 1000, 110);
		mainFrame.add(panelMedia);
		panelMedia.setLayout(null);
		
		panelResolution = new JPanel();
		panelResolution.setOpaque(false);
		panelResolution.setBounds(12, 25, 239, 70);
		panelMedia.add(panelResolution);
		panelResolution.setLayout(null);
		
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
		panelMedia.add(panelOutput);
		panelOutput.setLayout(null);
		
		textOutputFile = new JTextField();
		textOutputFile.setEditable(false);
		textOutputFile.setFont(font);
		textOutputFile.setForeground(color);
		textOutputFile.setBounds(12, 25, 617, 25);
		textOutputFile.setColumns(10);
		panelOutput.add(textOutputFile);
		
		buttonOutputSelect = new JButton(selectIcon);
		buttonOutputSelect.addActionListener((event) -> actionOutputSelect());
		buttonOutputSelect.setBounds(641, 25, 30, 25);
		panelOutput.add(buttonOutputSelect);
		
		buttonOutputClear = new JButton(clearIcon);
		buttonOutputClear.addActionListener((event) -> actionOutputClear());
		buttonOutputClear.setBounds(683, 25, 30, 25);
		panelOutput.add(buttonOutputClear);
		
		buttonExit = new JButton(exitIcon);
		buttonExit.addActionListener((event) -> dispose());
		buttonExit.setBounds(942, 658, 30, 25);
		mainFrame.add(buttonExit);
		
		buttonDownload = new JButton(downloadIcon);
		buttonDownload.addActionListener((event) -> actionDownload());
		buttonDownload.setBounds(982, 658, 30, 25);
		mainFrame.add(buttonDownload);
		
		buttonCancel = new JButton(cancelIcon);
		buttonCancel.addActionListener((event) -> actionDownloadStop());
		buttonCancel.setBounds(982, 658, 30, 25);
		mainFrame.add(buttonCancel);
		
		labelLog = new JLabel();
		labelLog.setFont(font);
		labelLog.setBounds(12, 658, 721, 25);
		mainFrame.add(labelLog);
		
		panelConsole = new JPanel();
		panelConsole.setOpaque(false);
		panelConsole.setBounds(12, 205, 1000, 361);
		mainFrame.add(panelConsole);
		panelConsole.setLayout(null);
		
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
		
		loadTitles(Locale.getDefault());
		
		setSize(dimension);
		setResizable(false);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
		setVisible(true);
		
	}

	/** Changes the program language.
	 *  @param locale - locale selected (using buttons) */
	private void loadTitles(final Locale locale) {
		
		this.titles = ResourceBundle.getBundle("locales/titles",locale);
		
		// Loading panel titles
		panelLanguages .setBorder(helper.getTitledBorder(titles.getString("panel-languages" )));
		panelURL       .setBorder(helper.getTitledBorder(titles.getString("panel-url"       )));
		panelMedia     .setBorder(helper.getTitledBorder(titles.getString("panel-media"     )));
		panelResolution.setBorder(helper.getTitledBorder(titles.getString("panel-resolution")));
		panelOutput    .setBorder(helper.getTitledBorder(titles.getString("panel-output"    )));
		panelURL       .setBorder(helper.getTitledBorder(titles.getString("panel-url"       )));
		panelConsole   .setBorder(helper.getTitledBorder(titles.getString("panel-console"   )));
		
		// Loading hints
		buttonBrazil      .setToolTipText(titles.getString("button-brazil"       ));
		buttonUSA         .setToolTipText(titles.getString("button-usa"          ));
		textURL           .setToolTipText(titles.getString("text-url"            ));
		buttonURLPaste    .setToolTipText(titles.getString("button-url-paste"    ));
		buttonURLClear    .setToolTipText(titles.getString("button-url-clear"    ));
		buttonURLParse    .setToolTipText(titles.getString("button-url-parse"    ));
		buttonOutputSelect.setToolTipText(titles.getString("button-output-select"));
		buttonOutputClear .setToolTipText(titles.getString("button-output-clear" ));
		buttonDownload    .setToolTipText(titles.getString("button-download"     ));
		buttonCancel      .setToolTipText(titles.getString("button-cancel"       ));
		buttonExit        .setToolTipText(titles.getString("button-exit"         ));
		
		// Loading popup menu
		itemSave .setText(titles.getString("item-save"));
		itemClear.setText(titles.getString("item-clear"));
	
	}
	
	/** Creating JTextArea popup menu */
	private void onCreateOptionsPopupMenu() {
		
		JPopupMenu popup = new JPopupMenu();
		
		itemSave = new JMenuItem();
		itemSave.addActionListener((event) -> actionMenuSave());
		popup.add(itemSave);
		
		itemClear = new JMenuItem();
		itemClear.addActionListener((event) -> textConsole.setText(null));
		popup.add(itemClear);
		
		textConsole.setComponentPopupMenu(popup);
		
	}
	
	/************************** Listener Methods Section **********************************/
	
	/** Keeps the console updated with the current media resolution selected. */
	private void listenerCombo() {
		
		// If there is a valid playlist
		if (this.playlist != null)
			consoleln(":: Selected Resolution: " + comboResolution.getSelectedItem());
		
	}
	
	/************************ Button Event Methods Section ********************************/
	
	/** Checks pre-requisites and, if everything's fine, procceed with the download of selected media. */
	private void actionDownload() {
		
		/*********** Checking pre-requisites ************/
		if (this.playlist == null) {
			AlertDialog.erro(this.titles.getString("action-download-parse-error"));
			return;
		}
		
		if (this.outputFile == null) {
			AlertDialog.erro(this.titles.getString("action-download-file-error"));
			return;
		}
		
		/********* Showing a confirm dialog *************/
		String resolution = this.comboResolution.getSelectedItem().toString();
		String overwrite = (this.outputFile.exists()) ? this.titles.getString("action-download-overwrite") : "";
		
		String message = ResourceManager.getText(this,"download-confirm.msg",resolution,this.outputFile.getAbsolutePath(),overwrite);
		int choice = AlertDialog.dialog(message);
		
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
		
		int choice = AlertDialog.dialog(this.titles.getString("action-download-stop"));
		
		if (choice == AlertDialog.OK_OPTION)
			this.ffmpeg.interrupt();
		
	}
	
	/** Saves the console text to a plain txt file using UTF-8 encoding. */
	private void actionMenuSave() {
		
		final String title = this.titles.getString("action-menu-save-title");
		
		// File selection dialog
		final File file = FileChooserHelper.loadFile(this,
				                                     Constants.Format.TXT,
				                                     this.titles.getString("action-menu-save-file-dialog"),
				                                     false,
				                                     lastSelectedDir
				                                     );
		
		if (file != null)
			
			if (file.getParentFile().canWrite())
				
				try {
					
					// Getting current timestamp
					String format = this.titles.getString("action-menu-save-timestamp");
					String timeStamp = new SimpleDateFormat(format).format(new Date());
					
					// Mounting log string
					StringBuilder builder = new StringBuilder(this.titles.getString("action-menu-save-log"));
					              builder.append(" - ");
								  builder.append(timeStamp);
								  builder.append("\n");
								  builder.append(textConsole.getText());
					
					// Writing string to file (UTF-8)
					FileUtils.writeStringToFile(file,builder.toString(),"UTF-8");
					
					AlertDialog.informativo(title, this.titles.getString("action-menu-save-success"));
					
				} catch (IOException exception) {
					exception.printStackTrace();
					AlertDialog.erro(title, this.titles.getString("action-menu-save-fail"));
				}
		
			else
				AlertDialog.erro(title, this.titles.getString("action-menu-save-ro"));
		
	}
	
	/** Clears the output file internal references. */
	private void actionOutputClear() {
		
		textOutputFile.setText(null);
		this.outputFile = null;
		
		consoleln(":: Output file path cleared");
		
	}
	
	/** Shows a selection dialog for the output media file. */
	private void actionOutputSelect() {
		
		// Recovering the selected file
		final File file = FileChooserHelper.loadFile(this,Constants.Format.MP4,"Select an output file",false,lastSelectedDir);
		
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
			
			consoleln(":: Output file set to '" + this.outputFile.getAbsolutePath() + "'");
			
		}
		
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
		
		this.labelLog.setVisible(false);
		this.labelDuration.setText(null);
		
		textOutputFile.setText(null);
		
		comboResolution.removeAllItems();
		utilLockMasterPanel(false);
		
		textConsole.setText(null);
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
				utilMessage("Downloading playlist...", blue, true);
				console(":: Started parsing process...");
				
				// Trying to download and parse the playlist object
				final URL playlistURL = new URL(website);
				final ArrayList<Chunklist> playlist = PlaylistParser.getConfig(playlistURL);
				
				// if I have a proper playlist...
				if (playlist != null) {
					
					utilMessage("Parsing playlist...", blue, true);
					
					// ...then I save it, ...
					this.playlist = playlist;
					
					// ...get the media duration...
					utilMediaProbe();
					
					// ...and fill the combobox.
					utilFillCombo();
					
					// When everything finishes, the label is hidden and the clear button shown. 
					utilHideMessage();	consoleln("ok");
					SwingUtilities.invokeLater(() -> buttonURLClear.setEnabled(true));
					
				}
				
			}
			catch (MalformedURLException exception) {
				utilLockMasterPanel(false);	consoleln("fail");
				utilMessage("Invalid playlist URL", rd_dk, false, 5);
			}
			catch (ConnectException exception) {
				utilLockMasterPanel(false);	consoleln("fail");
				utilMessage("The server is refusing connections", rd_dk, false, 5);
			}
			catch (IOException exception) {
				utilLockMasterPanel(false);	consoleln("fail");
				utilMessage(exception.getMessage(), rd_dk, false, 5);
			}
			catch (Exception exception) {
				exception.printStackTrace();
				utilLockMasterPanel(false);	consoleln("fail");
				utilMessage("Unknown error occurred, please check the console", rd_dk, false, 10);
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
	private synchronized void consoleln(final String text) {
		console(text + "\n");
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
			consoleln(":: Media probing failed");
			utilMessage("Failed to get media size and duration, please check the console", rd_dk, false, 10);
			exception.printStackTrace();
		}
		
		
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
			labelLog.setText(message);
			labelLog.setForeground(color);
			labelLog.setIcon(loading ? this.loading : null);
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
		utilMessage("Downloading media", blue, true);
		SwingUtilities.invokeLater(() -> {
			
			progressDownload.setValue(0);
			progressDownload.setVisible(true);
			progressDownload.setForeground(bl_lt);
			
			consoleln(":: Media download started");
			
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
	        		String.format("[%d%%] frame: %d | time: %s | fps: %.0f | speed: %.2fx | bytes loaded: %s\n",
	        			  progressValue,
      					  progress.frame,
      					  FFmpegUtils.toTimecode(progress.out_time_ns, TimeUnit.NANOSECONDS),
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
	        	
	        	consoleln(":: Media download complete");
	        	utilMessage("Everything complete", gr_dk, false, 5);
	        	JOptionPane.showMessageDialog(null,"Everything's complete");
	        	
	        }
	        else {
	        	
	        	consoleln(":: Media download stopped");
	        	utilMessage("Download stopped", yl_dk, false, 5);
	        	SwingUtilities.invokeLater(() -> progressDownload.setForeground(yl_dk));
	        	
	        }
        	
		}
		catch (Exception exception) {
			consoleln(":: Media download failed");
			utilMessage("Failed to download media, please check the console", rd_dk, false, 10);
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
			
			String message = ResourceManager.getText(this,"exit-confirm.msg",0);
			int choice = JOptionPane.showConfirmDialog(this,message);
			
			// and the user really wants to exit, we cancel the current running thread before
			if (choice == JOptionPane.OK_OPTION) {
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
