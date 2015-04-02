package mosaicgenerator;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.HashMap;

import javax.swing.JButton;

public class ImageDirectory extends JButton {
   private DirectoryLoader mBackgroundLoader;   
   
   private File mDirectory;
   private HashMap<String, BufferedImage> mImages;
   
   public ImageDirectory() {
      mDirectory = null;
      mImages = new HashMap<>();
   }
   
   public void addImage(String name, BufferedImage image) {
      mImages.put(name, image);
   }
   
   public void loadImages(File directory) {
      mDirectory = directory;
      mBackgroundLoader = new DirectoryLoader(this);
      mBackgroundLoader.addPropertyChangeListener(
            getProgressTracker());
      mBackgroundLoader.execute();
   }
   
   private PropertyChangeListener getProgressTracker() {
      return new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent e) {
           
        }
      };
   }
   
   public File getDirectory() {
      return mDirectory;
   }
   
   private Dimension getButtonSize() {
      return new Dimension(0, 50);
   }
   
   public Dimension getMinimumSize() {
      return getButtonSize();
   }
   
   public Dimension getPreferredSize() {
      return getButtonSize();
   }
   
   public Dimension getMaximumSize() {
      return getButtonSize();
   }
   
   @Override
   public void paintComponent(Graphics g) {
      // Man, drawing these buttons tho
      
      
   }
}
