package mosaicgenerator;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

import mosaicgenerator.utils.Settings;
import net.miginfocom.swing.MigLayout;

public class SettingsPage extends JPanel {
   private final String DEFAULT_CELL_WIDTH = "15";
   private final String DEFAULT_CELL_HEIGHT = "15";
   
   private final String DEFAULT_TILE_WIDTH = "150";
   private final String DEFAULT_TILE_HEIGHT = "150";
   
   private final String DEFAULT_REUSE_PENALTY = "15";
   
   private JTextField mCellWidth;
   private JTextField mCellHeight;
   
   private JTextField mTileWidth;
   private JTextField mTileHeight;
   
   private JCheckBox mReuseTiles;
   private JTextField mReusePenalty;
   
   public SettingsPage(ActionListener saveListener) {
      createWidgets(saveListener);
   }
   
   private void createWidgets(ActionListener saveListener) {
      setLayout(new BorderLayout());
      createCenterPanel();
      createApplyPanel(saveListener);
   }
   
   private void createCenterPanel() {
      JPanel centerPane = new JPanel(new MigLayout());
      createCellSizePanel(centerPane);
      createTileSizePanel(centerPane);
      createTileReusePanel(centerPane);
      add(centerPane, BorderLayout.CENTER);
   }
   
   private void createCellSizePanel(JPanel parent) {
      JPanel panel = makeSectionPanel("Cell Dimensions");
      mCellWidth = new JTextField(DEFAULT_CELL_WIDTH, 4);
      mCellHeight = new JTextField(DEFAULT_CELL_HEIGHT, 4);
      addToPanel("Cell Width", mCellWidth, panel);
      addToPanel("Cell Height", mCellHeight, panel);
      parent.add(panel);
   }
   
   private JPanel makeSectionPanel(String title) {
      JPanel panel = new JPanel();
      panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      panel.setBorder(makeBorder(title));
      return panel;
   }
   
   private Border makeBorder(String title) {
      Border titledBorder = BorderFactory
            .createTitledBorder(title);
      Border loweredBorder = BorderFactory
            .createBevelBorder(BevelBorder.LOWERED);
      Border raisedBorder = BorderFactory
            .createBevelBorder(BevelBorder.RAISED);
      Border bevelBorder = BorderFactory
            .createCompoundBorder(raisedBorder, loweredBorder);
      return BorderFactory
            .createCompoundBorder(titledBorder, bevelBorder);
   }
   
   private void addToPanel(String label, JComponent comp, JPanel panel) {
      JPanel container = new JPanel();
      container.add(new JLabel(label));
      container.add(comp);
      panel.add(container);
   }
   
   private void createTileSizePanel(JPanel parent) {
      JPanel panel = makeSectionPanel("Tile Dimensions");
      mTileWidth = new JTextField(DEFAULT_TILE_WIDTH, 4);
      mTileHeight = new JTextField(DEFAULT_TILE_HEIGHT, 4);
      addToPanel("Tile Width", mTileWidth, panel);
      addToPanel("Tile Height", mTileHeight, panel);
      parent.add(panel);
   }
   
   private void createTileReusePanel(JPanel parent) {
      JPanel panel = makeSectionPanel("Reuse Tiles");
      mReuseTiles = new JCheckBox("Reuse Tiles", true);
      mReusePenalty = new JTextField(DEFAULT_REUSE_PENALTY, 4);
      panel.add(mReuseTiles);
      panel.add(new JLabel("Reuse Penalty"));
      panel.add(mReusePenalty);
      parent.add(panel);
   }
   
   private void createApplyPanel(ActionListener saveListener) {
      JPanel panel = new JPanel();
      
      JButton saveButton = new JButton("Save");
      saveButton.addActionListener(saveListener);
      panel.add(saveButton);
      
      JButton resetButton = new JButton("Reset");
      resetButton.addActionListener(createResetListener());
      panel.add(resetButton);
      
      add(panel, BorderLayout.SOUTH);
   }
   
   private ActionListener createResetListener() {
      return new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
           mCellWidth.setText(DEFAULT_CELL_WIDTH);
           mCellHeight.setText(DEFAULT_CELL_HEIGHT);
           mTileWidth.setText(DEFAULT_TILE_WIDTH);
           mTileWidth.setText(DEFAULT_TILE_HEIGHT);
           mReuseTiles.setSelected(true);
           mReusePenalty.setText(DEFAULT_REUSE_PENALTY);
        }
      };
   }
   
   public Settings getSettings() throws RuntimeException {
      Settings settings = new Settings();
      settings.cellWidth = getInt(mCellWidth);
      settings.cellHeight = getInt(mCellHeight);
      settings.tileWidth = getInt(mTileWidth);
      settings.tileHeight = getInt(mTileHeight);
      settings.reuseTiles = true;
      settings.reusePenalty = getInt(mReusePenalty);
      return settings;
   }
   
   private int getInt(JTextField field) {
      try {
         String input = field.getText();
         int value = Integer.parseInt(input);
         if(value <= 0) throw new NumberFormatException();
         return value;
      } catch(NumberFormatException ex) {
         JOptionPane.showMessageDialog(
               this,
               "Make sure all the dimensions are greater than 0.",
               "Number Format Error.",
               JOptionPane.ERROR_MESSAGE);
         throw new RuntimeException("Dimensions invalid.");
      }
   }
}
