package storm;

import java.util.HashMap;
import java.util.Map;

import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.IBasicBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.BiMap;

public class CustomWordCountBolt extends BaseBasicBolt {
	
	private static final Logger LOG = LoggerFactory.getLogger(CustomWordCountBolt.class);
	
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
		LOG.info("New tuple recieved");
		Map pairs = (Map)tuple.getValue(0);			
		for (Object key: pairs.keySet())
		{
			String keystr = String.valueOf(key);
			String text = (String)pairs.get(key);
			
			Map<String, Integer> wordsMap = CountWords(text.split("\\s+"));
			
			for (Map.Entry<String, Integer> entry : wordsMap.entrySet())
			{
				LOG.info(keystr+":    "+entry.getKey()+":    "+entry.getValue());
				
				oc.emit(new Values(keystr,entry.getKey(),entry.getValue()));
			}
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
