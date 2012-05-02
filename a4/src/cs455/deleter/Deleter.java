package cs455.deleter;

import java.util.LinkedList;
import java.util.StringTokenizer;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.join.*;

public class Deleter {

    public class DeleteMapper extends Mapper<Text, TupleWritable, Text, TupleWritable> {
        private IntWritable count;
        private Text second;
        private Text first;

        public void map(Text key, TupleWritable value, Context context) throws IOException, InterruptedException {
            //TODO: Implement DeleteMapper
//            String line = value.toString();
//            StringTokenizer tok = new StringTokenizer(line);
//
//            while(tok.hasMoreTokens()) {
//                first.set(tok.nextToken());
//                context.write(word, count);
//            }
        }
    }

    public class DeleteReducer extends Reducer<Text, Iterable<Text>, Text, Iterable<Text>> {
        
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
		    LinkedList<Text> out = new LinkedList<Text>();
		    out.add(new Text(split[0]));
		    out.add(new Text(split[1]));
		    context.write(key, out);
		}
		i++;
	    }
        }
    }

    public static void main(String[] args) throws Exception {
        String input = args[0];
        for(int i = 1; i < args.length; i++) {
            input += " " + args[i];
        }
        Text sentence = new Text(input);
        // Create a new job
        Configuration jobConf = new Configuration();

        jobConf.set("sentence", input);
        jobConf.addResource(new Path("/probabilites/foo"));

        Job deletion = Job.getInstance(jobConf);
        deletion.setInputFormatClass(CompositeInputFormat.class);
//        deletion.setOuptutFormat(); //TODO: These two lines
//        deletion.setOutputValueFormat();
        deletion.setJarByClass(Deleter.class);

        // Specify job-specific parameters
        deletion.setJobName("Deleter");

        deletion.setMapperClass(DeleteMapper.class);
        deletion.setReducerClass(DeleteReducer.class);

        // Submit the job, poll for progress until it completes
        deletion.waitForCompletion(true);
    }
}
