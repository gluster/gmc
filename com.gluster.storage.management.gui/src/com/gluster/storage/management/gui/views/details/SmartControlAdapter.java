package com.gluster.storage.management.gui.views.details;

import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
 
/**
 * This is an extended version of the regular ControlAdapter.
 * It is used to get only the real resize events and excludes all resize
 * events of the text size determination.
 * 
 */
public abstract class SmartControlAdapter extends ControlAdapter {
 
  public void controlResized( ControlEvent e ) {
    Shell shell = ( ( Control )e.widget ).getShell();
    Point shellSize = shell.getSize();
    Point previousSize = ( Point )e.widget.getData( "previousShellSize"
                                                    + this.hashCode() );
    e.widget.setData( "previousShellSize" + this.hashCode(), shellSize );
    if( previousSize != null ) {
      int dx = Math.abs( Math.abs( shellSize.x - previousSize.x ) - 1000 );
      int dy = Math.abs( Math.abs( shellSize.y - previousSize.y ) - 1000 );
      if( ( dx <= 2 || dy <= 2 ) ) {
        // This came from the TextSizeDetermination
        return;
      }
    }
    handleControlResized( e );
  }
 
  protected abstract void handleControlResized( ControlEvent e );
}