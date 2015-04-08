package mosaicgenerator.utils;

import java.beans.PropertyChangeListener;

import javax.swing.JProgressBar;

public interface MosaicMakerCallback {
   public void makeMosaic(JProgressBar listener, PropertyChangeListener stateListener);
   public void stopMosaic();
}
