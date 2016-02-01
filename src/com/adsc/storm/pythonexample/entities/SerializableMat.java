package com.adsc.storm.pythonexample.entities;

import org.opencv.core.Mat;

import java.io.Serializable;

/**
 * Created by root on 16-2-1.
 */
public class SerializableMat implements Serializable{
    private byte[] matByte;

    public SerializableMat(){
        Mat mat = new Mat();
        matByte = new byte[(int)mat.total()*mat.channels()];
        mat.get(0, 0, matByte);
    }

    public SerializableMat(Mat mat){
        matByte = new byte[(int)mat.total()*mat.channels()];
        mat.get(0, 0, matByte);
    }

    public byte[] getMatByte(){
        return matByte;
    }

    public void setMat(byte[] matByte){
        this.matByte = matByte;
    }
}
