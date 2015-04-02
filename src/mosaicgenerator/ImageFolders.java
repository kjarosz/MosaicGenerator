package mosaicgenerator;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

public class ImageFolders extends JSplitPane {
   
   private JPanel mImagePanel;
   private JPanel mDirectoryList;
   
   public ImageFolders() {
      super(JSplitPane.HORIZONTAL_SPLIT);
      setLeftComponent(createFolderView());
      setRightComponent(createFolderLoader());
      
   }
   
   private JScrollPane createFolderView() {
      mImagePanel = new JPanel();
      JScrollPane scroller = new JScrollPane(mImagePanel);
      return scroller;
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
            int result = chooser.showOpenDialog(me);
            createDirectoryButton(result, chooser);
         }
      };
   }
   
   private void createDirectoryButton(int result, JFileChooser chooser) {
      if(result != JFileChooser.APPROVE_OPTION) {
         return;
      }
      
      File dir = chooser.getSelectedFile();
      if(dir.exists() && dir.isDirectory()) {
         ImageDirectory imgDir = new ImageDirectory();
         mDirectoryList.add(imgDir);
         imgDir.loadImages(dir);
      }
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
           
        }
      };
   }
}
