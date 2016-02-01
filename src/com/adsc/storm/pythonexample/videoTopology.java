package com.adsc.storm.pythonexample;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.topology.TopologyBuilder;
import com.adsc.storm.pythonexample.bolts.FeatureExtractorBolt;
import com.adsc.storm.pythonexample.bolts.ProcessingBolt;
import com.adsc.storm.pythonexample.spouts.FramesSenderSpout;
import com.adsc.storm.pythonexample.util.Constants;
import org.opencv.core.Core;

/**
 * Created by root on 15-11-30.
 */
public class videoTopology implements Constants{

    static{System.loadLibrary(Core.NATIVE_LIBRARY_NAME);}

    private static TopologyBuilder builder = new TopologyBuilder();

    public static void main(String[] args){

        Config config = new Config();

        builder.setSpout("FramesSenderSpout", new FramesSenderSpout(), 1);
        builder.setBolt("FeatureExtractorBolt", new FeatureExtractorBolt(), 2).shuffleGrouping("FramesSenderSpout");
        builder.setBolt("ProcessingBolt", new ProcessingBolt(), 1).shuffleGrouping("FeatureExtractorBolt");
        config.setDebug(false);

        if (args != null && args.length > 0){
            try{
                config.setNumWorkers(2);
                StormSubmitter.submitTopology(args[0], config, builder.createTopology());
            }catch(Exception e){
                e.printStackTrace();
            }
        }else{
            System.out.println("Local Mode");
            config.setMaxTaskParallelism(1);
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("PythonTest", config, builder.createTopology());
        }
    }
}
