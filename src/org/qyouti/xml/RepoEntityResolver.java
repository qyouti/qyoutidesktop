/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti.xml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author jon
 */
public class RepoEntityResolver implements EntityResolver
{

  String mathml = "http://www.w3.org/Math/DTD/mathml2/";

  private static final Hashtable<String,RepoEntity> resourcesbypublic=new Hashtable<String,RepoEntity>();
  private static final Hashtable<String,RepoEntity> resourcesbysystem=new Hashtable<String,RepoEntity>();
  private static final Hashtable<String,RepoEntity> resourcesbyfile=new Hashtable<String,RepoEntity>();

  public RepoEntityResolver()
  {
    synchronized ( resourcesbypublic )
    {
      if ( resourcesbypublic.isEmpty() )
      {
        URL tableurl = getClass().getClassLoader().getResource( "org/qyouti/xml/entitytable.xml" );
        if ( tableurl== null )
          return;
        System.out.println( "Loading " + tableurl.toExternalForm() );

        // Don't use the qyouti builder factory - it would instantiate another
        // one of these resolvers and then we'd be in a pickle.
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try
        {
          DocumentBuilder builder = factory.newDocumentBuilder();
          Document doc = builder.parse( tableurl.toExternalForm() );
          NodeList nl = doc.getElementsByTagName( "entity" );
          Element entity;
          RepoEntity repoentity;
          URL url;
          for ( int i=0; i<nl.getLength(); i++ )
          {
            entity = (Element)nl.item( i );
            repoentity = new RepoEntity();
            repoentity.publicid = entity.getAttribute("publicid");
            repoentity.systemid = entity.getAttribute("systemid");
            repoentity.file = entity.getAttribute("file");
            repoentity.local = entity.getAttribute("local");
            url = getClass().getClassLoader().getResource( repoentity.local );
            if ( url==null )
              continue;
            if ( repoentity.publicid!=null && repoentity.publicid.length() > 0 )
              resourcesbypublic.put(repoentity.publicid, repoentity);
            if ( repoentity.systemid!=null && repoentity.systemid.length() > 0 )
              resourcesbysystem.put(repoentity.systemid, repoentity);
            if ( repoentity.file!=null && repoentity.file.length() > 0 )
              resourcesbyfile.put(repoentity.file, repoentity);
          }
        }
        catch (Exception ex)
        {
          Logger.getLogger(RepoEntityResolver.class.getName()).log(Level.SEVERE, null, ex);
        }

      }
    }
  }

  @Override
  public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException
  {
    synchronized ( resourcesbypublic )
    {
      String name;
      RepoEntity repoentity=null;
      InputStream in;
      if ( publicId != null && publicId.length()>0 )
        repoentity = resourcesbypublic.get( publicId );
      if ( repoentity == null && systemId != null && systemId.length()>0 )
      {
        repoentity = resourcesbysystem.get( systemId );
        if ( repoentity == null )
        {
          name = systemId.substring(systemId.lastIndexOf('/') + 1);
          repoentity = resourcesbyfile.get( name );
        }
      }

      if ( repoentity != null)
      {
        //System.out.println( "Found entity in local store: " + repoentity.local );
        in = getClass().getClassLoader().getResourceAsStream( repoentity.local );
        if ( in!=null )
          return new InputSource( in );
      }
    }
      
    System.out.println( "Didn't find entity in local store: public: " + publicId + "  system: " + systemId );

//    System.out.print("<entity ");
//    System.out.print("publicid=\"" + publicId + "\" ");
//    System.out.print("systemid=\"" + systemId + "\" ");
//    System.out.print("file=\"" + systemId.substring(systemId.lastIndexOf('/') + 1) + "\" ");
//    System.out.print("local=\"org/qyouti/xml/entities/mathml2/" + systemId.substring(mathml.length()) + "\" ");
//    System.out.println(">");

    // returning null means resolve the default way
    return null;
  }


  class RepoEntity
  {
    String publicid;
    String systemid;
    String file;
    String local;
  }
}
