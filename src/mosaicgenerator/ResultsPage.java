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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import mosaicgenerator.components.ImageButton;
import mosaicgenerator.utils.ImageSaver;
import mosaicgenerator.utils.MosaicMaker;
import mosaicgenerator.utils.MosaicMakerCallback;

public class ResultsPage extends JPanel {
   private static enum State {
      SAVING,
      MOSAIC,
      STANDBY
   }
   
   private ImageButton mResultButton;
   private JScrollPane mScrollPane;
   
   private JProgressBar mProgressBar;
   private JButton mSaveButton;
   private JButton mMakeMosaicButton;
   
   private State mButtonState;
   
   private ImageSaver mImageSaver;
   
   private MosaicMakerCallback mMakerCallback;
   
   public ResultsPage(MosaicMakerCallback makerCallback) {
      mMakerCallback = makerCallback;
      mButtonState = State.STANDBY;
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
      createSouthPanel();
   }
   
   private void createResultView() {
      mResultButton = new ImageButton();
      mScrollPane = new JScrollPane(mResultButton);
      add(mScrollPane, BorderLayout.CENTER);
   }
   
   private void createSouthPanel() {
      JPanel buttonPanel = new JPanel();
      buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
      addProgressBar(buttonPanel);
      addSaveButton(buttonPanel);
      addGenerateButton(buttonPanel);
      add(buttonPanel, BorderLayout.SOUTH);
   }

   private void addProgressBar(JPanel savePanel) {
      mProgressBar = new JProgressBar();
      mProgressBar.setMinimum(0);
      mProgressBar.setMaximum(100);
      savePanel.add(mProgressBar);
   }
   
   private void addSaveButton(JPanel savePanel) {
      mSaveButton = new JButton("Save Image");
      mSaveButton.addActionListener(createSaveAction());
      savePanel.add(mSaveButton);
   }
   
   private void addGenerateButton(JPanel savePanel) {
      mMakeMosaicButton = new JButton("Make Mosaic");
      mMakeMosaicButton.addActionListener(createGenerateListener());
      savePanel.add(mMakeMosaicButton);
   }
   
   /* ********************************************************************** */
   /*                            CONTROL                                     *
   /* ********************************************************************** */
   
   private ActionListener createSaveAction() {
      JPanel me = this;
      return new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            if(isSavingFile()) {
               mImageSaver.cancel(true);
               setButtonState(State.STANDBY);
            } else {
               if(mResultButton != null) {
                  JFileChooser chooser = new JFileChooser();
                  FileNameExtensionFilter nameFilter = 
                        new FileNameExtensionFilter("Portable Network Graphic", "png");
                  chooser.setFileFilter(nameFilter);
                  int result = chooser.showSaveDialog(me);
                  saveImage(result, chooser);
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
   
   private void saveImage(int result, JFileChooser chooser) {
      if(result != JFileChooser.APPROVE_OPTION) {
         return;
      }
      
      File selectedFile = chooser.getSelectedFile();
      if(!selectedFile.getName().endsWith(".png")) {
         String path = selectedFile.getParent();
         String name = selectedFile.getName();
         selectedFile = new File(path, name + ".png");
      }
      
      mImageSaver = new ImageSaver(mProgressBar, 
                                   mResultButton.getImage(), 
                                   selectedFile);
      mImageSaver.addPropertyChangeListener(
                     getSaveStateListener(mSaveButton));
      mImageSaver.execute();
      setButtonState(State.SAVING);
   }
   
   private PropertyChangeListener getSaveStateListener(final JButton saveButton) {
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
                  setButtonState(State.STANDBY);
               }
            }
         }
      };
   }
   
   private ActionListener createGenerateListener() {
      return new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            if(mButtonState.equals(State.MOSAIC)) {
               mMakerCallback.stopMosaic();
               setButtonState(State.STANDBY);
            } else {
               mMakerCallback.makeMosaic(mProgressBar, createGeneratorListener());
               setButtonState(State.MOSAIC);
            }
         }
      };
   }
   
   private PropertyChangeListener createGeneratorListener() {
      return new PropertyChangeListener() {
         @Override
         public void propertyChange(PropertyChangeEvent e) {
            String property = e.getPropertyName();
            if(!"state".equals(property)) {
               return;
            }
            
            String state = e.getNewValue().toString();
            if(!"DONE".equals(state)) {
               return;
            }
            
            MosaicMaker src = (MosaicMaker)e.getSource();
            if(src.isCancelled()) {
               return;
            } else {
               try {
                  BufferedImage result = src.get();
                  publishResult(result);
               } catch(Exception error) {
                  JOptionPane.showMessageDialog(
                        null,
                        "Something went wrong retrieving results.\n"
                        + error.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
               } finally {
                  src = null;
               }
            }
            
            setButtonState(State.STANDBY);
         }
      };
   }
   
   private void setButtonState(State state) {
      switch(state) {
      case SAVING:
         mSaveButton.setText("Stop Saving");
         mMakeMosaicButton.setEnabled(false);
         break;
      case MOSAIC:
         mMakeMosaicButton.setText("Stop");
         mSaveButton.setEnabled(false);
         break;
      case STANDBY:
         mSaveButton.setEnabled(true);
         mSaveButton.setText("Save Image");
         
         mMakeMosaicButton.setEnabled(true);
         mMakeMosaicButton.setText("Make Mosaic");
         break;
      }
      
      mSaveButton.repaint();
      mMakeMosaicButton.repaint();
      
      mButtonState = state;
   }
}
