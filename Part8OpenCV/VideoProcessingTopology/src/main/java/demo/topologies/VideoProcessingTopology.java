package demo.topologies;

import demo.bolts.VideoDetectionBolt;

import java.util.Arrays;

import org.apache.storm.Config;
import org.apache.storm.StormSubmitter;
import org.apache.storm.drpc.LinearDRPCTopologyBuilder;

public class VideoProcessingTopology {

	public static void main(String[] args) throws Exception {		
		Config cfg = new Config();	
		cfg.put("drpc.servers", Arrays.asList(new String[]{"localhost"}));
		
		 LinearDRPCTopologyBuilder builder = new LinearDRPCTopologyBuilder("exclamation");
		 builder.addBolt(new VideoDetectionBolt(), 1);
		 
		 cfg.setNumWorkers(1);
		 StormSubmitter.submitTopology(args[0], cfg, builder.createRemoteTopology());

	}
}
