/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti.data;

import java.io.*;
import java.util.*;
import org.qyouti.qti1.gui.*;
import org.w3c.dom.*;

/**
 * 
 * @author jon
 */
public class PersonData
{
  private String name;
  private String id;
  private boolean anonymous;
  private boolean excluded;
  private UserRenderPreferences preferences = null;  
  
  private ExaminationData exam;
  
  public PersonData( String name, String id, boolean anon )
  {
    this.exam = exam;
    
    this.name = name;
    this.id = id;
    this.anonymous = anon;
    this.excluded = false;
  }

  public PersonData( ExaminationData exam, Element element )
  {
    name = element.getAttribute( "name" );
    id   = element.getAttribute( "id" );
    String str = element.getAttribute( "anonymous" );
    anonymous = str != null && str.toLowerCase().startsWith( "y" );
    str = element.getAttribute( "excluded" );
    excluded = str != null && str.toLowerCase().startsWith( "y" );

    this.exam = exam;
    exam.persons.put( id, this );
    exam.persons_sorted.add( this );
    
    NodeList nl = element.getElementsByTagName( "preferences" );
    if ( nl.getLength() > 0 )
      preferences = new UserRenderPreferences( (Element)nl.item( 0 ) );
    else
      preferences = null;
  }

  public ExaminationData getExam()
  {
    return exam;
  }

  public void setExam( ExaminationData exam )
  {
    this.exam = exam;
  }

  public String getName()
  {
    return name;
  }

  public void setName( String name )
  {
    this.name = name;  
    if ( exam != null )
      exam.setUnsavedChangesInMain( true );
  }

  public String getId()
  {
    return id;
  }

  public void setId( String id )
  {
    this.id = id;
    if ( exam != null )
      exam.setUnsavedChangesInMain( true );
  }

  public boolean isAnonymous()
  {
    return anonymous;
  }

  public void setAnonymous( boolean anonymous )
  {
    this.anonymous = anonymous;
    if ( exam != null )
      exam.setUnsavedChangesInMain( true );
  }

  public boolean isExcluded()
  {
    return excluded;
  }

  public void setExcluded( boolean excluded )
  {
    this.excluded = excluded;
    if ( exam != null )
      exam.setUnsavedChangesInMain( true );
  }

  public UserRenderPreferences getPreferences()
  {
    return preferences;
  }

  public void setPreferences( UserRenderPreferences preferences )
  {
    this.preferences = preferences;
    if ( exam != null )
      exam.setUnsavedChangesInMain( true );
  }
  
  
  
  public boolean isCustomisable()
  {
    return !anonymous && !excluded;
  }
  
  public void emit( Writer writer )
          throws IOException
  {
    writer.write( "  <person name=\"" + name + "\" id=\"" + id + "\"" );
    if ( excluded )
      writer.write( " excluded=\"yes\"" );
    if ( anonymous )
      writer.write( " anonymous=\"yes\"" );
    writer.write( ">\r\n" );
    if ( preferences != null )
      preferences.emit(writer);
    writer.write( "  </person>\r\n" );
  }
  
}
