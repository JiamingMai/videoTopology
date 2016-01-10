package com.adsc.storm.pythonexample.spouts;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import com.adsc.storm.pythonexample.util.Constants;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import java.io.File;
import java.util.Map;
import java.util.Random;

/**
 * Created by root on 15-11-30.
 */
public class FramesSenderSpout extends BaseRichSpout implements Constants{

    SpoutOutputCollector spoutOutputCollector;
    Random random;
    //String imgDirPath = "/usr/pictures/imagesData";
    String imgDirPath = "/usr/testPython/frames";

    final String VIDEO_PATH = "/home/jiamingmai/Downloads/inputVideo/v_BoxingPunchingBag_g01_c02.avi";
    VideoCapture capture;

    @Override
    public void open(Map conf, TopologyContext topologyContext, SpoutOutputCollector spoutOutputCollector) {
        this.spoutOutputCollector = spoutOutputCollector;
        random = new Random();
        capture = new VideoCapture(VIDEO_PATH);
    }

    @Override
    public void nextTuple() {
        Mat frameImg = new Mat();
        int id = 0;
        if (capture.isOpened() == true){
            while(true){
                capture.read(frameImg);
                if (frameImg.empty() != true){
                    byte[] buffer = new byte[(int)frameImg.total()*frameImg.channels()];
                    frameImg.get(0, 0, buffer);
                    spoutOutputCollector.emit(new Values(frameImg.height(), frameImg.width(), frameImg.channels(), buffer, id));
                }
                id++;
            }
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(FRAME_HEIGHT, FRAME_WIDTH, FRAME_CHANNELS, FRAME_IMG, FRAME_ID));
    }
}
