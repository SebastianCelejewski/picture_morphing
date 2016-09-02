package pl.sebcel.morph.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JSlider;

import pl.sebcel.morph.engine.MorphingEngine;
import pl.sebcel.morph.gui.PicturePane.Role;
import pl.sebcel.morph.model.TransformAnchor;

public class MainFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	private PicturePane sourcePicturePane;
	private PicturePane targetPicturePane;
	private PicturePane sourceTransformPicturePane;
	private PicturePane targetTransformPicturePane;
	private PicturePane outputPicturePane;

	private JSlider phaseSlider = new JSlider(JSlider.HORIZONTAL);
	private JButton autoSlider = new JButton("Buffer");

	private MorphingEngine engine;

	public MainFrame() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension windowSize = new Dimension(1200, 500);
		this.setBounds((screenSize.width - windowSize.width) / 2, (screenSize.height - windowSize.height) / 2, windowSize.width, windowSize.height);
		this.setTitle("Picture Morphing");

		this.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				super.windowClosing(e);
				System.exit(0);
			}
		});

		sourcePicturePane = new PicturePane(Role.SOURCE);
		targetPicturePane = new PicturePane(Role.TARGET);
		sourceTransformPicturePane = new PicturePane(Role.SOURCE_TRANSFORMED);
		targetTransformPicturePane = new PicturePane(Role.TARGET_TRANSFORMED);
		outputPicturePane = new PicturePane(Role.OUTPUT);

		sourcePicturePane.setMainFrame(this);
		targetPicturePane.setMainFrame(this);
		sourceTransformPicturePane.setMainFrame(this);
		targetTransformPicturePane.setMainFrame(this);
		outputPicturePane.setMainFrame(this);

		sourcePicturePane.setShowAnchors(true);
		targetPicturePane.setShowAnchors(true);
		sourceTransformPicturePane.setShowAnchors(false);
		targetTransformPicturePane.setShowAnchors(false);
		outputPicturePane.setShowAnchors(false);

		phaseSlider.setMinimum(0);
		phaseSlider.setMaximum(8);
		phaseSlider.setValue(4);

		this.setLayout(new GridBagLayout());
		this.add(sourcePicturePane, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 1, 1));
		this.add(targetPicturePane, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 1, 1));
		this.add(outputPicturePane, new GridBagConstraints(2, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 1, 1));
		this.add(sourceTransformPicturePane, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 1, 1));
		this.add(targetTransformPicturePane, new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 1, 1));
		this.add(autoSlider, new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 1, 1));
		this.add(phaseSlider, new GridBagConstraints(1, 2, 2, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(1, 1, 1, 1), 1, 1));

		phaseSlider.addChangeListener(e -> setSliderPosition(phaseSlider.getValue()));
		autoSlider.addActionListener(e -> bufferAllFrames());
	}

	public void setMainMenu(MainMenu mainMenu) {
		this.setJMenuBar(mainMenu);
	}

	public void setMorphingEngine(MorphingEngine engine) {
		this.engine = engine;
		this.sourcePicturePane.setMorphingEngine(engine);
		this.targetPicturePane.setMorphingEngine(engine);
		this.sourceTransformPicturePane.setMorphingEngine(engine);
		this.targetTransformPicturePane.setMorphingEngine(engine);
		this.outputPicturePane.setMorphingEngine(engine);
	}

	@Override
	public void repaint() {
		super.repaint();
		this.sourcePicturePane.repaint();
		this.targetPicturePane.repaint();
		this.sourceTransformPicturePane.repaint();
		this.targetTransformPicturePane.repaint();
		this.outputPicturePane.repaint();
	}

	public void setSelectedAnchor(TransformAnchor anchor) {
		sourcePicturePane.selectAnchor(anchor);
		targetPicturePane.selectAnchor(anchor);
		sourceTransformPicturePane.selectAnchor(anchor);
		targetTransformPicturePane.selectAnchor(anchor);
		outputPicturePane.selectAnchor(anchor);
	}

	private void setSliderPosition(int sliderValue) {
		double phase = (double) sliderValue / phaseSlider.getMaximum();
		engine.setPhase(phase);
	}

	private void bufferAllFrames() {
		new Thread(() -> {
			phaseSlider.setEnabled(false);
			int startPosition = phaseSlider.getMinimum();
			int endPosition = phaseSlider.getMaximum();
			for (int i = startPosition; i <= endPosition; i++) {
				phaseSlider.setValue(i);
			}
			phaseSlider.setEnabled(true);
		}).start();
	}
}