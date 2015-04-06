package mosaicgenerator;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;

import mosaicgenerator.components.ImageButton;
import mosaicgenerator.utils.ImageSaver;

public class ResultsPage extends JPanel {
   private ImageButton mResultButton;
   private JScrollPane mScrollPane;
   
   private JProgressBar mProgressBar;
   private ImageSaver mImageSaver;
   
   public ResultsPage() {
      createWidgets();
   }
   
   public void publishResult(BufferedImage result) {
      mResultButton.setImage(result);
      mScrollPane.revalidate();
      repaint();
   }
   
   private void createWidgets() {
      setLayout(new BorderLayout());
      createResultView();
      createSavePanel();
   }
   
   private void createResultView() {
      mResultButton = new ImageButton();
      mScrollPane = new JScrollPane(mResultButton);
      add(mScrollPane, BorderLayout.CENTER);
   }
   
   private void createSavePanel() {
      JPanel buttonPanel = new JPanel();
      buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
      addProgressBar(buttonPanel);
      addSaveButton(buttonPanel);
      add(buttonPanel, BorderLayout.SOUTH);
   }

   private void addProgressBar(JPanel savePanel) {
      mProgressBar = new JProgressBar();
      mProgressBar.setMinimum(0);
      mProgressBar.setMaximum(100);
      savePanel.add(mProgressBar);
   }
   
   private void addSaveButton(JPanel savePanel) {
      JButton button = new JButton("Save Image");
      button.addActionListener(createSaveAction(button));
      savePanel.add(button);
   }
   
   private ActionListener createSaveAction(final JButton saveButton) {
      JPanel me = this;
      return new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            if(isSavingFile()) {
               mImageSaver.cancel(true);
               saveButton.setText("Save Image");
            } else {
               if(mResultButton != null) {
                  JFileChooser chooser = new JFileChooser();
                  int result = chooser.showSaveDialog(me);
                  saveImage(result, chooser, saveButton);
                  saveButton.setText("Stop Saving");
               }
            }
         }
      };
   }
   
   private boolean isSavingFile() {
      if(mImageSaver == null)
         return false;
      
      if(mImageSaver.isCancelled() || mImageSaver.isDone()) {
         mImageSaver = null;
         return false;
      }
      
      return true;
   }
   
   private void saveImage(int result, JFileChooser chooser, JButton saveButton) {
      if(result != JFileChooser.APPROVE_OPTION) {
         return;
      }
      
      File selectedFile = chooser.getSelectedFile();
      mImageSaver = new ImageSaver(mProgressBar, 
                                   mResultButton.getImage(), 
                                   selectedFile);
      mImageSaver.addPropertyChangeListener(
            getPropertyChangeListener(saveButton));
      mImageSaver.execute();
   }
   
   private PropertyChangeListener getPropertyChangeListener(final JButton saveButton) {
      return new PropertyChangeListener() {
         @Override
         public void propertyChange(PropertyChangeEvent e) {
            String property = e.getPropertyName();
            if("state".equals(property))
            {
               ImageSaver saver = (ImageSaver)e.getSource();
               if(saver.isCancelled() || saver.isDone())
               {
                  mImageSaver = null;
                  saveButton.setText("Save Image");
               }
            }
         }
      };
   }
}
