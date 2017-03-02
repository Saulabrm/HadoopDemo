package storm;

import java.util.Map;

import org.apache.storm.Config;
import org.apache.storm.StormSubmitter;
import org.apache.storm.hbase.bolt.HBaseBolt;
import org.apache.storm.hbase.bolt.mapper.SimpleHBaseMapper;
import org.apache.storm.hdfs.bolt.HdfsBolt;
import org.apache.storm.hdfs.bolt.format.DefaultFileNameFormat;
import org.apache.storm.hdfs.bolt.format.FileNameFormat;
import org.apache.storm.hdfs.bolt.rotation.FileRotationPolicy;
import org.apache.storm.hdfs.bolt.rotation.FileSizeRotationPolicy;
import org.apache.storm.hdfs.bolt.rotation.FileSizeRotationPolicy.Units;
import org.apache.storm.hdfs.bolt.sync.CountSyncPolicy;
import org.apache.storm.hdfs.bolt.sync.SyncPolicy;
import org.apache.storm.hive.bolt.HiveBolt;
import org.apache.storm.hive.bolt.mapper.DelimitedRecordHiveMapper;
import org.apache.storm.hive.common.HiveOptions;
import org.apache.storm.kafka.KafkaSpout;
import org.apache.storm.kafka.KeyValueSchemeAsMultiScheme;
import org.apache.storm.kafka.SpoutConfig;
import org.apache.storm.kafka.StringKeyValueScheme;
import org.apache.storm.kafka.ZkHosts;
import org.apache.storm.topology.IBasicBolt;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;

import com.google.common.collect.Maps;

public class Driver {

    private static String TOPOLOGY_NAME = "WordCountTopology";

    private static final String ZK_SERVERS = "localhost:2181";

    private static final String HDFS_URL = "hdfs://sandbox.hortonworks.com";
    private static final String HDFS_CATALOG = "/stormwc";

    private static final String KAFKA_TOPIC = "stormwctopic";
    private static final String KAFKA_ZK_ROOT = "/stormspouts";
    private static final String KAFKA_ZK_ZNODE = "stormwckafkaspout";

    private static final String HIVE_METASTORE_URI = "thrift://localhost:9083";
    private static final String HIVE_DBNAME = "default";
    private static final String HIVE_TABLENAME = "stormwc";
    private static final String[] HIVE_COLUMNNAMES = { "filename", "words", "count" };

    private static final String HBASE_TABLENAME = "stormwc";
    private static final String HBASE_KEY = "filename";
    private static final String HBASE_COLUMNFAMILY = "cfwc";
    private static final String[] HBASE_COLUMNNAMES = { "words", "count" };

    public static void main(String[] args) throws Exception {

        int numSpoutExecutors = 1;

        KafkaSpout wcKafkaSpout = buildWordCountSpout();
        IBasicBolt wcCountBolt = CustomWordCountBolt.GetBoltInstance();
        HdfsBolt wcHdfsBolt = buildHdfsBolt();
        HBaseBolt wcHBaseBolt = buildHBaseBolt();
        HiveBolt wcHiveBolt = buildHiveBolt();

        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout("wcspout", wcKafkaSpout, numSpoutExecutors);
        builder.setBolt("wccountbolt", wcCountBolt).shuffleGrouping("wcspout");
    //   builder.setBolt("wcHdfsBolt", wcHdfsBolt).shuffleGrouping("wccountbolt");
        builder.setBolt("wcHBaseBolt", wcHBaseBolt).shuffleGrouping("wccountbolt");
     //   builder.setBolt("wcHiveBolt", wcHiveBolt).shuffleGrouping("wccountbolt");

        Config cfg = new Config();
        Map<String, String> HBConfig = Maps.newHashMap();
        cfg.put("HBCONFIG",HBConfig);

        StormSubmitter.submitTopology(TOPOLOGY_NAME, cfg, builder.createTopology());
    }

    private static KafkaSpout buildWordCountSpout() {
        SpoutConfig spoutCfg = new SpoutConfig(new ZkHosts(ZK_SERVERS), KAFKA_TOPIC, KAFKA_ZK_ROOT, KAFKA_ZK_ZNODE);
        spoutCfg.scheme = new KeyValueSchemeAsMultiScheme(new
        StringKeyValueScheme());;

        return new KafkaSpout(spoutCfg);
    }

    private static HdfsBolt buildHdfsBolt() {
        FileRotationPolicy rotationPolicy = new FileSizeRotationPolicy(128.0f, Units.MB);
        FileNameFormat fileNameFormat = new DefaultFileNameFormat().withPath(HDFS_CATALOG);

        return new HdfsBolt().withFsUrl(HDFS_URL)
        .withFileNameFormat(fileNameFormat)
        .withRotationPolicy(rotationPolicy)
        .withSyncPolicy(new CountSyncPolicy(1000));
    }

    private static HiveBolt buildHiveBolt() {
        DelimitedRecordHiveMapper mapper = new DelimitedRecordHiveMapper()
            .withColumnFields(new Fields(HIVE_COLUMNNAMES));
        HiveOptions hiveOptions = new HiveOptions(HIVE_METASTORE_URI, HIVE_DBNAME, HIVE_TABLENAME, mapper);
        hiveOptions.withTxnsPerBatch(2);
        hiveOptions.withBatchSize(2);
        hiveOptions.withIdleTimeout(2);

        return new HiveBolt(hiveOptions);
    }

    private static HBaseBolt buildHBaseBolt() {
        SimpleHBaseMapper mapper = new SimpleHBaseMapper()
            .withRowKeyField(HBASE_KEY)
            .withColumnFields(new Fields(HBASE_COLUMNNAMES))
            .withColumnFamily(HBASE_COLUMNFAMILY);

        return new HBaseBolt(HBASE_TABLENAME, mapper).withConfigKey("HBCONFIG");
    }
}