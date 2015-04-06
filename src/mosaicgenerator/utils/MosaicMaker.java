package mosaicgenerator.utils;

/* ************************************************************************* *
 *                                Mosaic Maker                               *
 * ************************************************************************* *
 *                                                                           *
 * Description:                                                              *
 *                                                                           *
 * The following algorithm converts an input image into a mosaic of out of   *
 * smaller sub images. There are three general steps to accomplish this:     *
 *                                                                           *
 *  1. Divide original image.                                                *
 *                                                                           *
 *    In this step, the algorithm takes the input image and divides it into  *
 * cells that will then be replaced with the tiles (provided in the          *
 * constructor). The cells do not have to be the same size as the tiles, but *
 * the correspondence between cell and tile will be 1-to-1, which means each *
 * cell will be replaced by exactly one tile (repeating or not).             *
 *                                                                           *
 * 2. Select matching tiles.                                                 *
 *                                                                           *
 *    Now the algorithm will go through each cell and compare it to all the  *
 * input tiles. First, if the cell is a different size than the tiles, then  *
 * the tiles will be scaled to the size of the cell (this will happen only   *
 * once and the scaled versions will be saved until the algorithm runs,      *
 * since all the cells are the same size). Then the cells will be compared   *
 * with each tile pixel-by-pixel using the manhattan distance. The           *
 * calculated difference between each cell and tile is (I think at least)    *
 * the root-mean-square value of each manhattan distance vector. If a tile   *
 * has been used multiple times, then the mismatch value will be penalized   *
 * by 15 for each time the tile has been used. All the selected tiles will   *
 * be returned in an array (again, 1-to-1 mapping to the cells from the      *
 * original image).                                                          *
 *                                                                           *
 * 3. Draw the image.                                                        *
 *                                                                           *
 *    This is fairly straight forward. The algorithm just takes all the      *
 * tiles and pieces them into one big image. Be wary of the size. The        *
 * resulting image can be quite large.                                       *
 *                                                                           *
 * ************************************************************************* */

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
      byte[] mCellSizedRaster;
      int mUseCount;
   }
   
   private MainImagePanel mStatusReporter;
   private BufferedImage mImage;
   private LinkedList<Tile> mTiles;
   
   private Dimension mCellSize;
   private Dimension mTileDimension;
   private int mReusePenalty;
   
   /**
    * Prepares the worker thread by setting up the initial state.
    * 
    * @param statusReporter Object that contains the element which
    *                       reports the progress status of the algorithm.
    * @param image          Image that will be turned to a mosaic.
    * @param tiles          Images to be used as tiles in creating the mosaic.
    */
   public MosaicMaker(MainImagePanel statusReporter, BufferedImage image,
            LinkedList<BufferedImage> tiles, Settings settings) {
      mStatusReporter = statusReporter;
      mImage = image;
      mTiles = copyTiles(tiles);
      mCellSize = new Dimension(settings.cellWidth, settings.cellHeight);
      mTileDimension = new Dimension(settings.tileWidth, settings.tileHeight);
      mReusePenalty = settings.reusePenalty;
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
      long nanoTime = System.nanoTime();
      
      BufferedImage[][] subImages = getCells();
      
      if(isCancelled()) return null;
      
      BufferedImage tiles[][] = selectTiles(subImages);
      
      if(isCancelled()) return null;
      
      BufferedImage result = assembleImage(tiles);
      
      System.out.println((System.nanoTime() - nanoTime)*Math.pow(10, -9));
      
      return result;
   }
   
   /* *********************************************************************** *
    * Step 1 - Divide image into cells.                                       *
    * *********************************************************************** */
   private BufferedImage[][] getCells() {
      int cols = (int)(Math.ceil((double)mImage.getWidth()
            /(double)mCellSize.width));
      int rows = (int)(Math.ceil((double)mImage.getHeight()
            /(double)mCellSize.height));
      
      int processed = 0, total = rows*cols;
      
      BufferedImage cells[][] = new BufferedImage[cols][];
      for(int i = 0; i < cells.length; i++) {
         
         cells[i] = new BufferedImage[rows];
         for(int j = 0; j < cells[i].length; j++) {
            if(isCancelled()) {
               return null;
            }
            
            cells[i][j] = getCell(i, j);
            
            publishStatus(1, calculateProgress(++processed, total));
         }
      }
      
      return cells;
   }
   
   private BufferedImage getCell(int col, int row) {
      int x = mCellSize.width*col;
      int y = mCellSize.height*row;
      int width = (mImage.getWidth() - x < mCellSize.width) ?
            mImage.getWidth() - x : mCellSize.width;
      int height = (mImage.getHeight() - y < mCellSize.height) ?
            mImage.getHeight() - x : mCellSize.height;
      int type = mImage.getType();
      
      BufferedImage cell = new BufferedImage(width, height, type);
      Graphics2D g2 = cell.createGraphics();
      g2.drawImage(mImage, 
            0, 0, width, height,
            x, y, x + width, y + height,
            null);
      g2.dispose();
      return cell;
   }
   
   /* *********************************************************************** *
    * Step 2 - Get matching tiles for each cell.                              *
    * *********************************************************************** */
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
      byte tileRaster[] = getData(tile);
      int difference = 0;
      if(cell.getAlphaRaster() != null) {
         difference = normalizeAlphaPixelDiff(cellRaster, tileRaster);
      } else {
         difference = normalizePixelDiff(cellRaster, tileRaster);
      }
      return difference + tile.mUseCount*mReusePenalty;
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
   
   private byte[] getData(Tile tile) {
      if(tile.mCellSizedRaster == null) {
         tile.mCellSizedRaster = getData(tile.mCellSized);
      }
      return tile.mCellSizedRaster;
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
   
   /* *********************************************************************** *
    * Step 3 - Assemble tiles into final image.                               *
    * *********************************************************************** */
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
         if(isCancelled()) return;
         
         String message = messages.get(messages.size()-1);
         mStatusReporter.setStatus(false, message);
      }
   }
   
   @Override
   protected void done() {
      mStatusReporter.setStatus(true, null);
   }
}
