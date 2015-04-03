package mosaicgenerator;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import mosaicgenerator.components.ImageDirectory;
import mosaicgenerator.components.ModifiedFlowLayout;

public class ImageFolders extends JSplitPane {
   private JPanel mImagePanel;
   private JPanel mDirectoryList;
   private LinkedList<ImageDirectory> mDirectoryButtons;
   private JLabel mImageCount;
   
   public ImageFolders() {
      super(JSplitPane.HORIZONTAL_SPLIT);
      mDirectoryButtons = new LinkedList<>();
      setLeftComponent(createFolderView());
      setRightComponent(createFolderLoader());
      setResizeWeight(0.75);
   }
   
   private JScrollPane createFolderView() {
      mImagePanel = new JPanel(new ModifiedFlowLayout());
      mImagePanel.addContainerListener(createContainerListener());
      JScrollPane scroller = new JScrollPane(mImagePanel);
      scroller.setHorizontalScrollBarPolicy(
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
      scroller.setVerticalScrollBarPolicy(
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      return scroller;
   }
   
   private ContainerListener createContainerListener() {
      return new ContainerListener() {
         @Override
         public void componentAdded(ContainerEvent arg0) {
            updateImageCount();
         }
         @Override
         public void componentRemoved(ContainerEvent arg0) {
            updateImageCount();
         }
      };
   }
   
   private void updateImageCount() {
      int count = mImagePanel.getComponentCount();
      mImageCount.setText("Images loaded: " + count);
   }
   
   private JPanel createFolderLoader() {
      JPanel folderLoader = new JPanel(new BorderLayout());
      createFolderList(folderLoader);
      createFolderControl(folderLoader);
      return folderLoader;
   }
   
   private void createFolderList(JPanel parent) {
      mDirectoryList = new JPanel();
      mDirectoryList.setLayout(new BoxLayout(mDirectoryList, BoxLayout.Y_AXIS));
      JScrollPane scroller = new JScrollPane(mDirectoryList);
      parent.add(scroller, BorderLayout.CENTER);
   }
   
   private void createFolderControl(JPanel parent) {
      JPanel buttonPanel = new JPanel();
      mImageCount = new JLabel("Image Count: 0");
      buttonPanel.add(mImageCount);
      createAddButton(buttonPanel);
      createRemoveButton(buttonPanel);
      parent.add(buttonPanel, BorderLayout.SOUTH);
   }
   
   private void createAddButton(JPanel parent) {
      JButton addButton = new JButton("Add");
      addButton.setToolTipText("Opens directory selector.");
      addButton.addActionListener(createAddAction());
      parent.add(addButton);
   }
   
   private ActionListener createAddAction() {
      final JComponent me = this;
      return new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setMultiSelectionEnabled(true);
            int result = chooser.showOpenDialog(me);
            createDirectoryButton(result, chooser);
         }
      };
   }
   
   private void createDirectoryButton(int result, JFileChooser chooser) {
      if(result != JFileChooser.APPROVE_OPTION) {
         return;
      }
      
      File dirs[] = chooser.getSelectedFiles();
      for(File dir: dirs) {
         if(dir.exists() && dir.isDirectory() && dirIsNotUsed(dir)) {
            ImageDirectory imgDir = new ImageDirectory(mImagePanel);
            mDirectoryList.add(imgDir);
            mDirectoryButtons.add(imgDir);
            imgDir.loadImages(dir);
         }
      }
   }
   
   private boolean dirIsNotUsed(File directory) {
      for(ImageDirectory imgDir: mDirectoryButtons) {
         File loadedDir = imgDir.getDirectory();
         if(loadedDir.equals(directory)) {
            JOptionPane.showMessageDialog(this,
                  "Directory already loaded.",
                  "Directory Exists",
                  JOptionPane.INFORMATION_MESSAGE);
            return false;
         }
      }
      return true;
   }
   
   private void createRemoveButton(JPanel parent) {
      JButton removeButton = new JButton("Remove");
      removeButton.setToolTipText("Remove selected directories.");
      removeButton.addActionListener(createRemoveAction());
      parent.add(removeButton);
   }
   
   private ActionListener createRemoveAction() {
      return new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
           Iterator<ImageDirectory> it = mDirectoryButtons.iterator();
           while(it.hasNext()) {
              ImageDirectory dir = it.next();
              if(dir.selected()) {
                 dir.removeImages();
                 mDirectoryList.remove(dir);
                 it.remove();
              }
           }
           mImagePanel.revalidate();
           mDirectoryList.revalidate();
           mDirectoryList.repaint();
        }
      };
   }
   
   public boolean hasImages() {
      return mImagePanel.getComponents().length != 0;
   }
   
   public LinkedList<BufferedImage> images() {
      LinkedList<BufferedImage> images = new LinkedList<>();
      for(ImageDirectory imgDir: mDirectoryButtons) {
         Collection<BufferedImage> dirImages = imgDir.images();
         images.addAll(dirImages);
      }
      return images;
   }
}
