package mosaicgenerator.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JPanel;

import mosaicgenerator.utils.DirectoryLoader;
import mosaicgenerator.utils.ProgressiveBilinear;

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
      mLoading = true;
      mSelected = false;
      mHover = false;
      mImages = new HashMap<>();
      mImagePanel = imagePanel;
      mThumbnails = new LinkedList<>();
      addMouseListener(this);
   }
   
   public void addImage(String name, BufferedImage image) {
      image = ProgressiveBilinear.progressiveScale(image, 150, 150);
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
            (e) -> trackProgress(e));
      mBackgroundLoader.execute();
   }
   
   public void removeImages() {
      mThumbnails.stream().forEach((item) -> {
         mImagePanel.remove(item);
      });
   }
   
   public Collection<BufferedImage> images() {
      return mImages.values();
   }
   
   private void trackProgress(PropertyChangeEvent e) {
      String property = e.getPropertyName();
      if(null != property) switch (property) {
         case "state":
            changeState(e.getNewValue().toString());
            break;
         case "progress":
            repaint();
            break;
      }
   }
   
   private void changeState(String state) {
      if(null != state) switch (state) {
         case "STARTED":
            mLoading = true;
            break;
         case "DONE":
            mLoading = false;
            mBackgroundLoader = null;
            break;
      }
      repaint();
   }
   
   public File getDirectory() {
      return mDirectory;
   }
   
   @Override
   public Dimension getMinimumSize() {
      return new Dimension(100, 50);
   }
   
   @Override
   public Dimension getPreferredSize() {
      return new Dimension(0, 50);
   }
   
   @Override
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
