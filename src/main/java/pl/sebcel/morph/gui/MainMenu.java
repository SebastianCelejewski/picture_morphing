package pl.sebcel.morph.gui;

import java.io.File;
import java.text.DecimalFormat;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import pl.sebcel.morph.ApplicationLogic;
import pl.sebcel.morph.model.TransformData;
import pl.sebcel.morph.utils.FileOperations;

public class MainMenu extends JMenuBar {

	private static final long serialVersionUID = 1L;

	private ApplicationLogic applicationLogic;
	private FileOperations fileOperations;

	private JMenu menuFile = new JMenu("File");
	private JMenuItem menuFileNew = new JMenuItem("New");
	private JMenuItem menuFileOpen = new JMenuItem("Open");
	private JMenuItem menuFileSave = new JMenuItem("Save");
	private JMenuItem menuFileSaveAs = new JMenuItem("Save As...");
	private JMenuItem menuFileExport = new JMenuItem("Export");
	private JMenuItem menuFileClose = new JMenuItem("Close");
	private JMenuItem menuFileExit = new JMenuItem("Exit");

	private JMenu menuImage = new JMenu("Images");
	private JMenuItem menuImageSelectSourceImage = new JMenuItem("Select source image");
	private JMenuItem menuImageSelectTargetImage = new JMenuItem("Select target image");

	private JMenu menuHelp = new JMenu("Help");
	private JMenuItem menuHelpAbout = new JMenuItem("About");

	private File currentFile = null;

	public void setFileOperations(FileOperations fileOperations) {
		this.fileOperations = fileOperations;
	}
	
	public void setApplicationLogic(ApplicationLogic applicationLogic) {
		this.applicationLogic = applicationLogic;
	}

	public MainMenu() {
		this.add(menuFile);
		this.menuFile.add(menuFileNew);
		this.menuFile.add(menuFileOpen);
		this.menuFile.add(menuFileSave);
		this.menuFile.add(menuFileSaveAs);
		this.menuFile.add(menuFileExport);
		this.menuFile.add(menuFileClose);
		this.menuFile.add(menuFileExit);

		this.add(menuImage);
		this.menuImage.add(menuImageSelectSourceImage);
		this.menuImage.add(menuImageSelectTargetImage);

		this.add(menuHelp);
		this.menuHelp.add(menuHelpAbout);

		this.menuFileNew.addActionListener(e -> newProject());
		this.menuFileOpen.addActionListener(e -> openProject());
		this.menuFileSave.addActionListener(e -> saveProject());
		this.menuFileSaveAs.addActionListener(e -> saveProjectAs());
		this.menuFileExport.addActionListener(e -> exportProject());
		this.menuFileClose.addActionListener(e -> close());
		this.menuFileExit.addActionListener(e -> exit());

		this.menuImageSelectSourceImage.addActionListener(e -> selectSourceImage());
		this.menuImageSelectTargetImage.addActionListener(e -> selectTargetImage());
	}

	private void newProject() {
		applicationLogic.createNewProject();
		currentFile = null;
	}

	private void openProject() {
		JFileChooser fc = new JFileChooser();
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			TransformData project = fileOperations.loadProject(file);
			applicationLogic.setProject(project);
			currentFile = file;
		}
	}

	private void saveProject() {
		if (currentFile == null) {
			saveProjectAs();
			return;
		}
		TransformData project = applicationLogic.getProject();
		fileOperations.saveProject(currentFile, project);
	}

	private void saveProjectAs() {
		JFileChooser fc = new JFileChooser();
		if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			TransformData project = applicationLogic.getProject();
			fileOperations.saveProject(file, project);
			currentFile = file;
		}
	}

	private void exportProject() {
		new Thread(() -> {
			int idx = 0;
			DecimalFormat df = new DecimalFormat("000");
			try {
				for (double phase = 0.0; phase < 1.0; phase += 1d / 30) {
					String filename = currentFile.getName() + "-export-" + df.format(idx++) + ".jpg";
					System.out.println(filename);
//					engine.setPhase(phase);
//					BufferedImage image = engine.getImage(Role.OUTPUT);
//					ImageIO.write(image, "jpg", new File(filename));
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}).start();
	}

	private void close() {
		applicationLogic.closeProject();
		currentFile = null;
	}

	private void exit() {
		System.exit(0);
	}

	private void selectSourceImage() {
		JFileChooser fc = new JFileChooser();
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			applicationLogic.setSourceImage(file);
		}
	}

	private void selectTargetImage() {
		JFileChooser fc = new JFileChooser();
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			applicationLogic.setTargetImage(file);
		}
	}
}