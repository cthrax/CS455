package cs455.deleter;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class Deleter {


    public static void main(String[] args) throws Exception {

        if (args.length <= 2) {
            DeleteMapper.setSentence("It was a dark rock and stormy night");
        } else {
            DeleteMapper.setSentence(args[2]);
        }
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
