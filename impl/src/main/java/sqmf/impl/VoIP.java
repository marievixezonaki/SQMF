/*
 * Copyright © 2017 M.E Xezonaki in the context of her MSc Thesis,
 * Department of Informatics and Telecommunications, UoA.
 * All rights reserved.
 */
package sqmf.impl;

/**
 * The class simulating VoIP application type.
 *
 * @author Marievi Xezonaki
 */
public class VoIP {



    /**
     * The method which returns the application type's name (VoIP).
     *
     * @return      The application type's name.
     */
    public static String getName(){
        return "VoIP";
    }




    /**
     * The method which computes the QoE for VoIP applications.
     *
     * @param delay         The computed delay (msec).
     * @param packetLoss    The computed packet loss (%).
     * @return              The computed QoE value.
     */
    public static double estimateQoE(float delay, double packetLoss) {
        int h;
        if (delay - 177.3 > 0){
            h = 1;
        }
        else {
            h = 0;
        }
        double R = 94.2 - 0.024*delay - 0.11*h*(delay-177.3) - 11 - 40*Math.log(1+10*packetLoss);
        double MOS;
        if (R < 0){
            MOS = 1;
        }
        else{
            MOS = 1 + 0.035*R + R*(R-60)*(100-R)/1000000;
        }

        return MOS;
    }


}
