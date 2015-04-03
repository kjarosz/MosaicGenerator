package mosaicgenerator.utils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

import javax.swing.SwingWorker;

import mosaicgenerator.MainImagePanel;

public class MosaicMaker extends SwingWorker<BufferedImage, String> {
   private static class Pixel {
      int r;
      int g;
      int b;
   }
   
   private static class Tile {
      BufferedImage mOriginal;
      BufferedImage mScaled;
      Pixel mAvgPixel;
      int mUseCount;
   }
   
   private MainImagePanel mStatusReporter;
   private BufferedImage mImage;
   private LinkedList<Tile> mTiles;
   
   private Dimension mSubImageDimension;
   private Dimension mTileDimension;
   
   public MosaicMaker(MainImagePanel statusReporter, BufferedImage image,
            LinkedList<BufferedImage> tiles) {
      mStatusReporter = statusReporter;
      mImage = image;
      mTiles = copyTiles(tiles);
      mSubImageDimension = new Dimension(30, 30);
      mTileDimension = new Dimension(100, 100);
   }
   
   private LinkedList<Tile> copyTiles(LinkedList<BufferedImage> tiles) {
      mTiles = new LinkedList<>();
      for(BufferedImage image: tiles) {
         Tile tile = new Tile();
         tile.mOriginal = image;
         tile.mScaled = null;
         tile.mUseCount = 0;
         tile.mAvgPixel = null;
         mTiles.add(tile);
      }
      return mTiles;
   }
   
   @Override
   protected BufferedImage doInBackground() {
      BufferedImage[][] subImages = getSubImages();
      if(isCancelled()) {
         return null;
      }
      BufferedImage result = makeMosaic(subImages);
      return result;
   }
   
   private BufferedImage[][] getSubImages() {
      int cols = (int)(Math.ceil((double)mImage.getWidth()
            /(double)mSubImageDimension.width));
      int rows = (int)(Math.ceil((double)mImage.getHeight()
            /(double)mSubImageDimension.height));
      
      int processed = 0, total = rows*cols;
      
      BufferedImage subImages[][] = new BufferedImage[cols][];
      for(int i = 0; i < subImages.length; i++) {
         
         subImages[i] = new BufferedImage[rows];
         for(int j = 0; j < subImages[i].length; j++) {
            if(isCancelled()) {
               return null;
            }
            
            subImages[i][j] = getSubImage(i, j);
            
            publishStatus(1, calculateProgress(++processed, total));
         }
      }
      
      return subImages;
   }
   
   private BufferedImage getSubImage(int col, int row) {
      int x = mSubImageDimension.width*col;
      int y = mSubImageDimension.height*row;
      int width = (mImage.getWidth() - x < mSubImageDimension.width) ?
            mImage.getWidth() - x : mSubImageDimension.width;
      int height = (mImage.getHeight() - y < mSubImageDimension.height) ?
            mImage.getHeight() - x : mSubImageDimension.height;
      int type = mImage.getType();
      
      BufferedImage subImage = new BufferedImage(width, height, type);
      Graphics2D g2 = subImage.createGraphics();
      g2.drawImage(mImage, 
            0, 0, width, height,
            x, y, x + width, y + height,
            null);
      g2.dispose();
      return subImage;
   }
   
   private BufferedImage makeMosaic(BufferedImage subImages[][]) {
      BufferedImage tiles[][] = selectTiles(subImages);
      if(isCancelled()) {
         return null;
      }
      BufferedImage result = assembleImage(tiles);
      return result;
   }
   
   private BufferedImage[][] selectTiles(BufferedImage subImages[][]) {
      int selected = 0, total = subImages.length*subImages[0].length;
      
      BufferedImage selectedTiles[][] = new BufferedImage[subImages.length][];
      for(int i = 0; i < selectedTiles.length; i++) {
         selectedTiles[i] = new BufferedImage[subImages[i].length];
         for(int j = 0; j < selectedTiles[i].length; j++) {
            if(isCancelled()) {
               return null;
            }
            selectedTiles[i][j] = selectTile(subImages[i][j]);
            publishStatus(2, calculateProgress(selected, total));
         }
      }
      return selectedTiles;
   }
   
