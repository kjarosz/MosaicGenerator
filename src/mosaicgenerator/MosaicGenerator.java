package mosaicgenerator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import mosaicgenerator.utils.MosaicMaker;

public class MosaicGenerator extends JFrame {
   private JTabbedPane mTabbedPane;   
   private MainImagePanel mImagePanel;
   private ImageFolders mFoldersPanel;
   private ResultsPage mResultsPage;    
   
   private MosaicMaker mMosaicMaker;
   
   public MosaicGenerator() {
      super("Mosaic Generator");
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      
      createWidgets();
      
      setSize(800, 600);
      setVisible(true);
      
   }
   
   private void createWidgets() {
      mTabbedPane = new JTabbedPane();
      addMainImagePanel(mTabbedPane);      
      addFolderViewer(mTabbedPane);
      addSettingsPage(mTabbedPane);
      addResultsPage(mTabbedPane);
      add(mTabbedPane);
   }
   
   private void addMainImagePanel(JTabbedPane parent) {
      ActionListener trigger = createTriggerListener();
      mImagePanel = new MainImagePanel(trigger);
      parent.addTab("1. Image", mImagePanel);
   }
   
   private ActionListener createTriggerListener() {
      return new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            if(mMosaicMaker == null) {
               if(isPrepared()) {
                  BufferedImage startImage = mImagePanel.image();
                  LinkedList<BufferedImage> images = mFoldersPanel.images();
                  makeMosaic(startImage, images);
               } 
            } else {
               mMosaicMaker.cancel(true);
            }
         }
      };
   }
   
   private boolean isPrepared() {
      if(!mImagePanel.hasImage()) {
         JOptionPane.showMessageDialog(this,
               "You have to select an image first.",
               "No Image",
               JOptionPane.WARNING_MESSAGE);
         return false;
      }
      
      if(!mFoldersPanel.hasImages()) {
         mTabbedPane.setSelectedIndex(1);
         return false;
      }
      
      return true;
   }
   
   private void makeMosaic(BufferedImage image, 
                           LinkedList<BufferedImage> images) {
      mMosaicMaker = new MosaicMaker(mImagePanel, image, images);
      mMosaicMaker.addPropertyChangeListener(createGeneratorListener());
      mMosaicMaker.execute();
   }
   
   private PropertyChangeListener createGeneratorListener() {
      return new PropertyChangeListener() {
         @Override
         public void propertyChange(PropertyChangeEvent e) {
            String property = e.getPropertyName();
            if(!"state".equals(property)) {
               return;
            }
            
            String state = e.getNewValue().toString();
            if(!"DONE".equals(state)) {
               return;
            }
            
            if(mMosaicMaker.isCancelled()) {
               mMosaicMaker = null;
            } else {
               try {
                  BufferedImage result = mMosaicMaker.get();
                  mResultsPage.publishResult(result);
               } catch(Exception error) {
                  JOptionPane.showMessageDialog(
                        null,
                        "Something went wrong retrieving results.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                  System.out.println(error.getMessage());
               } finally {
                  mMosaicMaker = null;
               }
            }
         }
      };
   }
   
   private void addFolderViewer(JTabbedPane parent) {
      mFoldersPanel = new ImageFolders();
      parent.addTab("2. Folders", mFoldersPanel);
   }
   
   private void addSettingsPage(JTabbedPane parent) {
      ActionListener saveAction = makeSaveSettingAction();
      SettingsPage settings = new SettingsPage(saveAction);
      parent.addTab("3. Settings", settings);
   }
   
   private ActionListener makeSaveSettingAction() {
      return new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            
         }
      };
   }
   
   private void addResultsPage(JTabbedPane parent) {
      mResultsPage = new ResultsPage();
      parent.addTab("4. Generator", mResultsPage);
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
