package cs455.deleter;

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

    public class DeleteReducer extends Reducer<Text, TupleWritable, Text, TupleWritable> {
        
        public void reduce(Text key, Iterable<TupleWritable> values, Context context) {
            //TODO: Implement DeleteReducer
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
