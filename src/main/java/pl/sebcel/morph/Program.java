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
        MorphingEngine morphingEngine = new MorphingEngine();
        FileOperations fileOperations = new FileOperations();
        ApplicationLogic applicationLogic = new ApplicationLogic();
        MainMenu mainMenu = new MainMenu();
        MainFrame mainFrame = new MainFrame();

        mainMenu.setFileOperations(fileOperations);
        mainMenu.setApplicationLogic(applicationLogic);

        mainFrame.setApplicationLogic(applicationLogic);
        mainFrame.setMainMenu(mainMenu);
        mainFrame.setVisible(true);

        applicationLogic.setMainFrame(mainFrame);
        applicationLogic.setMorphingEngine(morphingEngine);
        applicationLogic.setFileOperations(fileOperations);
    }
}