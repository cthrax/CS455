package cs455.statistics;

import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class Statistic {

    public static class WordMap extends Mapper<LongWritable, Text, Text, Text> {
        private final static IntWritable one = new IntWritable(1);
        private final Text word = new Text();
        private final Text prevw = new Text();

        @Override
        public void map(final LongWritable key, final Text value, final Context context) throws IOException, InterruptedException {
            if (value == null) {
                return;
            }
            String line = value.toString();
            StringTokenizer tokenizer = new StringTokenizer(line);
            String prev = "";
            while (tokenizer.hasMoreTokens()) {
                if (prev.equals("")) {
                    prev = tokenizer.nextToken();
                    if (prev.matches("[^A-Za-z']")) {
                        continue;
                    }
                    prevw.set(prev);
                    continue;
                }

                String token = tokenizer.nextToken();
                word.set(token);
                context.write(word, new Text(prevw.toString() + " 1"));
                prevw.set(word);
            }
        }
    }

    public static class WordCountReduce extends Reducer<Text, Text, Text, HashPrintable> {

        @Override
        public void reduce(final Text key, final Iterable<Text> values, final Context context) throws IOException, InterruptedException {
            HashPrintable prev = new HashPrintable();
            for (Text val : values) {
                String[] split = val.toString().split(" ");
                String prevKey = split[0];
                int count = Integer.parseInt(split[1]);

                if (!prev.containsKey(prevKey)) {
                    prev.put(prevKey, 0);
                }
                prev.put(prevKey, prev.get(prevKey) + count);
            }
            context.write(key, prev);
        }
    }

    public static class HashPrintable extends HashMap<String, Integer> {
        /**
         *
         */
        private static final long serialVersionUID = 1L;

         public HashPrintable() {super(); }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("{");

            for (String key : keySet()) {
                builder.append("(");
                builder.append(key);
                builder.append(",");
                builder.append(get(key));
                builder.append(")");
            }
            builder.append("}");
            return builder.toString();
        }
    }

    public static void main(final String[] args) throws Exception {
        Configuration conf = new Configuration();

        Job job = new Job(conf);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(HashPrintable.class);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);

        job.setMapperClass(WordMap.class);
        job.setReducerClass(WordCountReduce.class);

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));


        job.waitForCompletion(true);
    }

}