   private BufferedImage selectTile(BufferedImage subImage) {
      int selectedMismatch = Integer.MAX_VALUE;
      Tile selectedTile = null;
      Pixel avgPixel = calculateAvgPixel(subImage);
      for(Tile tile: mTiles) {
         int mismatch = calculateMismatch(tile, avgPixel);
         if(mismatch < selectedMismatch) {
            selectedMismatch = mismatch;
            selectedTile = tile;
         } else if (mismatch == selectedMismatch) {
            if (tile.mUseCount < selectedTile.mUseCount) {
               selectedTile = tile;
            }
         }
      }
      selectedTile.mUseCount++;
      return selectedTile.mScaled;
   }
   
   private Pixel calculateAvgPixel(BufferedImage img) {
      Pixel px = new Pixel();
      long r = 0, g = 0, b = 0;
      for(int i = 0; i < img.getWidth(); i++) {
         for(int j = 0; j < img.getHeight(); j++) {
            Color pixel = new Color(img.getRGB(i, j));
            r += pixel.getRed();
            g += pixel.getGreen();
            b += pixel.getBlue();
         }
      }
      int pixelCount = img.getWidth()*img.getHeight();
      px.r = (int)(r/pixelCount);
      px.g = (int)(g/pixelCount);
      px.b = (int)(b/pixelCount);
      return px;
   }
   
   private int calculateMismatch(Tile tile, Pixel avgPixel) {
      if (tile.mScaled == null) {
         tile.mScaled = ProgressiveBilinear.progressiveScale(
               tile.mOriginal, mTileDimension.width, mTileDimension.height);
      }
      if(tile.mAvgPixel == null) {
         tile.mAvgPixel = calculateAvgPixel(tile.mScaled);
      }
      return Math.abs(tile.mAvgPixel.r - avgPixel.r)
           + Math.abs(tile.mAvgPixel.g - avgPixel.g)
           + Math.abs(tile.mAvgPixel.b - avgPixel.b)
           + tile.mUseCount*10;
   }
   
   private BufferedImage assembleImage(BufferedImage tiles[][]) {
      BufferedImage image = createMatchingImage(tiles);
      drawToImage(image, tiles);
      return image;
   }
   
   private BufferedImage createMatchingImage(BufferedImage tiles[][]) {
      int cols = tiles.length;
      int rows = tiles[0].length;
      int width = cols*mTileDimension.width;
      int height = rows*mTileDimension.height;
      int type = mImage.getType();
      return new BufferedImage(width, height, type);
   }
   
   private void drawToImage(BufferedImage destination, 
                            BufferedImage tiles[][]) {
      int processed = 0, total = tiles.length*tiles[0].length;
      Graphics2D g = destination.createGraphics();
      for(int i = 0; i < tiles.length; i++) {
         for(int j = 0; j < tiles[i].length; j++) {
            if(isCancelled()) return;
            
            g.drawImage(tiles[i][j], 
                        i*mTileDimension.width, 
                        j*mTileDimension.height,
                        mTileDimension.width,
                        mTileDimension.height,
                        null);
            
            publishStatus(3, calculateProgress(++processed, total));
         }
      }
   }
   
   private int calculateProgress(int processed, int total) {
      float progress = (float)processed/(float)total;
      return (int)(progress*100.0);
   }
   
   private void publishStatus(int stage, int progress) {
      setProgress(progress);
      publish("Stop (" + stage + "/3 - " 
               + progress + "% )");
   }
   
   @Override
   protected void process(List<String> messages) {
      if(!messages.isEmpty()) {
         String message = messages.get(messages.size()-1);
         mStatusReporter.setStatus(false, message);
      }
   }
   
   @Override
   protected void done() {
      mStatusReporter.setStatus(true, null);
   }
}
