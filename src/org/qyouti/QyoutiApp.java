/*
 *
 * Copyright 2010 Leeds Metropolitan University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may 
 * not use this file except in compliance with the License. You may obtain 
 * a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 *
 *
 */



/*
 * QyoutiApp.java
 */

package org.qyouti;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.qyouti.scan.*;
import org.jdesktop.application.Action;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

/**
 * The main class of the application.
 */
public class QyoutiApp extends SingleFrameApplication {

  static String[] app_args;

    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup() {
      String basefoldername=null;
      String exam=null;
      String repliesfilename=null;
      String reportfilename=null;
      String scanfolder=null;
      boolean pdf=false;
      boolean exit=false;
      int classsize=-1;

      for ( int i=0; i<app_args.length; i++ )
      {
        System.out.println( app_args[i] );
        if ( app_args[i].startsWith( "-base=") )
          basefoldername = app_args[i].substring( "-base=".length() );
        if ( app_args[i].startsWith( "-scans=") )
          scanfolder = app_args[i].substring( "-scans=".length() );
        if ( app_args[i].startsWith( "-exam=") )
          exam = app_args[i].substring( "-exam=".length() );
        if ( app_args[i].startsWith( "-replies=") )
          repliesfilename = app_args[i].substring( "-replies=".length() );
        if ( app_args[i].startsWith( "-report=") )
          reportfilename = app_args[i].substring( "-report=".length() );
        if ( app_args[i].equals( "-pdf") )
          pdf = true;
        if ( app_args[i].equals( "-exit") )
          exit = true;
        if ( app_args[i].equals( "-classsize") )
        {
          try {classsize = Integer.parseInt( app_args[i].substring( "-classsize=".length() ) );}
          catch (NumberFormatException nfe ) {classsize=-1;}
        }
      }

      QyoutiView view = new QyoutiView(
          this,
          basefoldername,
          exam,
          repliesfilename,
          reportfilename,
          scanfolder,
          classsize,
          pdf,
          exit
          );
      show( view );
      view.getFrame().addWindowListener( new MainFrameListener() );
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of QyoutiApp
     */
    public static QyoutiApp getApplication() {
        return Application.getInstance(QyoutiApp.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
      app_args = args;
      launch(QyoutiApp.class, args);
    }

     private class MainFrameListener extends WindowAdapter {
         public void windowClosing(WindowEvent e) {
            exit();
         }
     }

}
