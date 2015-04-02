package mosaicgenerator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class MosaicGenerator extends JFrame {
   
   public MosaicGenerator() {
      super("Mosaic Generator");
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      
      createWidgets();
      
      setSize(800, 600);
      setVisible(true);
      
   }
   
   private void createWidgets() {
      JTabbedPane tabs = new JTabbedPane();
      addMainImagePanel(tabs);      
      addFolderViewer(tabs);
      addSettingsPage(tabs);
      addResultsPage(tabs);
      add(tabs);
   }
   
   private void addMainImagePanel(JTabbedPane parent) {
      ActionListener trigger = createTriggerListener();
      MainImagePanel mainImagePanel = new MainImagePanel(trigger);
      parent.addTab("1. Image", mainImagePanel);
   }
   
   private ActionListener createTriggerListener() {
      return new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            
         }
      };
   }
   
   private void addFolderViewer(JTabbedPane parent) {
      ImageFolders folders = new ImageFolders();
      parent.addTab("2. Folders", folders);
   }
   
   private void addSettingsPage(JTabbedPane parent) {
      SettingsPage settings = new SettingsPage();
      parent.addTab("3. Settings", settings);
   }
   
   private void addResultsPage(JTabbedPane parent) {
      ResultsPage results = new ResultsPage();
      parent.addTab("4. Generator", results);
   }
   
   public static void main(String args[]) {
      SwingUtilities.invokeLater(new Runnable() {
         @Override
         public void run() {
            setLookAndFeel();
            new MosaicGenerator();
         }
      });
   }
   
   private static void setLookAndFeel() {
      try {
         UIManager.setLookAndFeel(
               UIManager.getSystemLookAndFeelClassName());
      } catch(Exception e) {
         JOptionPane.showMessageDialog(null,
               "There has been an error setting the look"
               +" and feel.\nHere's the error in detail:\n\n"
               +e.getMessage(),
               "Error Setting Look and Feel",
               JOptionPane.ERROR_MESSAGE);
      }
   }
}
