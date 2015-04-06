package mosaicgenerator.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.event.IIOWriteProgressListener;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

public class ImageSaver extends SwingWorker<Boolean, String> 
            implements IIOWriteProgressListener {
   private JProgressBar mStatusBar;
   private BufferedImage mImage;
   private File mOutputFile;
   
   public ImageSaver(JProgressBar statusBar, BufferedImage image, File output) {
      mStatusBar = statusBar;
      mImage = image;
      mOutputFile = output;
      setProgressListener();
   }
   
   private void setProgressListener() {
      Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("png");
      ImageWriter writer = (ImageWriter)writers.next();
      writer.addIIOWriteProgressListener(this);
   }
   
   private void removeProgressListeners() {
      Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("png");
      ImageWriter writer = (ImageWriter)writers.next();
      writer.removeIIOWriteProgressListener(this);
   }
   
   @Override
   protected Boolean doInBackground() {
      try {
         ImageIO.write(mImage, "png", mOutputFile);
         return true;
      } catch(IOException ex) {
         return false;
      }
   }
   
   @Override
   protected void done() {
      mStatusBar.setValue(0);
      removeProgressListeners();
   }

   @Override
   public void imageComplete(ImageWriter arg0) {
      mStatusBar.setValue(0);
   }

   @Override
   public void imageProgress(ImageWriter arg0, float arg1) {
      mStatusBar.setValue((int)arg1);
   }

   @Override
   public void imageStarted(ImageWriter arg0, int arg1) {
      mStatusBar.setValue(0);
   }

   @Override
   public void writeAborted(ImageWriter arg0) {
      mStatusBar.setValue(0);
   }

   @Override
   public void thumbnailComplete(ImageWriter arg0) {}

   @Override
   public void thumbnailProgress(ImageWriter arg0, float arg1) {}

   @Override
   public void thumbnailStarted(ImageWriter arg0, int arg1, int arg2) {}
}
