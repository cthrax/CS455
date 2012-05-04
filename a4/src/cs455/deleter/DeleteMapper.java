package cs455.deleter;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

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
		for (int i = 0; i < split.length - 1; i++) {
			String first = split[i];
			String second = split[i + 1];
			if (!sentenceBigrams.containsKey(second)) {
				sentenceBigrams.put(second, new LinkedList<String>());
			}
			sentenceBigrams.get(second).add(first);
		}
	}

	@Override
    public void map(LongWritable key, Text value, Context context)
			throws IOException, InterruptedException {
		// second = key;
		String[] line = value.toString().split("\t");
		second = new Text(line[0]);
		line = line[1].split(";");

		for (String s : line) {
			String[] bigram = s.split(",");
			first = new Text(bigram[0]);
			Float prob = Float.parseFloat(bigram[1]);
			if (sentenceBigrams.containsKey(second.toString())) {
                if (sentenceBigrams.get(second.toString()).contains(first.toString())) {
					context.write(new Text(sentence), new Text(first.toString() + " " + second.toString() + " " + prob));
				}
			}
		}
	}
}
