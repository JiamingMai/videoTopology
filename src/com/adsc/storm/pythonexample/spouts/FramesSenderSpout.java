package com.adsc.storm.pythonexample.spouts;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import com.adsc.storm.pythonexample.entities.SerializableMat;
import com.adsc.storm.pythonexample.util.Constants;
import com.adsc.storm.pythonexample.util.SerializeUtil;
import redis.clients.jedis.Jedis;

import java.util.Map;
import java.util.Random;

/**
 * Created by root on 15-11-30.
 */
public class FramesSenderSpout extends BaseRichSpout implements Constants {

    SpoutOutputCollector spoutOutputCollector;
    Random random;
    final String host = "localhost";
    Jedis jedis;
    int frameHei;
    int frameWid;
    int frameChannels;

    @Override
    public void open(Map conf, TopologyContext topologyContext, SpoutOutputCollector spoutOutputCollector) {
        jedis = new Jedis(host);
        frameHei = Integer.parseInt(jedis.get("frameHei"));
        frameWid = Integer.parseInt(jedis.get("frameWid"));
        frameChannels = Integer.parseInt(jedis.get("frameChannels"));
        this.spoutOutputCollector = spoutOutputCollector;
        random = new Random();
    }

    @Override
    public void nextTuple() {
        int id = 0;
        while (true) {
            String key = new String("frame:" + id);
            byte[] serializedMatBytes = jedis.get(key.getBytes());
            if(serializedMatBytes == null){
                break;
            }
            SerializableMat serializableMat = (SerializableMat) SerializeUtil.unserialize(serializedMatBytes);
            byte[] matByates = serializableMat.getMatByte();
            spoutOutputCollector.emit(new Values(frameHei, frameWid, frameChannels, matByates, id));
            jedis.del(key.getBytes());
            id++;
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(FRAME_HEIGHT, FRAME_WIDTH, FRAME_CHANNELS, FRAME_IMG, FRAME_ID));
    }
}
