package com.hdw.view;

import java.awt.*;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.*;

import com.hdw.controller.PlaylistParser;
import com.hdw.model.Chunklist;
import com.phill.libs.*;

public class HDWMainGui extends JFrame {

	private static final long serialVersionUID = 610724819466691396L;
	
	private JTextField textURL;
	private ArrayList<Chunklist> playlist;
	private JTextField textOutputFile;
	
	private File lastSelectedDir, outputFile;
	private JComboBox<String> comboResolution;

	public static void main(String[] args) {
		new HDWMainGui();
	}

	public HDWMainGui() {
		super("HDW - build 20200914");
		
		// Recovering graphical elements from 'res' directory
		//GraphicsHelper.setFrameIcon(this,"icon/icon.png");
		GraphicsHelper helper = GraphicsHelper.getInstance();
		Font   font = helper.getFont ();
		Color color = helper.getColor();
		
		Icon pasteIcon = ResourceManager.getResizedIcon("icon/clipboard_past.png",20,20);
		Icon clearIcon = ResourceManager.getResizedIcon("icon/clear.png",20,20);
		Icon parseIcon = ResourceManager.getResizedIcon("icon/cog.png",20,20);
		Icon selectIcon = ResourceManager.getResizedIcon("icon/zoom.png",20,20);
		
		// Building UI
		Dimension dimension = new Dimension(1024,640);
		JPanel mainFrame = new JPaintedPanel("img/background.png",dimension);
		setContentPane(mainFrame);
		
		setSize(dimension);
		setResizable(false);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		mainFrame.setLayout(null);
		
		JPanel panelURL = new JPanel();
		panelURL.setBorder(helper.getTitledBorder("HLS Playlist URL"));
		panelURL.setLayout(null);
		panelURL.setOpaque(false);
		panelURL.setBounds(12, 12, 1000, 75);
		mainFrame.add(panelURL);
		
		textURL = new JTextField();
		textURL.setToolTipText("Here goes the 'master.json' URL");
		textURL.setForeground(color);
		textURL.setFont(font);
		textURL.setColumns(10);
		textURL.setBounds(12, 30, 850, 25);
		panelURL.add(textURL);
		
		JButton buttonURLPaste = new JButton(pasteIcon);
		buttonURLPaste.setToolTipText("Get link from clipboard");
		buttonURLPaste.addActionListener((event) -> textURL.setText(AlertDialog.copyFromClipboard()));
		buttonURLPaste.setBounds(875, 30, 30, 25);
		panelURL.add(buttonURLPaste);
		
		JButton buttonURLClear = new JButton(clearIcon);
		buttonURLClear.setToolTipText("Clear");
		buttonURLClear.addActionListener((event) -> actionURLClear());
		buttonURLClear.setBounds(915, 30, 30, 25);
		panelURL.add(buttonURLClear);
		
		JButton buttonURLParse = new JButton(parseIcon);
		buttonURLParse.setToolTipText("Parse");
		buttonURLParse.addActionListener((event) -> actionURLParse());
		buttonURLParse.setBounds(955, 30, 30, 25);
		panelURL.add(buttonURLParse);
		
		JPanel panelMedia = new JPanel();
		panelMedia.setOpaque(false);
		panelMedia.setBorder(helper.getTitledBorder("Media Selection"));
		panelMedia.setBounds(12, 90, 1000, 110);
		mainFrame.add(panelMedia);
		panelMedia.setLayout(null);
		
		JPanel panel = new JPanel();
		panel.setBorder(helper.getTitledBorder("Resolution"));
		panel.setOpaque(false);
		panel.setBounds(12, 25, 145, 70);
		panelMedia.add(panel);
		panel.setLayout(null);
		
		comboResolution = new JComboBox<String>();
		comboResolution.setBounds(12, 25, 115, 25);
		comboResolution.setFont(font);
		comboResolution.setForeground(color);
		panel.add(comboResolution);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(helper.getTitledBorder("Output File"));
		panel_1.setOpaque(false);
		panel_1.setBounds(169, 25, 819, 70);
		panelMedia.add(panel_1);
		panel_1.setLayout(null);
		
		textOutputFile = new JTextField();
		textOutputFile.setEditable(false);
		textOutputFile.setFont(font);
		textOutputFile.setForeground(color);
		textOutputFile.setBounds(12, 25, 713, 25);
		panel_1.add(textOutputFile);
		textOutputFile.setColumns(10);
		
		JButton buttonOutputSelect = new JButton(selectIcon);
		buttonOutputSelect.addActionListener((event) -> actionOutputSelect());
		buttonOutputSelect.setToolTipText("Select file");
		buttonOutputSelect.setBounds(737, 25, 30, 25);
		panel_1.add(buttonOutputSelect);
		
		JButton buttonOutputClear = new JButton(clearIcon);
		buttonOutputClear.addActionListener((event) -> actionOutputClear());
		buttonOutputClear.setToolTipText("Clear");
		buttonOutputClear.setBounds(777, 25, 30, 25);
		panel_1.add(buttonOutputClear);
		
		JPanel panel_2 = new JPanel();
		panel_2.setBorder(helper.getTitledBorder("Console"));
		panel_2.setOpaque(false);
		panel_2.setBounds(12, 205, 1000, 361);
		mainFrame.add(panel_2);
		panel_2.setLayout(null);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 25, 976, 324);
		panel_2.add(scrollPane);
		
		JTextArea textArea = new JTextArea();
		textArea.setBackground(Color.BLACK);
		textArea.setForeground(Color.WHITE);
		textArea.setFont(font);
		scrollPane.setViewportView(textArea);
		
		JButton button = new JButton((Icon) null);
		button.setToolTipText("Download media");
		button.setBounds(982, 578, 30, 25);
		mainFrame.add(button);
		
		JButton button_1 = new JButton((Icon) null);
		button_1.setToolTipText("Exit");
		button_1.setBounds(942, 578, 30, 25);
		mainFrame.add(button_1);
		
		setVisible(true);
	}

	private void actionURLClear() {
		
		textURL.setText(null);
		textURL.requestFocus();
		
	}
	
	private void actionURLParse() {
		
		try {
		
			// Getting URL from text field
			final String  website = textURL.getText().trim();
			final URL playlistURL = new URL(website);
			
			this.playlist = PlaylistParser.getConfig(playlistURL);
			
			for (Chunklist chunklist: this.playlist)
				comboResolution.addItem(chunklist.getResume());
		
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
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
