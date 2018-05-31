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
public interface Video {


    /**
     * The method which returns the application type's name (video).
     *
     * @return      The application type's name.
     */
    static String getName(){
        return "Video";
    }


    /**
     * The method which computes the video duration.
     *
     * @param videoLocation     The video's absolute path in the file system.
     * @return                  The video duration.
     */
 /*   static float computeVideoDuration(String videoLocation){

        //TODO : check function
        int duration = -1;
        String command = "ffmpeg -i file.flv 2>&1 | grep \"Duration\"| cut -d ' ' -f 4 | sed s/,// | sed 's@\\..*@@g' | awk '{ split($1, A, \":\"); split(A[3], B, \".\"); print 3600*A[1] + 60*A[2] + B[1] }'";
        ExecuteShellCommand obj = new ExecuteShellCommand();
        String output = obj.executeCommand(command);
        if (output != null) {
            duration = Integer.parseInt(output);
        }
        return duration;
    }*/

}
