package mosaicgenerator.utils;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;

public class ProgressiveBilinear {
   public static BufferedImage progressiveScale(BufferedImage image, 
         int width, int height) 
   {
      if(width == image.getWidth() && height == image.getHeight()) {
         return image;
      }
      
      int type = (image.getTransparency() == Transparency.OPAQUE) ?
            BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
      BufferedImage ret = (BufferedImage)image;
      BufferedImage scratchImage = null;
      
      Graphics2D g2 = null;
      int w, h;
      int prevW = ret.getWidth();
      int prevH = ret.getHeight();
      
      // Use multistep technique: start with original size,
      // then scale down in multiple passes with drawImage()
      // until the target size is reached
      w = image.getWidth();
      h = image.getHeight();
      
      do {
         if (w > width) {
            w /= 2;
            if (w < width) {
               w = width;
            }
         }
         if (h > height) {
            h /= 2;
            if (h < height) {
               h = height;
            }
         }
         
         if (scratchImage == null) {
            // Use a single scratch buffer for all iterations
            // and then copy to the final, correctly sized image
            // before returning
            scratchImage = new BufferedImage(w, h, type);
            g2 = scratchImage.createGraphics();
         }
         
         g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
               RenderingHints.VALUE_INTERPOLATION_BILINEAR);
         g2.drawImage(ret, 0, 0, w, h, 0, 0, prevW, prevH, null);
         prevW = w;
         prevH = h;
         ret = scratchImage;
      } while (w != width || h != height);
      
      if (g2 != null) {
         g2.dispose();
      }
      
      // If we used a scratch buffer that is larger than our
      // target size, create an image of the right size and copy
      // the results into it
      if (width != ret.getWidth() || height != ret.getHeight()) {
         scratchImage = new BufferedImage(width, height, type);
         g2 = scratchImage.createGraphics();
         g2.drawImage(ret, 0, 0, null);
         g2.dispose();
         ret = scratchImage;
      }
      return ret;
   }
}
