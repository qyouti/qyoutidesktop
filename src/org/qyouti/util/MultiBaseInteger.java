/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qyouti.util;

/**
 *
 * @author jon
 */
public class MultiBaseInteger
{
  int[] bases;
  int[] number;
  
  public MultiBaseInteger( int parts )
  {
    number = new int[parts];
    bases = new int[parts];
    for ( int i=0; i<bases.length; i++ )
      bases[i] = 10;
  }
  
  public int size()
  {
    return number.length;
  }
  
  public void reset()
  {
    for ( int i=0; i<bases.length; i++ )
      bases[i] = 10;    
  }
  
  public void setBase( int part, int base )
  {
    bases[part] = base;
    if ( number[part] >= base )
      number[part] = number[part] % base;
  }
  
  public void increment()
  {
    for ( int i=0; i<bases.length; i++ )
    {
      number[i]++;
      if ( number[i] < bases[i] )
        return;
      number[i]=0;
    }
  }
  
  public int getDigit( int part )
  {
    return number[part];
  }
  
  public void setDigit( int part, int value )
  {
    number[part] = value % bases[part];
  }
  
  public int intValue()
  {
    int value=0;
    int m=1;
    for ( int i=0; i<bases.length; i++ )
    {
      value += m*number[i];
      m = m*bases[i];
    }
    return value;
  }

  public int intValueMaximum()
  {
    int value=0;
    int m=1;
    for ( int i=0; i<bases.length; i++ )
    {
      value += m*(bases[i]-1);
      m = m*bases[i];
    }
    return value;
  }
}
