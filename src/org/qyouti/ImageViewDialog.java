/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.*;
import java.util.logging.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.bullseye.BullseyeGenerator;
import org.bullseye.BullseyePage;
import org.bullseye.BullseyePageScanner;
import org.qyouti.qti1.gui.PaginationRecord;
import org.qyouti.qti1.gui.QuestionMetricBox;
import org.qyouti.qti1.gui.QuestionMetricsRecord;
import org.qyouti.scan.process.BarcodeResult;
import org.qyouti.scan.process.BarcodeScanner;

/**
 *
 * @author jon
 */
public class ImageViewDialog
        extends javax.swing.JDialog
{
  QyoutiFrame frame;
  
  /**
   * Creates new form ImageViewDialog
   */
  public ImageViewDialog( QyoutiFrame parent, boolean modal )
  {
    super( parent, modal );
    frame=parent;
    initComponents();
  }

  public void setImage( File file )
  {
    int i, j, k;
    PaginationRecord paginationrecord=null;
    PaginationRecord.Page page=null;
    PaginationRecord.Bullseye b=null;
    BullseyePage bpage=null;
    
    try
    {
      BufferedImage image = ImageIO.read( file );
      
      BarcodeResult barcode = BarcodeScanner.scan(image);
      if ( barcode == null )
        errormessagelabel.setText("Error attempting to read barcode");
      else if ( barcode.getPrintID() == null)
        errormessagelabel.setText("No print ID in barcode");
      else if ( !barcode.getPrintID().equals(frame.exam.getLastPrintID()) )
        errormessagelabel.setText("Print ID in barcode does not match loaded exam.");
      else
      {
        paginationrecord = frame.exam.examcatalogue.getPrintMetric( barcode.getPrintID() );
        page = paginationrecord.getPage( barcode.getPageID() );
        
        Point2D.Float[] pointd = new Point2D.Float[3];
        for ( i=0; i<3; i++ )
        {
          b = page.getBullseye( i );
          pointd[i] = new Point2D.Float( b.getX(), b.getY() );
        }
        BullseyePageScanner bpscanner = new BullseyePageScanner( 
                page.getWidth(), 
                page.getHeight(), 
                pointd, 
                b.getR(), 
                BullseyeGenerator.RADII );
        bpage = bpscanner.scan(image);
        for ( i=0; i<3; i++ )
        {
          JLabel label = new JLabel();
          label.setIcon( new ImageIcon( bpage.searchimages[i] ) );
          centrepanel.add(label);
          label = new JLabel();
          label.setIcon( new ImageIcon( bpage.votemapimages[i] ) );
          centrepanel.add(label);
        }
      }
      
      Graphics2D g = image.createGraphics();
      if ( barcode != null)
      {
        printidlabel.setText( barcode.getPrintID() );
        pageidlabel.setText( barcode.getPageID() );
        
        g.setColor( Color.GREEN );
        g.drawOval(barcode.getStartPoint().x-4, barcode.getStartPoint().y-4, 8, 8);
        g.drawOval(barcode.getEndPoint().x-4,   barcode.getEndPoint().y-4,   8, 8);
        //for ( i=0; i<barcode.getBarcodeSearchRectCount(); i++ )
        //  g.draw(barcode.getBarcodeSearchRect(i));
      }
      if ( bpage != null)
      {
        g.setColor( Color.BLUE );
        for ( i=0; i<bpage.searchareas.length; i++ )
          g.draw(bpage.searchareas[i]);
        g.setColor( Color.RED );
        for ( i=0; i<bpage.bullseyepointsscan.length; i++ )
        {
          if ( bpage.bullseyepointsscan[i] != null )
          {
            g.drawLine(
                    bpage.bullseyepointsscan[i].x, 
                    bpage.bullseyepointsscan[i].y-16, 
                    bpage.bullseyepointsscan[i].x , 
                    bpage.bullseyepointsscan[i].y+16 );
            g.drawLine(
                    bpage.bullseyepointsscan[i].x-16, 
                    bpage.bullseyepointsscan[i].y, 
                    bpage.bullseyepointsscan[i].x+16, 
                    bpage.bullseyepointsscan[i].y );
          }
        }
        
        PaginationRecord.Item[] items = page.getItems();
        Point[] test=new Point[4];
        Point[] p=new Point[4];
        Rectangle bounds = new Rectangle();
        for ( i=0; i < p.length; i++ )
        {
          test[i]=new Point();
          p[i]=new Point();
        }
        
        for ( i=0; i < items.length; i++ )
        {
          test[0].x = items[i].getX();
          test[0].y = items[i].getY();
          test[1].x = test[0].x + items[i].getWidth();
          test[1].y = test[0].y;
          test[2].x = test[0].x + items[i].getWidth();
          test[2].y = test[0].y + items[i].getHeight();
          test[3].x = test[0].x;
          test[3].y = test[0].y + items[i].getHeight();
          
          bounds.setLocation(0, 0);
          bounds.setSize(0, 0);
          for ( k=0; k<test.length; k++ )
          {
            bpage.toImageCoordinates( test[k], p[k] );
            if ( k==0 )
              bounds.setLocation(p[k]);
            else
              bounds.add(p[k]);
          }
          
          g.setColor( Color.RED );
//          for ( k=0; k<test.length; k++ )
//            g.drawLine( p[k].x, p[k].y, p[(k+1)%test.length].x, p[(k+1)%test.length].y    );
//
//          g.setColor( Color.YELLOW );
          g.draw(bounds);
          
          QuestionMetricsRecord qmr = items[i].getQuestionMetricsRecord();
          QuestionMetricBox box;
          for ( j=0; j<qmr.boxes.size(); j++ )
          {
            box = qmr.boxes.get(j);
            test[0].x = items[i].getX() + box.x;
            test[0].y = items[i].getY() + box.y;
            test[1].x = test[0].x + box.width;
            test[1].y = test[0].y;
            test[2].x = test[0].x + box.width;
            test[2].y = test[0].y + box.height;
            test[3].x = test[0].x;
            test[3].y = test[0].y + box.height;
            
            bounds.setLocation(0, 0);
            bounds.setSize(0, 0);
            for ( k=0; k<test.length; k++ )
            {
              bpage.toImageCoordinates( test[k], p[k] );
              if ( k==0 )
                bounds.setLocation(p[k]);
              else
                bounds.add(p[k]);
            }
            g.setColor( Color.GREEN );
            g.draw(bounds);
//            for ( k=0; k<test.length; k++ )
//              g.drawLine( p[k].x, p[k].y, p[(k+1)%test.length].x, p[(k+1)%test.length].y    );
          }
        }
      }
      g.dispose();
      
      
      imagelabel.setIcon( new ImageIcon( image ) );
      pack();
    }
    catch ( Exception ex )
    {
      Logger.getLogger( ImageViewDialog.class.getName() ).log( Level.SEVERE, null, ex );
    }
  }
  
  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents()
  {

    scrollpane = new javax.swing.JScrollPane();
    centrepanel = new javax.swing.JPanel();
    imagelabel = new javax.swing.JLabel();
    bottompanel = new javax.swing.JPanel();
    closebutton = new javax.swing.JButton();
    toppanel = new javax.swing.JPanel();
    jLabel1 = new javax.swing.JLabel();
    printidlabel = new javax.swing.JLabel();
    jLabel2 = new javax.swing.JLabel();
    pageidlabel = new javax.swing.JLabel();
    errormessagelabel = new javax.swing.JLabel();

    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    setTitle("View Scan Image");

    centrepanel.add(imagelabel);

    scrollpane.setViewportView(centrepanel);

    getContentPane().add(scrollpane, java.awt.BorderLayout.CENTER);

    closebutton.setText("Close");
    closebutton.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        closebuttonActionPerformed(evt);
      }
    });
    bottompanel.add(closebutton);

    getContentPane().add(bottompanel, java.awt.BorderLayout.SOUTH);

    toppanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 20, 5));

    jLabel1.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
    jLabel1.setText("Print ID:");
    toppanel.add(jLabel1);

    printidlabel.setText(" ");
    toppanel.add(printidlabel);

    jLabel2.setFont(new java.awt.Font("Cantarell", 1, 18)); // NOI18N
    jLabel2.setText("Page ID:");
    toppanel.add(jLabel2);

    pageidlabel.setText(" ");
    toppanel.add(pageidlabel);

    errormessagelabel.setText(" ");
    errormessagelabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
    toppanel.add(errormessagelabel);

    getContentPane().add(toppanel, java.awt.BorderLayout.NORTH);

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void closebuttonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_closebuttonActionPerformed
  {//GEN-HEADEREND:event_closebuttonActionPerformed
    setVisible( false );
    dispose();
  }//GEN-LAST:event_closebuttonActionPerformed


  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JPanel bottompanel;
  private javax.swing.JPanel centrepanel;
  private javax.swing.JButton closebutton;
  private javax.swing.JLabel errormessagelabel;
  private javax.swing.JLabel imagelabel;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JLabel pageidlabel;
  private javax.swing.JLabel printidlabel;
  private javax.swing.JScrollPane scrollpane;
  private javax.swing.JPanel toppanel;
  // End of variables declaration//GEN-END:variables
}
