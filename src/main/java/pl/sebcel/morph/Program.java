package pl.sebcel.morph;

import pl.sebcel.morph.engine.MorphingEngine;
import pl.sebcel.morph.gui.MainFrame;
import pl.sebcel.morph.gui.MainMenu;
import pl.sebcel.morph.utils.FileOperations;

public class Program {

    public static void main(String[] args) {
        new Program().run();
    }

    public void run() {

        MorphingEngine engine = new MorphingEngine();
        FileOperations fileOperations = new FileOperations();

        MainMenu mainMenu = new MainMenu();
        mainMenu.setMorphingEngine(engine);
        mainMenu.setFileOperations(fileOperations);

        MainFrame mainFrame = new MainFrame();
        mainFrame.setMorphingEngine(engine);
        mainFrame.setMainMenu(mainMenu);
        mainFrame.setVisible(true);

        engine.setMainFrame(mainFrame);
    }
}