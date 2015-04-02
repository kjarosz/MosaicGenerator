package mosaicgenerator;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import mosaicgenerator.components.ImageButton;

public class MainImagePanel extends JPanel {
   private JScrollPane mScrollPane;
   private ImageButton mImageButton;
   private JTextField mImagePath;
   
   public MainImagePanel(ActionListener triggerListener) {
      setLayout(new BorderLayout());
      createScrollPaneButton();
      createControlPanel(triggerListener);
   }
   
   private void createScrollPaneButton() {
      mImageButton = new ImageButton();
      mScrollPane = new JScrollPane(mImageButton);
      mScrollPane.setPreferredSize(new Dimension(400, 300));
      add(mScrollPane, BorderLayout.CENTER);
   }
   
   private void createControlPanel(ActionListener triggerListener) {
      JPanel controlPanel = new JPanel();
      controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.X_AXIS));
      createImageLoader(controlPanel);
      controlPanel.add(Box.createHorizontalGlue());
      createTrigger(controlPanel, triggerListener);
      add(controlPanel, BorderLayout.SOUTH);
   }
   
   private void createImageLoader(JPanel parent) {
      createPathDisplay(parent);
      createLoadButton(parent);
   }
   
   private void createPathDisplay(JPanel parent) {
      mImagePath = new JTextField(30);
      mImagePath.setEditable(false);
      parent.add(mImagePath);
   }
   
   private void createLoadButton(JPanel parent) {
      JButton loadButton = new JButton("Load Image");
      loadButton.addActionListener(createLoadTrigger());
      parent.add(loadButton);
   }
   
   private ActionListener createLoadTrigger() {
      final JComponent me = this;
      return new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
           JFileChooser imageChooser = new JFileChooser(); 
           setImageFilter(imageChooser);
           imageChooser.setMultiSelectionEnabled(false);
           int result = imageChooser.showOpenDialog(me);
           openFile(result, imageChooser.getSelectedFile());
        }
      };
   }
   
   private void setImageFilter(JFileChooser fileChooser) {
      FileNameExtensionFilter filter 
         = new FileNameExtensionFilter(
            "Images", "jpg", "jpeg", "gif", "png");
      fileChooser.setFileFilter(filter);
   }
   
   private void openFile(int result, File file) {
      if(result != JFileChooser.APPROVE_OPTION) {
         return;
      }
      
      try {
         // File comes from JFileChooser so we assume it exists.
         BufferedImage image = ImageIO.read(file);
         String path = file.getAbsolutePath();
         mImagePath.setText(path);
         mImageButton.setImage(image);
         mScrollPane.revalidate();
         repaint();
      } catch(IOException ex) {
         notifyIOException(ex);
      }
   }
   
   private void notifyIOException(IOException ex) {
      JOptionPane.showMessageDialog(this, 
            "There was an error opening the file.\n"
            + "Here is more detail:\n" + ex.getMessage(),
            "Error Opening File",
            JOptionPane.ERROR_MESSAGE);
   }
   
   private void createTrigger(JPanel parent, ActionListener triggerListener) {
      JButton triggerButton = new JButton("Create Mosaic.");
      triggerButton.addActionListener(triggerListener);
      parent.add(triggerButton);
   }
}
