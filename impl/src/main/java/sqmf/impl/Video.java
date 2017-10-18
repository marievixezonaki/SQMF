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
public class Video {

    private static double v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12;



    /**
     * The method which returns the application type's name (video).
     *
     * @return      The application type's name.
     */
    public static String getName(){
        return "Video";
    }



    /**
     * The method which computes the QoE for video applications.
     *
     * @param frameRate     The video's frame rate (FPS - frames/sec).
     * @param bitRate       The video's bit rate (bits/sec).
     * @param packetLoss    The computed packet loss (%).
     * @param videoCase     The number of the video cases (1-5) where the examined video belongs.
     *                          * Case 1 : keyFrame = 1, codec = mpeg4, format = 320x240 (QQVGA)
                                * Case 2 : keyFrame = 1, codec = mpeg4, format = 160x120 (QVGA)
                                * Case 3 : keyFrame = 1, codec = mpeg2, format = 640x480 (VGA)
                                * Case 4 : keyFrame = 1, codec = mpeg4, format = 640x480 (VGA)
                                * Case 5 : keyFrame = 1, codec = h264, format = 640x480 (VGA)
     *
     * @return              The computed QoE value.
     */
    public static double estimateQoE(float frameRate, float bitRate, double packetLoss, int videoCase) {

        assignCoefficientsValues(videoCase);
        double OFr = v1 + v2*bitRate;
        double IOfr = v3 - v3/(1 + (Math.pow(bitRate, v5)/v4));
        double DFrv = v6 + v7*bitRate;
        double DPplV = v10 + v11*Math.exp(-frameRate/v8) + v12*Math.exp(-bitRate/v9);

        double numeratorIcoding = -Math.pow((Math.log(frameRate)-Math.log(OFr)), 2);
        double denominatorIcoding = 2*Math.pow(DFrv, 2);
        double Icoding = IOfr*Math.exp(numeratorIcoding/denominatorIcoding);
        double Itransmission = Math.exp(-(packetLoss/DPplV));
        double Vq = 1 + Icoding*Itransmission;
        return Vq;
    }



    /**
     * The method which computes video's codec.
     *
     * @param videoLocation     The video's absolute path in the file system.
     */
    public static String getVideoCodec(String videoLocation){
        String videoCodec;
        String command = "ffmpeg -i " + videoLocation + " -hide_banner";
        ExecuteShellCommand obj = new ExecuteShellCommand();
        String output = obj.executeCommand(command);
        if (output != null) {
            String[] outputParts = output.split(",");
            for (int i = 0; i < outputParts.length; i++){
                if (outputParts[i].contains("Video:")){
                    String[] codecParts = outputParts[i].split(" ");
                    for (int j = 0; j < codecParts.length; j++){
                        if (codecParts[j].contains("Video")){
                            videoCodec = codecParts[j+1];
                            return videoCodec;
                        }
                    }
                }
            }
        }
        return null;
    }



