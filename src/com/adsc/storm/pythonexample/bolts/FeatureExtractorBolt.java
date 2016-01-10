package com.adsc.storm.pythonexample.bolts;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import com.adsc.storm.pythonexample.util.Constants;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Map;

/**
 * Created by root on 15-11-30.
 */
public class FeatureExtractorBolt implements IRichBolt, Constants{

    static{System.loadLibrary(Core.NATIVE_LIBRARY_NAME);}
    public static final String IP_ADDRESS = "127.0.0.1";
    public static final int PORT_NO = 8881;

    OutputCollector outputCollector;
    String componenetId;

    @Override
    public void prepare(Map conf, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.outputCollector = outputCollector;
        componenetId = topologyContext.getThisComponentId();
    }

    @Override
    public void execute(Tuple input) {

        long startTime = System.currentTimeMillis();

        int imgHeight = input.getInteger(0);
        int imgWidth = input.getInteger(1);
        int imgChannels = input.getInteger(2);
        byte[] buffer = input.getBinary(3);
        int id = input.getInteger(4);

        String s;
        StringBuffer dataBuf = new StringBuffer();
        String data;

        // Connect the local python script by socket
        // and get back the extracted features
        //
        try {
            Socket sock = new Socket(IP_ADDRESS, PORT_NO);
            DataOutputStream dos = new DataOutputStream(sock.getOutputStream());
            //System.out.println("Array Length = " + buffer.length);
            dos.writeInt(imgHeight);
            dos.writeInt(imgWidth);
            dos.writeInt(buffer.length);
            dos.write(buffer);
            dataBuf = new StringBuffer();
            BufferedReader br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            dataBuf.append(br.readLine());
            sock.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Check and print the received data
        if (dataBuf != null){
            data = new String(dataBuf);
            outputCollector.emit(new Values(data, id));
        }else{
            System.out.println("Error: no data received! ");
        }

        long endTime = System.currentTimeMillis();
        long runningTime = endTime - startTime;
        //System.out.println("Running Time (FeatureExtractorBolt): " + runningTime + " ms");
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer){
        outputFieldsDeclarer.declare(new Fields(EXTRACTED_FEATS, FRAME_ID));
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }

    @Override
    public void cleanup() {

    }
}
