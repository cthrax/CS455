package cs455.deleter;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;

public class Deleter {

    public class DeleteMapper extends Mapper<Text, Tuple<Text,DoubleWritable>, Text, Tuple<Text,DoubleWritable>> {
        private IntWritable count;
        private Text second;
        private Text first;

        public void map(ik key, iv value, Context context) throws IOException, InterruptedException {
            //TODO: Implement DeleteMapper
            String line = value.toString();
            StringTokenizer tok = new StringTokenizer(line);

            while(tok.hasMoreTokens()) {
                first.set(tokenizer.nextToken());
                context.write(word, count);
            }
        }
    }

    public class DeleteReducer extends Reducer<ik, iv, ok, ov> {
        
        public void reduce(ik key, Iterable<iv> values, Context context) {
            //TODO: Implement DeleteReducer
        }
    }

    public static void main(String[] args) {
        // Create a new job
        Job deletion = new Job(new Configuration());
        job.setJarByClass(Deleter.class);

        // Specify job-specific parameters
        job.setJobName("Deleter");

        job.setInputPath(new Path("hdfs:///probabilities"));
        job.setOutputPath(new Path("hdfs:///out"));

        job.setMapperClass(DeleteMapper.class);
        job.setReducerClass(DeleteReducer.class);

        // Submit the job, poll for progress until it completes
        job.waitForCompletion(true);
    }
}
