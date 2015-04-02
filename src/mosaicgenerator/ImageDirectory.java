package mosaicgenerator;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
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
      mBackgroundLoader.execute();
   }
   
   public File getDirectory() {
      return mDirectory;
   }
   
   @Override
   public void paintComponent(Graphics g) {
      
   }
}
