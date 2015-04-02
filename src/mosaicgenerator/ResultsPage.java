package mosaicgenerator;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
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
      return new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            
         }
      };
   }
}
