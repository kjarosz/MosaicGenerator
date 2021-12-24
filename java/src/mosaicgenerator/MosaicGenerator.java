package mosaicgenerator;

import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import mosaicgenerator.utils.MosaicMaker;
import mosaicgenerator.utils.MosaicMakerCallback;
import mosaicgenerator.utils.Settings;

public class MosaicGenerator extends JFrame {
   private JTabbedPane mTabbedPane;   
   private MainImagePanel mImagePanel;
   private ImageFolders mFoldersPanel;
   private ResultsPage mResultsPage;    
   
   private Settings mSettings;
   private MosaicMaker mMosaicMaker;
   
   public MosaicGenerator() {
      super("Mosaic Generator");
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      
      mSettings = makeDefaultSettings();
      createWidgets();
      
      setSize(800, 600);
      setVisible(true);
   }
   
   private Settings makeDefaultSettings() {
      Settings settings = new Settings();
      settings.cellWidth = settings.cellHeight = 15;
      settings.tileWidth = settings.tileHeight = 150;
      settings.reuseTiles = false;
      settings.reusePenalty = 15;
      return settings;
   }
   
   private void createWidgets() {
      mTabbedPane = new JTabbedPane();
      addMainImagePanel(mTabbedPane);      
      addFolderViewer(mTabbedPane);
      addResultsPage(mTabbedPane);
      add(mTabbedPane);
   }
   
   private void addMainImagePanel(JTabbedPane parent) {
      mImagePanel = new MainImagePanel();
      parent.addTab("1. Source Image", mImagePanel);
   }
   
   private void addFolderViewer(JTabbedPane parent) {
      mFoldersPanel = new ImageFolders();
      parent.addTab("2. Source Tiles", mFoldersPanel);
   }
   
   private void addResultsPage(JTabbedPane parent) {
      mResultsPage = new ResultsPage(createMosaicMakerCallback());
      parent.addTab("3. Mosaic", mResultsPage);
   }
   
   private MosaicMakerCallback createMosaicMakerCallback() {
      return new MosaicMakerCallback() {
         @Override
         public void makeMosaic(JProgressBar listener, PropertyChangeListener stateListener) {
            if(isPrepared()) {
               BufferedImage startImage = mImagePanel.image();
               LinkedList<BufferedImage> images = mFoldersPanel.images();
               mMosaicMaker = new MosaicMaker(listener, startImage, images, mSettings);
               mMosaicMaker.addPropertyChangeListener(stateListener);
               mMosaicMaker.execute();
            }
         }
         
         @Override
         public void stopMosaic() {
            mMosaicMaker.cancel(true);
            mMosaicMaker = null;
         }
      };
   }
   
   private boolean isPrepared() {
      if(!mImagePanel.hasImage()) {
         JOptionPane.showMessageDialog(this,
               "You have to load a base image first.",
               "No Image",
               JOptionPane.WARNING_MESSAGE);
         mTabbedPane.setSelectedIndex(0);
         return false;
      }
      
      if(!mFoldersPanel.hasImages()) {
         JOptionPane.showMessageDialog(this,
               "You have to load some images first.",
               "No Images",
               JOptionPane.WARNING_MESSAGE);
         mTabbedPane.setSelectedIndex(1);
         return false;
      }
      
      return true;
   }
   
   public static void main(String args[]) {
      SwingUtilities.invokeLater(() -> {
         setLookAndFeel();
         new MosaicGenerator();
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
