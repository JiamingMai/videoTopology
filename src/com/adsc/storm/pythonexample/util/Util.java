package com.adsc.storm.pythonexample.util;

import java.io.*;
import java.util.*;

/**
 * Created by root on 15-12-9.
 */
public class Util {

    public static float innerProduct(float[] x1, float[] y1) {
        if (x1.length != y1.length) {
            System.out.println("warning, x1.length != y1.length, "
                    + x1.length + ", " + y1.length);
            return -1;
        }
        float retVal = 0;
        for (int i = 0; i < y1.length; i++) {
            retVal += x1[i] * y1[i];
        }
        return retVal;
    }

    public static float[] maxPooling(List<float[]> list){
        if(list.isEmpty() != true){
            // Get the dimension
            //
            float[] tmp = list.get(0);
            int dim = tmp.length;

            // Do max pooling
            //
            float[] vec = new float[dim];
            for(int i = 0; i < dim; i++){
                float maxVal = list.get(0)[i];
                for(int j = 1; j< list.size(); j++){
                    float tmpVal = list.get(j)[i];
                    if(tmpVal > maxVal){
                        maxVal = tmpVal;
                    }
                }
                vec[i] = maxVal;
            }
            return vec;
        }else{
            return null;
        }
    }

    public static float[] normalizeFeats(float[] normParms, float[] pooledFeats){
        float denominator = 0;
        for (int i = 0; i < normParms.length; i++){
            pooledFeats[i] = pooledFeats[i]/normParms[i];
            denominator += pooledFeats[i]*pooledFeats[i];
        }
        denominator = (float)Math.sqrt(denominator);
        // Have a L2 normalization
        //
        for(int i = 0; i < pooledFeats.length; i++){
            pooledFeats[i] = pooledFeats[i]/denominator;
        }
        return pooledFeats;
    }

    public static float classcifyWithSingleSVM(SVMModel svmModel, float[] feats){
        float[] weights = svmModel.getWeights();
        float confidence = Util.innerProduct(weights, feats) + svmModel.getBias();
        //System.out.println("Class #" + svmModel.getId() +" confidence = " + confidence);
        return confidence;
    }

    public static Object[] classifyWithMultiSVM(float[] normParms, SVMModel[] svmModels, float[] pooledFeats){
        pooledFeats = normalizeFeats(normParms, pooledFeats);
        float maxConf = Float.NEGATIVE_INFINITY;
        int label = 0;
        for(int i = 0; i < svmModels.length; i++){
            float conf = classcifyWithSingleSVM(svmModels[i], pooledFeats);
            if (conf > maxConf){
                maxConf = conf;
                label = i;
            }
        }
        return new Object[]{label, maxConf};
    }

}
