package com.adsc.storm.pythonexample;

import com.adsc.storm.pythonexample.entities.SerializableMat;
import com.adsc.storm.pythonexample.util.SerializeUtil;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import redis.clients.jedis.Jedis;

/**
 * Hello world!
 */
public class RedisWriter {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private static volatile Jedis jedis;

    private RedisWriter() {
    }

    public static Jedis getJedisInstance() {
        if (jedis == null) {
            synchronized (RedisWriter.class) {
                if (jedis == null) {
                    jedis = new Jedis("localhost");
                }
            }
        }
        return jedis;
    }

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        Jedis jedis = RedisWriter.getJedisInstance();
        final String VIDEO_PATH = "/home/jiamingmai/Downloads/inputVideo/v_BoxingPunchingBag_g01_c02.avi";
        VideoCapture capture = new VideoCapture(VIDEO_PATH);
        Mat frameImg = new Mat();
        int frameId = 0;
        if (capture.isOpened() == true) {
            while (true) {
                capture.read(frameImg);
                if (frameImg.empty() == false) {
                    if(frameId == 0){
                        jedis.set("frameHei", new String(frameImg.rows()+""));
                        jedis.set("frameWid", new String(frameImg.cols()+""));
                        jedis.set("frameChannels", new String(frameImg.channels() + ""));
                    }
                    SerializableMat serializableMat = new SerializableMat(frameImg);
                    byte[] serializedMatBytes = SerializeUtil.serialize(serializableMat);
                    jedis.set(new String("frame:" + frameId).getBytes(), serializedMatBytes);
                    System.out.println("Frame #" + frameId + " Written");
                } else {
                    System.out.println("All the frames haven been written in redis!");
                    break;
                }
                frameId++;
            }
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Total Time: " + (endTime - startTime) + " ms");
    }
}
