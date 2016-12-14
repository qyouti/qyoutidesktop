/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti.data;

/**
 * Intended to be used by a form or other GUI class that needs to
 * know when individual fields have changed so they can be updated
 * in the GUI.  This doesn't include data that is represented by
 * TableModels because they communicate with JTable controls using
 * standard table events.
 * This interface won't be exhaustive - only changes that the 
 * QyoutiFrame wants to know about or which it used to want to know about
 * are supported.
 * 
 * @author jon
 */
public interface ExaminationDataListener
{
  
}
