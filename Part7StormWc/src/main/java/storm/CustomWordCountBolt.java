package storm;

import java.util.HashMap;
import java.util.Map;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.IBasicBolt;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import com.sun.tools.javac.parser.Scanner;

public class CustomWordCountBolt extends BaseBasicBolt {
	
	public static IBasicBolt GetBoltInstance()
	{
		return new CustomWordCountBolt();
	}
	
	private CustomWordCountBolt()
	{		
	}

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("filename", "word", "count"));
	}

	public void execute(Tuple tuple, BasicOutputCollector oc) {
		System.out.println("New tuple recieved");
		
		String key = tuple.getString(0);
		String text = tuple.getString(1);
		Map<String, Integer> wordsMap = CountWords(text.split("\\s+"));
		
		for (Map.Entry<String, Integer> entry : wordsMap.entrySet())
		{
			oc.emit(new Values(tuple.getString(0),entry.getKey(),entry.getValue()));
		}
	}

	private Map<String, Integer> CountWords(String[] words) {
		Map<String, Integer> map = new HashMap<String, Integer>();
		for (String s : words) {

			if (!map.containsKey(s)) { 
				map.put(s, 1);
			} else {
				int count = map.get(s);
				map.put(s, count + 1);
			}
		}
		return map;
	}
}
