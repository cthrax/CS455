package cs455.deleter;

import cs455.deleter.DeleteMapper;
import cs455.deleter.DeleteReducer;
import cs455.statistics.*;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.join.*;
import org.apache.hadoop.mapreduce.lib.input.*;
import org.apache.hadoop.mapreduce.lib.output.*;

public class Deleter {


    public static void main(String[] args) throws Exception {

        DeleteMapper.setSentence("Twas a dark rock and stormy night");
        // Create a new job
        Configuration jobConf = new Configuration();

        Job deletion = Job.getInstance(jobConf);
        deletion.setInputFormatClass(TextInputFormat.class);
        deletion.setOutputFormatClass(TextOutputFormat.class);
        deletion.setOutputKeyClass(Text.class);
        deletion.setOutputValueClass(Text.class);
        deletion.setJarByClass(Deleter.class);

        deletion.setJobName("Deleter");

        deletion.setMapperClass(DeleteMapper.class);
        deletion.setReducerClass(DeleteReducer.class);

        FileInputFormat.addInputPath(deletion, new Path(args[0]));
        FileOutputFormat.setOutputPath(deletion, new Path(args[1]));

        // Submit the job, poll for progress until it completes
        deletion.waitForCompletion(true);
    }
}
