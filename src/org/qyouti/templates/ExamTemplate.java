/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti.templates;

import java.awt.*;
import org.w3c.dom.*;

/**
 *
 * @author jon
 */
public abstract class ExamTemplate extends javax.swing.JPanel
{
  public ExamTemplate(){super();}
  public String getTemplateTitle() {return "Unknown Template Title";}
  public abstract String getMainDocumentAsString();  
  public abstract String getQuestionDocumentAsString();  
}
