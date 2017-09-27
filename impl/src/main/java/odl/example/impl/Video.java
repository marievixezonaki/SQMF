/*
 * Copyright © 2017 M.E Xezonaki in the context of her MSc Thesis, Department of Informatics and Telecommunications, UoA.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package odl.example.impl;

public class Video {

    private static double v1 = 1.431, v2 = 2.228/100, v3 = 3.759, v4 = 184.1, v5 = 1.161,
            v6 = 1.446, v7 = 3.881/10000, v8 = 2.116, v9 = 467.4, v10 = 2.736, v11 = 15.28, v12 = 4.170;

    public static String getName(){
        return "Video";
    }

    public static double estimateQoE(float frameRate, float bitRate, double packetLoss) {

        double OFr = v1 + v2*bitRate;
        double IOfr = v3 - v3/(1 + (Math.pow(bitRate, v5)/v4));
        double DFrv = v6 + v7*bitRate;
        double DPplV = v10 + v11*Math.exp(-frameRate/v8) + v12*Math.exp(-bitRate/v9);

      //  System.out.println("OFr " + OFr);
     //   System.out.println("IOfr " + IOfr);
    //    System.out.println("DFrv " + DFrv);
    //    System.out.println("DPplV " + DPplV);

        double numeratorIcoding = -Math.pow((Math.log(frameRate)-Math.log(OFr)), 2);
        double denominatorIcoding = 2*Math.pow(DFrv, 2);
        double Icoding = IOfr*Math.exp(numeratorIcoding/denominatorIcoding);
        double Itransmission = Math.exp(-(packetLoss/DPplV));
      //  System.out.println("Icoding " + Icoding);
      //  System.out.println("Itransmission " + Itransmission);
        double MOS = 1 + Icoding*Itransmission;
        return MOS;
    }

    public static String getVideoCodec(String videoLocation){
        return null;
    }

    public static float getVideoFPS(String videoLocation){
        float frameRate;
        String command = "ffmpeg -i " + videoLocation + " -hide_banner";
        //      System.out.println(command);
        ExecuteShellCommand obj = new ExecuteShellCommand();
        String output = obj.executeCommand(command);
        if (output != null) {
            //       System.out.println(output);
            String[] outputParts = output.split(",");
            for (int i = 0; i < outputParts.length; i++){
                if (outputParts[i].contains("fps")){
                    String fps = outputParts[i];
                    String[] fpsParts = fps.split(" ");
                    if (fpsParts.length > 2){
                        frameRate = Float.parseFloat(fpsParts[1]);
                        System.out.println(frameRate);
                        return frameRate;
                    }
                    break;
                }
            }
        }
        return -1;
    }

    public static int getKeyFrame(String videoLocation){
        return -1;
    }

    public static String getVideoFormat(String videoLocation){
        return null;
    }
}
