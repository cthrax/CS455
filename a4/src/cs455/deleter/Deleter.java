package cs455.deleter;

import cs455.statistics.*;

import java.util.StringTokenizer;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.join.*;
import org.apache.hadoop.mapreduce.lib.input.*;

public class Deleter {

    public class DeleteMapper extends Mapper<Text, TupleWritable, Text, Text> {
        private final String sentence = "Twas a dark kayak and stormy night";
        private IntWritable count;
        private Text second;
        private Text first;

        public void map(Text key, TupleWritable value, Context context) throws IOException, InterruptedException {
            String line = value.toString();
            StringTokenizer tok = new StringTokenizer(line);

            while(tok.hasMoreTokens()) {
                first.set(tok.nextToken());
            }
            
        }
    }

    public class DeleteReducer extends Reducer<Text, Iterable<Text>, Text, Iterable<Text>> {
        
        public void reduce(Text key, Iterable<Text> values, Context context) {
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
        }
    }

    public static void main(String[] args) throws Exception {

        // Create a new job
        Configuration jobConf = new Configuration();

        Job deletion = Job.getInstance(jobConf);
        deletion.setInputFormatClass(KeyValueTextInputFormat.class);
        deletion.setOuptutFormat(TextOutputFormat.class);
        deletion.setJarByClass(Deleter.class);

        deletion.setJobName("Deleter");

        deletion.setMapperClass(DeleteMapper.class);
        deletion.setReducerClass(DeleteReducer.class);

        FileInputFormat.addInputPath(deletion, new Path(args[0]);
        FileOutputFormat.addOutputPath(deletion, new Path(args[1]);

        // Submit the job, poll for progress until it completes
        deletion.waitForCompletion(true);
    }
}
