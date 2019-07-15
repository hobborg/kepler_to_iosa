//==============================================================================
//
//  PandImportanceFunctionTruthTableGenerator.java
//
//  Copyright 2018-
//  Authors:
//  - Marco Biagi <marcobiagiing@gmail.com> (Universita di Firenze)
//
//------------------------------------------------------------------------------
//
//  This file is part of the Galileo-to-IOSA converter.
//
//  The Galileo-to-IOSA converter is free software;
//  you can redistribute it and/or modify it under the terms of the GNU
//  General Public License as published by the Free Software Foundation;
//  either version 3 of the License, or (at your option) any later version.
//
//  The Galileo-to-IOSA converter is distributed in the hope that it will be
//	useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU General Public License for more details.
//
//	You should have received a copy of the GNU General Public License
//	along with this file; if not, write to the Free Software Foundation,
//	Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
//==============================================================================


package it.unifi.converter.utils;
import java.math.BigInteger;

public class PandImportanceFunctionTruthTableGenerator {

    private static int n = 1; //A \in [0,n]
    private static int m = 5; //B \in [0,m]
    
    public static void main(String[] args) {
        //Test all possible values
        int lcm = intLeastCommonMultiple(n,m);
        System.out.println("lcm is " + lcm);
        System.out.println("A\t|B\t|G\t|A'\t|B'\t|GIF\t|");
        System.out.println("-------------------------------------------------");
        for(int a=0; a<=n; a++){
            for(int b=0; b<=m; b++){
               int N_a = 
                       (int)(
                           (float)a
                           /(float)n
                           *(float)lcm
                       );
               int N_b = 
                       (int)(
                           (float)b
                           /(float)m
                           *(float)lcm
                        );
               if(a != n && b != m)
                   printSolution(a, b, N_a, N_b, 0, lcm);
               else if(a == n && b != m)
                   printSolution(a, b, N_a, N_b, 1, lcm);
               else if(a != n && b == m)
                   printSolution(a, b, N_a, N_b, 0, lcm);
               else{
                   printSolution(a, b, N_a, N_b, 0, lcm);
                   printSolution(a, b, N_a, N_b, 3, lcm);
               }
            }
        }
    }
    
    private static void printSolution(int a, int b, int N_a, int N_b, int g, int lcm){
        int gif;
        //Old version
        //gif = (lcm * g) +  N_a + Math.min(N_a, N_b) - (lcm * Math.max(0, N_b - N_a)) + ((int)Math.pow(lcm, 2));
        //New version
        if( g > 0){
            gif = Math.max(0, lcm * g + N_a + N_b);
        }else{
            gif = Math.max(0, lcm * g + N_a - N_b);
        }
        System.out.print(a + "\t|");
        System.out.print(b + "\t|");
        System.out.print(g + "\t|");
        System.out.print(N_a + "\t|");
        System.out.print(N_b + "\t|");
        System.out.print(gif + "\t|");
        System.out.print("\n");
    }
    
    private static int intGCD(int x, int y){
        BigInteger b1 = BigInteger.valueOf(x);
        BigInteger b2 = BigInteger.valueOf(y);
        BigInteger gcd = b1.gcd(b2);
        return gcd.intValue();
    }
    
    
    private static int intLeastCommonMultiple(int x, int y){
        int mcm = 1;
        int f = intGCD(x, y);
        mcm = x*y/f;
        return mcm;
    }

}
