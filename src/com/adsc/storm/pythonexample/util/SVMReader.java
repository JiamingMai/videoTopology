package com.adsc.storm.pythonexample.util;

import java.io.*;

/**
 * Created by root on 15-12-10.
 */
public class SVMReader {
    private float[] normParms;
    private SVMModel[] svmModels;

    public SVMReader(String normFilePath, String modelFilePath, int classNum){
        readSVM(modelFilePath, classNum);
        readNormParameters(normFilePath);
    }

    public void readSVM(String modelFilePath, int classNum){
        try{
            svmModels = new SVMModel[classNum];

            FileInputStream fs = new FileInputStream(new File(modelFilePath));
            BufferedReader br = new BufferedReader(new InputStreamReader(fs));
            String line;
            for (int i = 0; i < classNum; i++){
                // Read the weights from the file
                //
                line = br.readLine();
                String[] weightsStr = line.split(" ");
                /*for (String weight : weightsStr)
                    System.out.print(weight + " ");
                System.out.println();*/

                float[] weights = cvtStrArrToFloArr(weightsStr);

                // Read the bias from the file
                //
                line = br.readLine();
                float bias = Float.parseFloat(line.split(" ")[0]);

                // Create the SVMModel Object
                //
                svmModels[i] = new SVMModel(i, weights, bias);
            }
        }catch(IOException ex){
            ex.printStackTrace();
        }
    }

    public void readNormParameters(String normFilePath){
        try {
            FileInputStream fs = new FileInputStream(new File(normFilePath));
            BufferedReader br = new BufferedReader(new InputStreamReader(fs));
            String line;
            StringBuffer buf = new StringBuffer();
            int count = 1;
            while((line = br.readLine()) != null){
                System.out.println("count: " + count);
                count++;
                buf.append(line);
            }
            String[] parmsStr = buf.toString().split(" ");
            normParms = new float[parmsStr.length];
            for (int i = 0; i < normParms.length; i++){
                normParms[i] = Float.parseFloat(parmsStr[i].trim());
            }
        }catch(IOException ex){
            ex.printStackTrace();
        }

    }

    private float[] cvtStrArrToFloArr(String[] strings){
        float[] floats = new float[strings.length];
        for (int i = 0; i < floats.length; i++){
            floats[i] = Float.parseFloat(strings[i].trim());
        }
        return floats;
    }

    public void setSvmModels(SVMModel[] svmModels) {
        this.svmModels = svmModels;
    }

    public void setNormParms(float[] normParms) {
        this.normParms = normParms;
    }

    public SVMModel[] getSvmModels() {
        return svmModels;
    }

    public float[] getNormParms() {
        return normParms;
    }
}
