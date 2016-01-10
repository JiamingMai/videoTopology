package com.adsc.storm.pythonexample.bolts;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;
import com.adsc.storm.pythonexample.util.SVMModel;
import com.adsc.storm.pythonexample.util.SVMReader;
import com.adsc.storm.pythonexample.util.Util;

import java.awt.*;
import java.io.Serializable;
import java.util.*;
import java.util.List;

/**
 * Created by root on 15-12-4.
 */
public class ProcessingBolt implements IRichBolt{

    OutputCollector outputCollector;
    QueueProcessor queueProcessor;

    @Override
    public void prepare(Map conf, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.outputCollector = outputCollector;
        queueProcessor = new QueueProcessor();
        queueProcessor.start();
    }

    @Override
    public void execute(Tuple input) {
        long startTime = System.currentTimeMillis();

        String data = input.getString(0);
        int id = input.getInteger(1);
        String[] featsStr = data.split(",");
        float[] feats = new float[featsStr.length];
        for (int i = 0; i < feats.length; i++){
           feats[i] = Float.parseFloat(featsStr[i].trim());
        }
        FrameFeatures frameFeatures = new FrameFeatures(id, feats);
        System.out.println("Adding frame features to queue");
        queueProcessor.queueAdd(frameFeatures);

        long endTime = System.currentTimeMillis();
        long runningTime = endTime - startTime;
        //System.out.println("Running Time (ProcessingBolt): " + runningTime + " ms");
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {

    }

    @Override
    public void cleanup() {

    }

    public void printFeats(String[] feats){
        System.out.println("--------------------------------");
        for(String feat : feats){
            System.out.println(feat.trim());
        }
        System.out.println("--------------------------------");
    }
}

class FrameFeatures implements Serializable{
    private int frameId;
    private float[] feats;

    public FrameFeatures(int frameId, float[] feats){
        this.frameId = frameId;
        this.feats = feats;
    }

    public void setFrameId(int frameId){
        this.frameId = frameId;
    }

    public int getFrameId(){
        return frameId;
    }

    public void setFeats(float[] feats) {
        this.feats = feats;
    }

    public float[] getFeats() {
        return feats;
    }
}

class FrameFeaturesComparator implements Comparator<FrameFeatures>{
    @Override
    public int compare(FrameFeatures o1, FrameFeatures o2) {
        return o1.getFrameId() - o2.getFrameId();
    }
}

class QueueProcessor implements  Runnable{
    Thread t;
    PriorityQueue<FrameFeatures> priorityQueue;
    List<float[]> featsList;
    String normFilePath = "/home/jiamingmai/Downloads/svm_for_cnnaction/norm_weights.txt";
    String svmModelFilePath = "/home/jiamingmai/Downloads/svm_for_cnnaction/svm_weights.txt";
    float[] normParms;
    SVMModel[] svmModels;
    int windowSize = 30;
    int currentFrameId = 0;

    public QueueProcessor(){
        priorityQueue = new PriorityQueue<>(10, new FrameFeaturesComparator());
        featsList = new ArrayList<float[]>();
        SVMReader svmReader = new SVMReader(normFilePath, svmModelFilePath, 14);
        normParms = svmReader.getNormParms();
        svmModels = svmReader.getSvmModels();
    }

    public void queueAdd(FrameFeatures frameFeatures){
        priorityQueue.add(frameFeatures);
    }

    public FrameFeatures queuePoll(){
        synchronized (priorityQueue){
            return priorityQueue.poll();
        }
    }

    public FrameFeatures queuePeek(){
        synchronized (priorityQueue){
            return priorityQueue.isEmpty() ? null : priorityQueue.peek();
        }
    }

    public int queueSize(){
        synchronized (priorityQueue){
            return priorityQueue.size();
        }
    }

    @Override
    public void run() {
        // Do the processing here
        //
        while(true){
            FrameFeatures frameFeatures = queuePeek();
            if(frameFeatures != null){
                if(frameFeatures.getFrameId() < currentFrameId - windowSize){
                    queuePoll();
                }
                if (frameFeatures.getFrameId() >= currentFrameId - windowSize && frameFeatures.getFrameId() < currentFrameId){
                    System.out.println("add queque ID: " + frameFeatures.getFrameId());
                    featsList.add(queuePoll().getFeats());
                    updateInfo();
                }else{
                    currentFrameId++;
                }
            }else{
                try {
                    System.out.println("The thread is waiting...");
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void start(){
        if (t == null){
            t = new Thread(this);
            t.start();
        }
    }

    public void updateInfo(){
        if(featsList.size() >= windowSize){
            float[] pooledFeats = Util.maxPooling(featsList);
            Object[] res = Util.classifyWithMultiSVM(normParms, svmModels, pooledFeats);
            int label = (int) res[0];
            float maxConf = (float) res[1];
            System.out.println("Frame #" + currentFrameId +": Label = " + label + ", " + "Max confidence = " + maxConf);
            featsList.clear();
        }
    }
}
