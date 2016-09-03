package pl.sebcel.morph.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import pl.sebcel.morph.engine.MorphingEngine;

public class RenderingControlsPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private JButton precalculateButton = new JButton("Precalculate");
	private JSlider qualitySlider = new JSlider();
	private JSlider phaseSlider = new JSlider();

	private MorphingEngine morphingEngine;

	public void setMorphingEngine(MorphingEngine morphingEngine) {
		this.morphingEngine = morphingEngine;
	}

	public RenderingControlsPanel() {
		this.setLayout(new GridBagLayout());
		this.add(new JLabel("Precalculate all frames"), new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 1, 1));
		this.add(new JLabel("Quality"), new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 1, 1));
		this.add(new JLabel("Frame"), new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 1, 1));
		this.add(precalculateButton, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 1, 1));
		this.add(qualitySlider, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 1, 1));
		this.add(phaseSlider, new GridBagConstraints(2, 1, 1, 1, 4.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(1, 1, 1, 1), 1, 1));

		qualitySlider.setMinimum(0);
		qualitySlider.setMaximum(2);
		qualitySlider.setValue(0);
		qualitySlider.setPaintTicks(true);
		qualitySlider.setPaintLabels(true);
		Dictionary<Integer, JComponent> labels = new Hashtable<>();
		labels.put(0, new JLabel("Low"));
		labels.put(1, new JLabel("Medium"));
		labels.put(2, new JLabel("High"));
		qualitySlider.setLabelTable(labels);

		phaseSlider.setMinimum(0);
		phaseSlider.setMaximum(6);
		phaseSlider.setValue(3);

		phaseSlider.addChangeListener(e -> setSliderPosition(phaseSlider.getValue()));
		precalculateButton.addActionListener(e -> bufferAllFrames());
	}

	private void setSliderPosition(int sliderValue) {
		double phase = (double) sliderValue / phaseSlider.getMaximum();
		morphingEngine.setPhase(phase);
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