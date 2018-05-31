/*
 * Copyright © 2017 M.E Xezonaki in the context of her MSc Thesis,
 * Department of Informatics and Telecommunications, UoA.
 * All rights reserved.
 */
package sqmf.impl;

/**
 * The class simulating video application type.
 *
 * @author Marievi Xezonaki
 */
public class WebBasedVideo implements Video{

    public static volatile int stallingsNum = 0;

    /**
     * The method which returns the application type's name (video).
     *
     * @return      The application type's name.
     */
    public static String getName(){
        return "Web Based Video";
    }

    public static double estimateQoE(int numberOfStallings, int durationOfStallings) {
        return 0;
    }


    public static int computeNumberOfStallings(){
        if (PacketProcessing.buffer < 0){
            stallingsNum++;
        }
        return stallingsNum;
    }

    public static int computeDurationOfStallings(){
        return 0;
    }



    /**
     * The method which computes the QoE for TCP video applications.
     *
     * @param stallings     The number of stallings.
     * @param duration      The duration of stallings.
     * @return              The computed QoE value.
     */
    public static double estimateTCPVideoQoE(int stallings, int duration){
        double a = 3.5;
        double c = 1.5;
        double b = -(0.15*duration + 0.19);
        double Vq = a*Math.exp(b*stallings) + c;
        return Vq;

    }
}
