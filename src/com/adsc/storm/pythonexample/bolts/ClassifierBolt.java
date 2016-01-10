package com.adsc.storm.pythonexample.bolts;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import com.adsc.storm.pythonexample.util.Constants;
import com.adsc.storm.pythonexample.util.SVMModel;
import com.adsc.storm.pythonexample.util.SVMReader;
import com.adsc.storm.pythonexample.util.Util;

import java.util.Map;

/**
 * Created by root on 15-12-4.
 */
public class ClassifierBolt implements IRichBolt, Constants{

    OutputCollector outputCollector;
    String normFilePath = "/home/jiamingmai/Downloads/svm_for_cnnaction/norm_weights.txt";
    String svmModelFilePath = "/home/jiamingmai/Downloads/svm_for_cnnaction/svm_weights.txt";
    float[] normParms;
    SVMModel[] svmModels;

    @Override
    public void prepare(Map conf, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.outputCollector = outputCollector;
        SVMReader svmReader = new SVMReader(normFilePath, svmModelFilePath, 14);
        normParms = svmReader.getNormParms();
        svmModels = svmReader.getSvmModels();
    }

    @Override
    public void execute(Tuple input) {
        long startTime = System.currentTimeMillis();

        // Arrange the incoming features
        //
        String data = input.getString(0);
        int id = input.getInteger(1);
        String[] featsStr = data.split(",");
        float[] feats = new float[featsStr.length];
        for (int i = 0; i < feats.length; i++){
           feats[i] = Float.parseFloat(featsStr[i].trim());
        }

        // Classfy with the SVM model
        //
        Object[] res = Util.classifyWithMultiSVM(normParms, svmModels, feats);
        int label = (int) res[0];
        float maxConf = (float) res[1];

        // Emit the result
        //
        outputCollector.emit(new Values(label, maxConf));

        long endTime = System.currentTimeMillis();
        long runningTime = endTime - startTime;
        System.out.println("Running Time (ClassiferBolt): " + runningTime + " ms");
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(OUTPUT_LABEL, MAX_CONF));
    }

    @Override
    public void cleanup() {

    }
}

