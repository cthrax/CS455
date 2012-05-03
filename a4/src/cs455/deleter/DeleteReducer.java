package cs455.deleter;

import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.HashMap;
import java.io.IOException;
//import java.util.StringBuilder

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.join.*; 
import org.apache.hadoop.mapreduce.lib.input.*;

public class DeleteReducer extends Reducer<Text, Text, Text, Text> {

   public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
	LinkedList<Text> hackIter = new LinkedList<Text>();
	int kickerIndex = -1;
	float kickerProb = Float.MAX_VALUE;
	int i = 0;
     	for (Text val : values) {
	    hackIter.add(new Text(val.toString()));
            String[] split = val.toString().split(" ");
            float prob = Float.parseFloat(split[2]);
            if (prob < kickerProb) {
            	kickerIndex = i;
            	kickerProb = prob; 
            }
            i++;
      	}
      	String[] sentence = key.toString().split(" ");;
	String toRemove = hackIter.get(kickerIndex).toString().split(" ")[0];
	StringBuilder out = new StringBuilder();
	for (String s : sentence) {
	    if (!s.equals(toRemove)) {
		out.append(s);
		out.append(" ");
	    }
	}
	context.write(new Text(out.toString()), new Text(""));
      /*
      i = 0;
      StringBuilder builder = new StringBuilder();
      for (Text val : hackIter) {
         String[] split = val.toString().split(" ");
         if (i != kickerIndex) {
            builder.append(split[0] + " ");
         }
         i++;
      }
      context.write(key, new Text(builder.toString()));
      */
   }
}

