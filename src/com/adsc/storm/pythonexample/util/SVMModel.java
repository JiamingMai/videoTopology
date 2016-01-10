package com.adsc.storm.pythonexample.util;

/**
 * Created by root on 15-12-10.
 */
public class SVMModel {
    private int id;
    private float[] weights;
    private float bias;

    public SVMModel(int id, float[] weights, float bias) {
        this.id = id;
        this.weights = weights;
        this.bias = bias;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setWeights(float[] weights){
        this.weights = weights;
    }

    public void setBias(float bias){
        this.bias = bias;
    }

    public int getId() {
        return id;
    }

    public float[] getWeights(){
        return weights;
    }

    public float getBias(){
        return bias;
    }
}