    /**
     * The method which computes video's frame rate.
     *
     * @param videoLocation     The video's absolute path in the file system.
     */
    public static float getVideoFPS(String videoLocation){
        float frameRate;
        String command = "ffmpeg -i " + videoLocation + " -hide_banner";
        ExecuteShellCommand obj = new ExecuteShellCommand();
        String output = obj.executeCommand(command);
        if (output != null) {
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



    /**
     * The method which computes video's key frame interval.
     *
     * @param videoLocation     The video's absolute path in the file system.
     */
    public static int getKeyFrame(String videoLocation){
        int keyFrame;
        String[] command = { "ffprobe", "-select_streams", "v:0", "-show_frames", videoLocation, "-hide_banner" };
        ExecuteShellCommand obj = new ExecuteShellCommand();
        String output = obj.executeCommand(command);
        if (output != null) {
            String[] outputParts = output.split("\\=");

            boolean firstTimeFindingFrame = true;
            for (int i = 0; i < outputParts.length; i++){
                if (outputParts[i].contains("key_frame")){
                    String[] keyFrameParts = outputParts[i+1].split("\\r?\\n");
                    keyFrame = Integer.parseInt(keyFrameParts[0]);
                    if (firstTimeFindingFrame){
                        firstTimeFindingFrame = false;
                    }
                    else{
                        return keyFrame;
                    }
                }
            }
        }
        return -1;
    }



    /**
     * The method which computes video's format.
     *
     * @param videoLocation     The video's absolute path in the file system.
     */
    public static String getVideoFormat(String videoLocation){

        int targetTimes = 2;
        boolean pathContainsX = videoLocation.contains("x");
        if (pathContainsX){
            targetTimes++;
        }
        String videoFormat;
        String command = "ffmpeg -i " + videoLocation + " -hide_banner";
        ExecuteShellCommand obj = new ExecuteShellCommand();
        String output = obj.executeCommand(command);
        if (output != null) {
            String[] outputParts = output.split(",");
            int times = 0;
            for (int i = 0; i < outputParts.length; i++){
                if (outputParts[i].contains("x")){
                    times++;
                    if (times == targetTimes){
                        String[] codecParts = outputParts[i].split(" ");
                        String format = codecParts[1];
                        String width = format.substring(0, 3);
                        String height = format.substring(4);
                        videoFormat = width.concat("x").concat(height);
                        return videoFormat;
                    }
                }
            }
        }
        return null;
    }


    /**
     * The method which computes the video bit rate.
     *
     * @param videoLocation     The video's absolute path in the file system.
     * @return                  The video bit rate.
     */
    public static float computeVideoBitRate(String videoLocation){

        float bitRate = -1;
        String command = "ffmpeg -i " + videoLocation + " -hide_banner";
        ExecuteShellCommand obj = new ExecuteShellCommand();
        String output = obj.executeCommand(command);
        if (output != null) {
            String[] outputParts = output.split(" ");
            for (int i = 0; i < outputParts.length; i++){
                if (outputParts[i].contains("bitrate")){
                    bitRate = Float.parseFloat(outputParts[i+1]);
                    bitRate = bitRate*1000;
                    break;
                }
            }
        }
        return bitRate;
    }



    /**
     * The method which assigns values to coefficients v1-v12 needed for the video QoE estimation formula, according
     * to the case (1-5) the video belongs to.
     * Case 1 : keyFrame = 1, codec = mpeg4, format = 320x240 (QQVGA)
     * Case 2 : keyFrame = 1, codec = mpeg4, format = 160x120 (QVGA)
     * Case 3 : keyFrame = 1, codec = mpeg2, format = 640x480 (VGA)
     * Case 4 : keyFrame = 1, codec = mpeg4, format = 640x480 (VGA)
     * Case 5 : keyFrame = 1, codec = h264, format = 640x480 (VGA)
     *
     * @param videoCase     The number of the video cases (1-5) where the examined video belongs.
     */
    public static void assignCoefficientsValues(int videoCase){
        if (videoCase == 1){
            v1 = 1.431;
            v2 = 0.02228;
            v3 = 3.759;
            v4 = 184.1;
            v5 = 1.161;
            v6 = 1.446;
            v7 = 0.0003881;
            v8 = 2.116;
            v9 = 467.4;
            v10 = 2.736;
            v11 = 15.28;
            v12 = 4.170;
        }
        else if (videoCase == 2){
            v1 = 7.16;
            v2 = 0.02215;
            v3 = 3.461;
            v4 = 111.9;
            v5 = 2.091;
            v6 = 1.382;
            v7 = 0.0005881;
            v8 = 0.8401;
            v9 = 113.9;
            v10 = 6.047;
            v11 = 46.87;
            v12 = 10.87;
        }
        else if (videoCase == 3){
            v1 = 4.78;
            v2 = 0.0122;
            v3 = 2.614;
            v4 = 51.68;
            v5 = 1.063;
            v6 = 0.898;
            v7 = 0.0006923;
            v8 = 0.7846;
            v9 = 85.15;
            v10 = 1.32;
            v11 = 539.48;
            v12 = 356.6;
        }
        else if (videoCase == 4){
            v1 = 1.182;
            v2 = 0.0111;
            v3 = 4.286;
            v4 = 607.86;
            v5 = 1.184;
            v6 = 2.738;
            v7 = -0.000998;
            v8 = 0.896;
            v9 = 187.24;
            v10 = 5.212;
            v11 = 254.11;
            v12 = 268.24;
        }
        else if (videoCase == 5){
            v1 = 5.517;
            v2 = 0.0129;
            v3 = 3.459;
            v4 = 178.53;
            v5 = 1.02;
            v6 = 1.15;
            v7 = 0.000355;
            v8 = 0.114;
            v9 = 513.77;
            v10 = 0.736;
            v11 = -6.451;
            v12 = 13.684;
        }
    }
}
