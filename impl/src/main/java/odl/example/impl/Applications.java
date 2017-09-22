/*
 * Copyright © 2017 M.E Xezonaki in the context of her MSc Thesis, Department of Informatics and Telecommunications, UoA.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package odl.example.impl;

public enum  Applications implements QoEEstimation{
    VoIP{

        @Override
        public String getName(){
            return "VoIP";
        }

        @Override
        public double estimateQoE(Long delay, double packetLoss) {
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
                MOS = 0;
            }
            else{
                MOS = 1 + 0.035*R + R*(R-60)*(100-R)/1000000;
            }

            return MOS;
        }

    },

    Video{

        @Override
        public String getName(){
            return "Video";
        }

        @Override
        public double estimateQoE(Long delay, double packetLoss) {
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
                MOS = 0;
            }
            else{
                MOS = 1 + 0.035*R + R*(R-60)*(100-R)/1000000;
            }

            return MOS;
        }

    }
}
