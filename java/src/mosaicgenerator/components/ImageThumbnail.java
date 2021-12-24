package mosaicgenerator.components;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class ImageThumbnail extends JLabel {
   private Dimension mThumbnailSize;
   
   public ImageThumbnail(BufferedImage image) {
      super(new ImageIcon(image));
      mThumbnailSize = new Dimension(100, 100);
   }
   
   public void setThumbnailSize(int size) {
      mThumbnailSize.width = size;
      mThumbnailSize.height = size;
   }
   
   public int getThumbnailSize() {
      return mThumbnailSize.width;
   }
   
   public Dimension getMinimumSize() {
      return mThumbnailSize;
   }
   
   public Dimension getPreferredSize() {
      return mThumbnailSize;
   }
   
   public Dimension getMaximumSize() {
      return mThumbnailSize;
   }
}
