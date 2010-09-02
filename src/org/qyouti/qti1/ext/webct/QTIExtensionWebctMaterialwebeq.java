/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.qyouti.qti1.ext.webct;

import java.io.*;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import net.sourceforge.jeuclid.context.LayoutContextImpl;
import net.sourceforge.jeuclid.converter.Converter;
import net.sourceforge.jeuclid.converter.ConverterPlugin;
import org.qyouti.qti1.QTIMatmedia;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author jon
 */
public class QTIExtensionWebctMaterialwebeq
        extends QTIMatmedia
{
    Vector<Fragment> fragments = new Vector<Fragment>();

    @Override
    public boolean isSupported()
    {
        return true;
    }

    @Override
    public void initialize()
    {
        super.initialize();
        String content = domelement.getTextContent();
        boolean in_eq = false;
        int begin = 0;
        int i;

        for ( i=0; i<content.length(); i++ )
        {
            if ( in_eq )
            {
                if ( content.charAt(i) == '}' )
                {
                    fragments.add( new MatMLEq( content.substring(begin, i+1) ) );
                    begin = i+1;
                    in_eq = false;
                }
            }
            else
            {
                if ( content.substring(i).startsWith( "{Equation:eqn=" ) )
                {
                    fragments.add( new Fragment( content.substring(begin, i) ) );
                    begin = i;
                    in_eq = true;
                }
            }
        }
        if ( begin < content.length() )
            fragments.add( new Fragment( content.substring(begin) ) );

//        System.out.println( "Fragments: " + fragments.size() );
//        for ( i=0; i<fragments.size(); i++ )
//        {
//            if ( fragments.get(i) instanceof String )
//                System.out.println( "Text " + fragments.get(i) );
//            if ( fragments.get(i) instanceof MatMLEq )
//                System.out.println( "Equation " + fragments.get(i) );
//        }
    }


    public Fragment[] getContentFragments()
    {
        return fragments.toArray(new Fragment[0]);
    }

    public class Fragment
    {
        public String content;
        public Fragment( String content )
        {
            this.content = content;
        }
    }


    public class MatMLEq extends Fragment
            //implements EntityResolver
    {
        Hashtable<String,String> attributes = new Hashtable<String,String>();;

        static final String doctype =
                "<?xml version=\"1.0\"?>\n" +
                "<!DOCTYPE math SYSTEM "+
                "\"http://www.w3.org/Math/DTD/mathml2/mathml2.dtd\" " +
                "[<!ENTITY infty \"&#8734;\">] >\n";



        public MatMLEq( String content )
        {
            super( content );
            this.content = content.substring( 10, content.length()-1 );
            StringTokenizer tok = new StringTokenizer( this.content, "," );
            String doublet;
            int off;
            while ( tok.hasMoreTokens() )
            {
                doublet = tok.nextToken();
                off = doublet.indexOf('=');
                if ( off>0 )
                    attributes.put(doublet.substring(0, off), doublet.substring(off+1) );
            }

            System.out.println( "Hashtable: " + attributes.toString() );
            String eqn = attributes.get("eqn");
            if ( eqn == null || eqn.length() == 0 )
                return;
            eqn = doctype + eqn;
        }


        public Document getMathML()
        {
            String eqn = attributes.get("eqn");
            if ( eqn == null || eqn.length() == 0 )
                return null;
            eqn = doctype + eqn;
            try
            {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                return builder.parse(new InputSource(new StringReader(eqn)));
            } catch (Exception e)
            {
                e.printStackTrace();
            }
            return null;
        }

        public int getWidth()
        {
            return Integer.parseInt(attributes.get("width"));
        }

        public int getHeight()
        {
            return Integer.parseInt(attributes.get("height"));
        }

        public String toString()
        {
            return content;
        }

//        @Override
//        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException
//        {
//            System.out.println( "resolveEntity(String " + publicId + ", String " + systemId + ")" );
//            return null;
//        }
    }
}

