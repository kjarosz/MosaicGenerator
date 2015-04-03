package mosaicgenerator;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import mosaicgenerator.components.ImageButton;

public class ResultsPage extends JPanel {
   private ImageButton mResultButton;
   private JScrollPane mScrollPane;
   
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
      JButton button = new JButton("Save Image");
      button.addActionListener(createSaveAction());
      JPanel buttonPanel = new JPanel();
      buttonPanel.add(button);
      add(buttonPanel, BorderLayout.SOUTH);
   }
   
   private ActionListener createSaveAction() {
      JPanel me = this;
      return new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            if(mResultButton != null) {
               JFileChooser chooser = new JFileChooser();
               int result = chooser.showSaveDialog(me);
               saveImage(result, chooser);
            }
         }
      };
   }
   
   private void saveImage(int result, JFileChooser chooser) {
      if(result != JFileChooser.APPROVE_OPTION) {
         return;
      }
      
      File selectedFile = chooser.getSelectedFile();
      try {
         ImageIO.write(mResultButton.getImage(), "png", selectedFile);
      } catch(IOException ex) {
         JOptionPane.showMessageDialog(
               this,
               "Could not save file.\n\n" + ex.getMessage(),
               "Error saving file.",
               JOptionPane.ERROR_MESSAGE);
      }
   }
}
