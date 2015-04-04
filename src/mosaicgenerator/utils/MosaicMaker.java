package mosaicgenerator.utils;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.util.LinkedList;
import java.util.List;

import javax.swing.SwingWorker;

import mosaicgenerator.MainImagePanel;

public class MosaicMaker extends SwingWorker<BufferedImage, String> {   
   private static class Tile {
      BufferedImage mOriginal;
      BufferedImage mScaled;
      BufferedImage mCellSized;
      int mUseCount;
   }
   
   private MainImagePanel mStatusReporter;
   private BufferedImage mImage;
   private LinkedList<Tile> mTiles;
   
   private Dimension mCellSize;
   private Dimension mTileDimension;
   
   public MosaicMaker(MainImagePanel statusReporter, BufferedImage image,
            LinkedList<BufferedImage> tiles) {
      mStatusReporter = statusReporter;
      mImage = image;
      mTiles = copyTiles(tiles);
      mCellSize = new Dimension(15, 15);
      mTileDimension = new Dimension(150, 150);
   }
   
   private LinkedList<Tile> copyTiles(LinkedList<BufferedImage> tiles) {
      mTiles = new LinkedList<>();
      for(BufferedImage image: tiles) {
         Tile tile = new Tile();
         tile.mOriginal = image;
         tile.mScaled = null;
         tile.mUseCount = 0;
         mTiles.add(tile);
      }
      return mTiles;
   }
   
   @Override
   protected BufferedImage doInBackground() {
      BufferedImage[][] subImages = getSubImages();
      
      if(isCancelled()) return null;
      
      BufferedImage tiles[][] = selectTiles(subImages);
      
      if(isCancelled()) return null;
      
      BufferedImage result = assembleImage(tiles);
      
      return result;
   }
   
   private BufferedImage[][] getSubImages() {
      int cols = (int)(Math.ceil((double)mImage.getWidth()
            /(double)mCellSize.width));
      int rows = (int)(Math.ceil((double)mImage.getHeight()
            /(double)mCellSize.height));
      
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
      int x = mCellSize.width*col;
      int y = mCellSize.height*row;
      int width = (mImage.getWidth() - x < mCellSize.width) ?
            mImage.getWidth() - x : mCellSize.width;
      int height = (mImage.getHeight() - y < mCellSize.height) ?
            mImage.getHeight() - x : mCellSize.height;
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
            publishStatus(2, calculateProgress(++selected, total));
         }
      }
      return selectedTiles;
   }
   
   private BufferedImage selectTile(BufferedImage subImage) {
      int selectedMismatch = Integer.MAX_VALUE;
      Tile selectedTile = null;
      for(Tile tile: mTiles) {
         int mismatch = calculateMismatch(tile, subImage);
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
   
   private int calculateMismatch(Tile tile, BufferedImage cell) {
      prepareTile(tile, cell.getType());
      byte cellRaster[] = getData(cell);
      byte tileRaster[] = getData(tile.mCellSized);
      int difference = 0;
      if(cell.getAlphaRaster() != null) {
         difference = normalizeAlphaPixelDiff(cellRaster, tileRaster);
      } else {
         difference = normalizePixelDiff(cellRaster, tileRaster);
      }
      return difference + tile.mUseCount*15;
   }
   
   private int normalizeAlphaPixelDiff(byte cellRaster[], byte tileRaster[]) {
      long r = 0, g = 0, b = 0;
      float total = mCellSize.width * mCellSize.height;
      for(int pxOffset = 0; pxOffset < cellRaster.length; pxOffset += 4) {
         int dr = ((int)cellRaster[pxOffset+3] & 0xff) - ((int)tileRaster[pxOffset+3] & 0xff);
         int dg = ((int)cellRaster[pxOffset+2] & 0xff) - ((int)tileRaster[pxOffset+2] & 0xff);
         int db = ((int)cellRaster[pxOffset+1] & 0xff) - ((int)tileRaster[pxOffset+1] & 0xff);
         
         r += dr*dr;
         g += dg*dg;
         b += db*db;
      }
      return (int)Math.sqrt(r/total + g/total + b/total);
   }
   
   private int normalizePixelDiff(byte cellRaster[], byte tileRaster[]) {
      long r = 0, g = 0, b = 0;
      float total = mCellSize.width * mCellSize.height;
      for(int pxOffset = 0; pxOffset < cellRaster.length; pxOffset += 3) {
         int dr = ((int)cellRaster[pxOffset+2] & 0xff) - ((int)tileRaster[pxOffset+2] & 0xff);
         int dg = ((int)cellRaster[pxOffset+1] & 0xff) - ((int)tileRaster[pxOffset+1] & 0xff);
         int db = ((int)cellRaster[pxOffset] & 0xff) - ((int)tileRaster[pxOffset] & 0xff);
         
         r += dr*dr;
         g += dg*dg;
         b += db*db;
      }
      return (int)Math.sqrt(r/total + g/total + b/total);
   }
   
   private void prepareTile(Tile tile, int cellType) {
      if (tile.mScaled == null) {
         tile.mScaled = ProgressiveBilinear.progressiveScale(
               tile.mOriginal, mTileDimension.width, mTileDimension.height);
      }
      if (tile.mCellSized == null) {
         tile.mCellSized = new BufferedImage(
               mCellSize.width, 
               mCellSize.height, 
               cellType);
         Graphics2D g2 = tile.mCellSized.createGraphics();
         g2.drawImage(tile.mScaled, 0, 0, 
               tile.mCellSized.getWidth(), 
               tile.mCellSized.getHeight(), 
               null);
         g2.dispose();
      }
   }
   
   private byte[] getData(BufferedImage img) {
      Raster rasta = img.getData();
      DataBuffer buff = rasta.getDataBuffer();
      DataBufferByte byteBuff = (DataBufferByte)buff;
      return byteBuff.getData();
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
