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
    double low_delta;
    double high_delta;
    
    /** Creates a new instance of HodgesLehmann */
    public HodgesLehmann( Double[] group_a, Double[] group_b )
    {
        if ( group_a == null || group_b == null || group_a.length < 2 || group_b.length < 2 )
            throw new ArithmeticException( "HodgesLehman requires both groups to have data points." );
        
        int i, j, x=group_a.length, y=group_b.length;
        Vector vdiffs = new Vector();
        for ( i=0; i<x; i++ )
            for ( j=0; j<y; j++ )
                vdiffs.add( new Double( group_a[i].doubleValue() - group_b[j].doubleValue() ) );
        double[] diffs = new double[vdiffs.size()];
        for ( i=0; i<vdiffs.size(); i++ )
            diffs[i] = ((Double)vdiffs.get( i )).doubleValue();
        vdiffs.clear();
        Arrays.sort( diffs );
        hldelta = diffs[diffs.length/2];
        
        double c_alpha = Math.round(
                             (x * y / 2.0) - 
                             (
                               ZDistribution.xnormi( 0.05 / 2.0 ) * 
                               Math.sqrt( x * y * (x + y +1) / 12.0 )
                             )
                         );
        
        int high_ordinal = (int)c_alpha;
        int low_ordinal = (int)Math.round( (double)x*(double)y+1-c_alpha );
        
        low_delta = diffs[low_ordinal];
        high_delta = diffs[high_ordinal];
    }
 
    public double getDelta()
    {
        return hldelta;
    }
    
    public double getLowerDelta()
    {
        return low_delta;
    }
    
    public double getUpperDelta()
    {
        return high_delta;
    }
}


