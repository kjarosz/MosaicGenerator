package mosaicgenerator.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JButton;

import mosaicgenerator.utils.ProgressiveBilinear;

public class ImageThumbnail extends JButton {
   private BufferedImage mImage;
   private BufferedImage mScaledImage;
   
   private Dimension mThumbnailSize;
   
   public ImageThumbnail(BufferedImage image) {
      mImage = image;
      mScaledImage = null;
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
   
   @Override
   public void paintComponent(Graphics g) {
      Color oldColor = g.getColor();
      g.setColor(Color.BLACK);
      g.fillRect(0, 0, getWidth(), getHeight());
      g.setColor(oldColor);
      
      scaleImage();
      if(mScaledImage != null) {
         g.drawImage(mImage, 0, 0, getWidth(), getHeight(), null);
      }
   }
   
   private void scaleImage() {
      if((mScaledImage == null && mImage != null) ||
            getWidth() != mScaledImage.getWidth() ||
            getHeight() != mScaledImage.getHeight()) {
         mScaledImage = ProgressiveBilinear.progressiveScale(
                     mImage, getWidth(), getHeight());
      }
   }
}
