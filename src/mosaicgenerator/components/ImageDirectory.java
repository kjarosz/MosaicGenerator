package mosaicgenerator.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JPanel;

import mosaicgenerator.utils.DirectoryLoader;

public class ImageDirectory extends JButton implements MouseListener {
   private final Color DISABLED = Color.CYAN;
   private final Color DISABLED_HOVER = new Color(0, 160, 160);
   private final Color ENABLED = Color.GREEN;
   private final Color ENABLED_HOVER = new Color(0, 160, 0);
   
   private DirectoryLoader mBackgroundLoader;    
   private boolean mLoading;
   
   private boolean mSelected;
   private boolean mHover;
   
   private File mDirectory;
   private HashMap<String, BufferedImage> mImages;
   
   private JPanel mImagePanel;
   private LinkedList<ImageThumbnail> mThumbnails; 
   
   public ImageDirectory(JPanel imagePanel) {
      mDirectory = null;
      mLoading = false;
      mSelected = false;
      mHover = false;
      mImages = new HashMap<>();
      mImagePanel = imagePanel;
      mThumbnails = new LinkedList<>();
      addMouseListener(this);
   }
   
   public void addImage(String name, BufferedImage image) {
      mImages.put(name, image);
      ImageThumbnail thumb = new ImageThumbnail(image);
      mThumbnails.add(thumb);
      mImagePanel.add(thumb);
      mImagePanel.revalidate();
      mImagePanel.repaint();
   }
   
   public void loadImages(File directory) {
      mDirectory = directory;
      mBackgroundLoader = new DirectoryLoader(this);
      mBackgroundLoader.addPropertyChangeListener(
            getProgressTracker());
      mBackgroundLoader.execute();
   }
   
   public void removeImages() {
      for(ImageThumbnail item: mThumbnails) {
         mImagePanel.remove(item);
      }
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
   
   public Dimension getMinimumSize() {
      return new Dimension(100, 50);
   }
   
   public Dimension getMaximumSize() {
      return new Dimension(Integer.MAX_VALUE, 50);
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
      
      drawBackground(g, width, height);
      
      if(mLoading) {
         float progress = mBackgroundLoader.getProgress()/100.0f;
         drawRect(g, (int)(width*progress), height, Color.GREEN);
      }
   }
   
   private void drawBackground(Graphics g, int width, int height) {
      if(mSelected) {
         if(mHover) {
            drawRect(g, width, height, ENABLED_HOVER);
         } else {
            drawRect(g, width, height, ENABLED);
         }
      } else {
         if(mHover) {
            drawRect(g, width, height, DISABLED_HOVER);
         } else {
            drawRect(g, width, height, DISABLED);
         }
      }
   }
   
   private void drawRect(Graphics g, int width, int height, Color color) {
      Color oldColor = g.getColor();
      g.setColor(color);
      g.fillRect(0, 0, width, height);
      g.setColor(oldColor);
   }
   
   private void drawStatusString(Graphics g) {
      if(mLoading) {
         String text = getText();
         g.drawString(text, 5, 20);
      } else {
         g.drawString("Included.", 5, 20);
      }
   }
   
   private void drawDirectoryString(Graphics g) {
      if(mDirectory != null) {
         String folder = mDirectory.getAbsolutePath();
         g.drawString(folder, 5, 40);
      }
   }
   
   public boolean selected() {
      return mSelected;
   }

   @Override
   public void mouseClicked(MouseEvent arg0) {
      mSelected = !mSelected;
   }
   @Override
   public void mouseEntered(MouseEvent arg0) {
      mHover = true;      
   }
   @Override
   public void mouseExited(MouseEvent arg0) {
      mHover = false;
   }

   @Override
   public void mousePressed(MouseEvent arg0) {}
   @Override
   public void mouseReleased(MouseEvent arg0) {}
}
