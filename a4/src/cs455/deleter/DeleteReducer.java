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

public class DeleteReducer extends Reducer<Text, Text, Text, Text> {

   public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
      int kickerIndex = -1;
      float kickerProb = Float.MAX_VALUE;
      int i = 0;
      for (Text val : values) {
         String[] split = val.toString().split(" ");
         float prob = Float.parseFloat(split[2]);
         if (prob < kickerProb) {
            kickerIndex = i;
            kickerProb = prob; 
         }
         i++;
      }
      i = 0;
      for (Text val : values) {
         String[] split = val.toString().split(" ");
         if (i != kickerIndex) {
            String out = split[0];
            out += split[1];
            context.write(key, new Text(out));
         }
         i++;
      }
   }
}

