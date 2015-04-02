package mosaicgenerator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

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
      
      add(tabs);
   }
   
   private void addMainImagePanel(JTabbedPane parent) {
      ActionListener trigger = createTriggerListener();
      MainImagePanel mainImagePanel = new MainImagePanel(trigger);
      parent.addTab("Image", mainImagePanel);
   }
   
   private ActionListener createTriggerListener() {
      return new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            
         }
      };
   }
   
   public static void main(String args[]) {
      SwingUtilities.invokeLater(new Runnable() {
         @Override
         public void run() {
            new MosaicGenerator();
         }
      });
   }
}
