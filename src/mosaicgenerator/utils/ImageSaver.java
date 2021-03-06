package mosaicgenerator.utils;

import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.event.IIOWriteProgressListener;
import javax.imageio.stream.FileImageOutputStream;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

public class ImageSaver extends SwingWorker<Boolean, String> 
            implements IIOWriteProgressListener,
                       PropertyChangeListener {
   private JProgressBar mStatusBar;
   private BufferedImage mImage;
   private File mOutputFile;
   private ImageWriter mImageWriter;
   
   public ImageSaver(JProgressBar statusBar, BufferedImage image, File output) {
      mStatusBar = statusBar;
      mImage = image;
      mOutputFile = output;
      addPropertyChangeListener(this);
   }
   
   private void setProgressListener(ImageWriter writer) {
      writer.addIIOWriteProgressListener(this);
   }
   
   private void removeProgressListeners(ImageWriter writer) {
      writer.removeIIOWriteProgressListener(this);
   }
   
   private ImageWriter getWriter() {
      return (ImageWriter)ImageIO.getImageWritersByFormatName("png").next();
   }
   
   @Override
   protected Boolean doInBackground() {
      mImageWriter = getWriter();
      try (FileImageOutputStream ifoStream = new FileImageOutputStream(mOutputFile))
      {
         mImageWriter.setOutput(ifoStream);
         setProgressListener(mImageWriter);
         mImageWriter.write(mImage);
         removeProgressListeners(mImageWriter);
         mImageWriter = null;
         return true;
      } catch(IOException ex) {
         return false;
      } 
   }
   
   @Override
   protected void done() {
      mStatusBar.setValue(0);
   }

   @Override
   public void propertyChange(PropertyChangeEvent e) {
      String property = e.getPropertyName();
      if("state".equals(property))
      {
         if(isCancelled() && mImageWriter != null) {
            mImageWriter.abort();
         }
      }      
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
