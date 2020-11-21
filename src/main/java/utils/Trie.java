package utils;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.bson.Document;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/* @Author: Huaze Shen
        * @Date: 2018-06-06
        * @Description: trie树的实现，支持添加单词，判断字符串的匹配状态
        */
public class Trie {
    // 根节点
    private final TrieNode rootNode;

    // 匹配状态
    private static final int UNMATCHED = -1;
//    private static final int MATCH = 1;

    public Trie() {
        // 根节点初始化为空
        this.rootNode = new TrieNode(null, null);
    }

     /* 添加单词到trie树中
     */
    public void addWord(String word, int match) {
        TrieNode node = rootNode;
        for (int i = 0; i < word.length(); i++) {
            Character character = word.charAt(i);
            node = node.addCharacter(character);
        }
        node.setState(true);
        node.setMatch(match);
    }
     public void addWords(String[] words, int match) {
         for(String word: words) {
            addWord(word, match);
         }
     }


     /* 判断字符串在trie树中的匹配状态
     */
    public int match(String string) {
        if(string == null || string.length() == 0){
            return UNMATCHED;
        }
        TrieNode node = rootNode;
        for (int i = 0; i < string.length(); i++) {
            if(node != null&&node.getNodeMap() != null) {
                node = node.getCharacterNode(string.charAt(i));
            }
            else {
                return UNMATCHED;
            }
        }
        if (node!=null&&node.isWord()) {
            return node.getMatch();
        }
        return UNMATCHED;
    }
     public int contains(String string) {
         if(string == null || string.length() == 0){
             return UNMATCHED;
         }
         for (int i = 0; i < string.length(); i++) {
             if(rootNode.getCharacterNode(string.charAt(i))!=null){
                 int result = has(string.substring(i));
                 if(result!=-1){
                     return result;
                 }
             }
         }
         return UNMATCHED;
     }
     public void init(){
         try {
             String filePath = "hdfs://mycluster/weibo/schoolDic.txt";
             Path path = new Path(filePath);
             Configuration configuration = new Configuration();
             configuration.addResource(new Path("core-site.xml"));
             configuration.addResource(new Path("hdfs-site.xml"));
             FSDataInputStream fsDataInputStream = FileSystem.get(configuration).open(path);
             BufferedReader br = new BufferedReader(new InputStreamReader(fsDataInputStream));
             String str;
             int count = 0;
             while ((str = br.readLine())!=null){
                 this.addWords(str.split(","), count);
                 count++;
             }
             System.out.println("init count 50 = "+count);
             br.close();
         } catch (IOException e){
             e.printStackTrace();
         }
     }
     private int has(String string) {
         if(string == null || string.length() == 0){
             return UNMATCHED;
         }
         TrieNode node = rootNode;
         for (int i = 0; i < string.length(); i++) {
             if(node != null&&node.getNodeMap() != null) {
                 node = node.getCharacterNode(string.charAt(i));
                 if(node!=null&&node.isWord()){
                     return node.getMatch();
                 }
             } else {
                 return UNMATCHED;
             }
         }
         return UNMATCHED;
     }
//     public static void main(String[] args)  {
//        try {
//
//            MongoClient mongoClient = new MongoClient("172.19.241.121", 27017);
//            MongoDatabase mongoDatabase = mongoClient.getDatabase("weibo");
//            MongoCollection<Document> collection = mongoDatabase.getCollection("weibo_school");
//            BufferedReader br = new BufferedReader(new FileReader("D:\\schoolDic.txt"));
//            String str;
//            int id = 0;
//            List<Document> documents = new ArrayList<>();
//            while ((str = br.readLine()) != null) {
//                String name = str.split(",")[0];
//                // 连接到 mongodb 服务
//                Document document = new Document("id",id);
//                document.append("name",name);
//                document.append("num",0);
//                documents.add(document);
//                id++;
//            }
////            collection.deleteMany(Filters.eq("num",0));
//            collection.insertMany(documents);
//            FindIterable<Document> findIterable = collection.find();
//            MongoCursor<Document> mongoCursor = findIterable.iterator();
//            while(mongoCursor.hasNext()){
//                System.out.println(mongoCursor.next());
//            }
//            mongoClient.close();
//        } catch (Exception e){
//            e.printStackTrace();
//        }
//     }
}
