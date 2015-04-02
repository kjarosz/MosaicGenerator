package mosaicgenerator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JButton;

public class ImageButton extends JButton {
   private BufferedImage mImage;
   
   public ImageButton() {
      mImage = null;
      setEnabled(false);
   }
   
   private Dimension getImageSize() {
      if(mImage != null) {
         return new Dimension(mImage.getWidth(), mImage.getHeight());
      } else {
         return new Dimension(0, 0);
      }
   }
   
   public Dimension getMinimumSize() {
      return getImageSize();
   }
   
   public Dimension getPreferredSize() {
      return getImageSize();
   }
   
   public Dimension getMaximumSize() {
      return getImageSize();
   }
   
   public void setImage(BufferedImage image) {
      mImage = image;
   }
   
   public BufferedImage getImage() {
      return mImage;
   }
   
   @Override
   public void paintComponent(Graphics g) {
      Color oldColor = g.getColor();
      g.setColor(Color.GRAY);
      
      g.fillRect(0, 0, getWidth(), getHeight());
      
      g.setColor(oldColor);
      
      if(mImage != null) 
         g.drawImage(mImage, 0, 0, null);
   }
   
}
