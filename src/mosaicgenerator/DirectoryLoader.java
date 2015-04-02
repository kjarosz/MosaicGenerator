package mosaicgenerator;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.SwingWorker;

public class DirectoryLoader extends SwingWorker<ImageDirectory, String> {
   private ImageDirectory mImageDirectory;
   
   public DirectoryLoader(ImageDirectory progressButton) {
      mImageDirectory = progressButton;
   }
   
   @Override
   protected ImageDirectory doInBackground() {
      File imageFiles[] = findFiles();
      loadImages(imageFiles);
      return mImageDirectory;
   }
   
   private File[] findFiles() {
      publish("Selecting files.");
      File directory = mImageDirectory.getDirectory();
      FileFilter filter = getImageFileFilter();
      return directory.listFiles(filter);
   }
   
   private FileFilter getImageFileFilter() {
      return new FileFilter() {
         @Override
         public boolean accept(File file) {
            String name = file.getName();
            String extensions[] = {".jpg", ".jpeg", ".gif", ".png"};
            for(String ext: extensions) {
               if(name.endsWith(ext)) {
                  return true;
               }
            }
            return false;
         }
      };
   }
   
   private void loadImages(File imageFiles[]) {
      int total = imageFiles.length, loaded = 0;
      for(File imageFile: imageFiles) {
         if(isCancelled()) {
            break;
         }
         
         try {
            BufferedImage image = ImageIO.read(imageFile);
            mImageDirectory.addImage(imageFile.getName(), image);
            updateProgress(loaded, total);
         } catch(IOException ignore) {
            
         }
      }
   }
   
   private void updateProgress(int loaded, int total) {
      int progress = calculateProgress(loaded, total);
      publish("Loading " + progress + "%");
      setProgress(progress);
   }
   
   private int calculateProgress(int loaded, int total) {
      float progress = ((float)loaded)/((float)total);
      return (int)(progress*100.0);
   }
   
   @Override
   protected void process(List<String> progress) {
      if(!progress.isEmpty()) {
         String text = progress.get(progress.size()-1);
         mImageDirectory.setText(text);
      }
   }
   
   @Override
   protected void done() {
      if(isCancelled()) {
         publish("Cancelled");
      } else {
         publish("Finished");
      }
   }
}
