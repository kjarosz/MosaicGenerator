package mosaicgenerator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.HashMap;

import javax.swing.JButton;

public class ImageDirectory extends JButton {
   private boolean mLoading;
   
   private DirectoryLoader mBackgroundLoader;   
   
   private File mDirectory;
   private HashMap<String, BufferedImage> mImages;
   
   public ImageDirectory() {
      mDirectory = null;
      mLoading = false;
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
           String property = e.getPropertyName();
           if("state".equals(property)) {
              changeState(e.getNewValue().toString());
           } else if("progress".equals(property)) {
              repaint();
           }
        }
      };
   }
   
   private void changeState(String state) {
      if("STARTED".equals(state)) {
         mLoading = true;
      } else if("DONE".equals(state)) {
         mLoading = false;
         mBackgroundLoader = null;
      }
      repaint();
   }
   
   public File getDirectory() {
      return mDirectory;
   }
   
   private Dimension getButtonSize() {
      return new Dimension(100, 50);
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
      drawStatusBar(g);
      drawStatusString(g);
      drawDirectoryString(g);
   }
   
   private void drawStatusBar(Graphics g) {
      int width = getWidth();
      int height = getHeight();
      
      drawRect(g, width, height, Color.GRAY);
      
      if(mLoading) {
         float progress = mBackgroundLoader.getProgress()/100.0f;
         drawRect(g, (int)(width*progress), height, Color.GREEN);
      }
   }
   
   private void drawRect(Graphics g, int width, int height, Color color) {
      Color oldColor = g.getColor();
      g.setColor(color);
      g.fillRect(0, 0, width, height);
      g.setColor(oldColor);
   }
   
   private void drawStatusString(Graphics g) {
      String text = getText();
      g.drawString(text, 5, 20);
   }
   
   private void drawDirectoryString(Graphics g) {
      if(mDirectory != null) {
         String folder = mDirectory.getAbsolutePath();
         g.drawString(folder, 5, 40);
      }
   }
}
