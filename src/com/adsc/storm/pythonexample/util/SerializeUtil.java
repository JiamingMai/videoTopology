package com.adsc.storm.pythonexample.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by root on 16-2-1.
 */
public class SerializeUtil {

    public static byte[] serialize(Object obj){
        ByteArrayOutputStream baos = null;
        ObjectOutputStream oos = null;
        try{
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            return baos.toByteArray();
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static Object unserialize(byte[] bytes){
        ByteArrayInputStream bais = null;
        try{
            bais = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bais);
            return ois.readObject();
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
