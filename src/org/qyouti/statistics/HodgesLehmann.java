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
 * HodgesLehmann.java
 *
 * Created on August 7, 2008, 2:36 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.qyouti.statistics;

import java.util.Vector;
import java.util.Arrays;

/**
 *
 * @author jon
 */
public class HodgesLehmann
{
    double hldelta = Double.NaN;
    double low_95_delta;
    double high_95_delta;
    double low_90_delta;
    double high_90_delta;
    
    /** Creates a new instance of HodgesLehmann */
    public HodgesLehmann( Double[] group_a, Double[] group_b )
    {
        if ( group_a == null || group_b == null || group_a.length < 2 || group_b.length < 2 )
            throw new ArithmeticException( "HodgesLehman requires both groups to have data points." );
        
        int i, j, x=group_a.length, y=group_b.length;
        int m = x*y;
        double diffs[] = new double[m];
        for ( i=0; i<x; i++ )
            for ( j=0; j<y; j++ )
                diffs[i*y+j]=group_a[i].doubleValue() - group_b[j].doubleValue();
        Arrays.sort( diffs );
        if ( (diffs.length & 1) == 0 )
            hldelta = (diffs[m/2] + diffs[m/2 - 1])/2.0;  // median for even data points
        else
            hldelta = diffs[m/2];

        int c_alpha;
        int high_ordinal;
        int low_ordinal;

        c_alpha = calpha( x, y, 0.05 );
        // Remember the maths assumes 1 based ranking but our arrays are 0 based.
        high_ordinal = c_alpha - 1;
        low_ordinal = x*y-1-high_ordinal;
        low_95_delta = diffs[low_ordinal];
        high_95_delta = diffs[high_ordinal];

        c_alpha = calpha( x, y, 0.1 );
        high_ordinal = c_alpha-1;
        low_ordinal = x*y-1-high_ordinal;
        low_90_delta = diffs[low_ordinal];
        high_90_delta = diffs[high_ordinal];
    }

    private int calpha( int x, int y, double p )
    {
        double calphastar =  (x * y / 2.0) -
                             (
                               ZDistribution.xnormi( p / 2.0 ) *
                               Math.sqrt( x * y * (x + y +1) / 12.0 )
                         );
       
        return (int)Math.round( calphastar );
    }

    public double getDelta()
    {
        return hldelta;
    }
    
    public double getLower95Delta()
    {
        return low_95_delta;
    }
    
    public double getUpper95Delta()
    {
        return high_95_delta;
    }

    public double getLower90Delta()
    {
        return low_90_delta;
    }

    public double getUpper90Delta()
    {
        return high_90_delta;
    }
}



