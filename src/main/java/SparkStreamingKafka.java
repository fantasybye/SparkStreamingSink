import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;
import org.bson.Document;
import scala.Tuple2;
import utils.Trie;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class SparkStreamingKafka {
    private static final Integer schoolNum = 50;
//    private static final String CHECK_POINT_DIR = "D:\\updatestatebykey2";
//    private static final String CHECK_POINT_DIR = "hdfs://mycluster/weibo/checkdir";
    private static final int CHECK_POINT_DURATION_SECONDS = 30;
    private static final String brokers = "slave1:9092,slave2:9092,slave3:9092";
    private static final String filePath = "hdfs://mycluster/test/schoolTest";
    //        String topics = "user_behavior";
    private static final String topics = "weibo_content";
//    private static final String topics = "school_test";
    private static AtomicLong[] schoolCount = new AtomicLong[schoolNum];
    private static final Trie trie = new Trie();
    public static void main(String[] args) throws InterruptedException {
        for (int i = 0;i<schoolNum;i++){
            schoolCount[i] = new AtomicLong(0);
        }
        trie.init();
        SparkConf conf = new SparkConf().setMaster("local[2]").setAppName("test");
//        SparkConf conf = new SparkConf().setMaster("spark://master:7077").setAppName("SparkStreaming");
        JavaSparkContext sc = new JavaSparkContext(conf);
        sc.setLogLevel("WARN");
        JavaStreamingContext ssc = new JavaStreamingContext(sc, Durations.seconds(CHECK_POINT_DURATION_SECONDS));
//        ssc.checkpoint(CHECK_POINT_DIR);
        //kafka相关参数，必要！缺了会报错
        Map<String, Object> kafkaParams = new HashMap<>();
        kafkaParams.put("bootstrap.servers", brokers);
        kafkaParams.put("group.id", "weibo_school");
        kafkaParams.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        kafkaParams.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        Collection<String> topicsSet = new HashSet<>(Arrays.asList(topics.split(",")));
        final JavaInputDStream<ConsumerRecord<String, String>> stream =
                KafkaUtils.createDirectStream(
                        ssc,
                        LocationStrategies.PreferConsistent(),
                        ConsumerStrategies.Subscribe(topicsSet, kafkaParams)
                );
        //获得kafka流数据
        JavaDStream<String> lines = stream.map(ConsumerRecord::value);
        //获取微博内容
        JavaDStream<String> contents = lines.map(line-> {
            JSONObject jo = JSON.parseObject(line);
            String content = "";
            if (jo != null) {
               content = jo.getString("content");
            }
            return content;
        }).filter(str-> !StringUtils.isEmpty(str));

        contents.foreachRDD(rdd-> {
//        lines.foreachRDD(rdd-> {
            rdd.foreach(content -> {
                int pos = trie.contains(content);
                if (pos != -1) {
                    schoolCount[pos].addAndGet(1);
                    System.out.println(schoolCount[pos].get());
                }
            });
            MongoClient mongoClient = new MongoClient("172.19.241.121", 27017);
            MongoDatabase mongoDatabase = mongoClient.getDatabase("weibo");
            MongoCollection<Document> collection = mongoDatabase.getCollection("weibo_school");
            for (int i = 0;i<schoolNum;i++){
                collection.findOneAndUpdate(Filters.eq("id",i), Updates.set("num",schoolCount[i].intValue()));
            }
        });
        ssc.start();
        ssc.awaitTermination();
    }
}