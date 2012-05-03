package cs455.deleter;

import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.HashMap;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.join.*; 
import org.apache.hadoop.mapreduce.lib.input.*;

public class DeleteMapper extends Mapper<LongWritable, Text, Text, Text> {
   private static String sentence;
   private static HashMap<String, LinkedList<String>> sentenceBigrams;
   private IntWritable count;
   private Text second;
   private Text first;

   public static void setSentence(String input) {
      sentenceBigrams = new HashMap<String, LinkedList<String>>();
      sentence = input;
      String[] split = input.split(" ");
      for (int i = 0; i < split.length-1; i++) {
         String first = split[i];
         String second = split[i+1];
         if (!sentenceBigrams.containsKey(second)) {
            sentenceBigrams.put(second, new LinkedList<String>());
         }
         sentenceBigrams.get(second).add(first);
      }
   }

   public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
//      second = key;
      String[] line = value.toString().split("\t");
      second = new Text(line[0]);
      line = value.toString().substring(value.toString().indexOf("\t")).split(";");

      for(String s : line) {
         first = new Text(s.split(",")[0]);
         Float prob = Float.parseFloat(s.split(",")[1]);
         if (sentenceBigrams.containsKey(second.toString())
               && sentenceBigrams.get(second.toString()).contains(first.toString())) {
            context.write(new Text(sentence), new Text(first.toString() + " " + second.toString() + " " + prob));
         }
      }
   }
}
