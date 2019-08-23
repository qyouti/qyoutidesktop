/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti.templates;

import java.awt.*;
import javax.swing.*;
import org.qyouti.data.QuestionDefinitions;
import org.qyouti.qti1.element.*;

/**
 *
 * @author jon
 */
public interface ItemTemplate
{
  public Component getComponent();
  public boolean isPresentationeditenabled();
  public void setPresentationeditenabled( boolean presentationeditenabled );
  public boolean isProcessingeditenabled();
  public void setProcessingeditenabled( boolean processingeditenabled );
  public QTIElementItem getItem();
  public void setItem( QTIElementItem item, QuestionDefinitions overrides );
  public boolean isChanged();
  public boolean isOverrideChanged();
  /**
   * Store edited fields back into the item. Does not save to disk.
  */
  public void store( boolean override );
}
