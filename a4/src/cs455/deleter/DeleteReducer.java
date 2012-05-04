package cs455.deleter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class DeleteReducer extends Reducer<Text, Text, Text, Text> {

	public static HashMap<String, LinkedList<String>> setSentence(String input) {
		HashMap<String, LinkedList<String>> sentenceBigrams = new HashMap<String, LinkedList<String>>();
		String[] split = input.split(" ");
		for (int i = 0; i < split.length - 1; i++) {
			String first = split[i];
			String second = split[i + 1];
			if (!sentenceBigrams.containsKey(second)) {
				sentenceBigrams.put(second, new LinkedList<String>());
			}
			sentenceBigrams.get(second).add(first);
		}
		return sentenceBigrams;
	}

	@Override
    public void reduce(Text key, Iterable<Text> values, Context context)
			throws IOException, InterruptedException {
		LinkedList<Text> hackIter = new LinkedList<Text>();
		Text kicker = new Text();
		float kickerProb = Float.MAX_VALUE;
		int i = 0;
		HashMap<String, LinkedList<String>> sbigrams = DeleteReducer.setSentence(key.toString());
		HashMap<String, LinkedList<String>> pbigrams = new HashMap<String, LinkedList<String>>();

		for (Text val : values) {
			hackIter.add(new Text(val.toString()));
			String[] split = val.toString().split(" ");
			float prob = Float.parseFloat(split[2]);

			if (prob < kickerProb) {
				kicker = new Text(split[0]);
				kickerProb = prob;
			}

			i++;
			if (! pbigrams.containsKey(split[1])) {
				pbigrams.put(split[1], new LinkedList<String>());
			}

			pbigrams.get(split[1]).add(split[0]);
			Collections.sort(pbigrams.get(split[1]));
		}


		for (String k : sbigrams.keySet()) {
			if (!pbigrams.containsKey(k)) {
				pbigrams.put(k, sbigrams.get(k));
				kickerProb = 0;
				kicker = new Text(sbigrams.get(k).get(0));
			} else {
				Collections.sort(sbigrams.get(k));
				if (pbigrams.get(k).size() != sbigrams.get(k).size()) {
					for (int j = 0; j < sbigrams.get(k).size(); j++) {
						if (!sbigrams.get(k).get(j).equals(pbigrams.get(k).get(j))) {
							pbigrams.get(k).add(j, sbigrams.get(k).get(j));
							kickerProb = 0;
							kicker = new Text(sbigrams.get(k).get(j));
							break;
						}
					}
				}
			}
		}
		String[] sentence = key.toString().split(" ");
		String toRemove = kicker.toString();
		StringBuilder out = new StringBuilder();
		boolean removed = false;
		for (String s : sentence) {
            if (!s.equals(toRemove) || s.equals(toRemove) && removed) {
				out.append(s);
				out.append(" ");
			} else {
				removed = true;
			}
		}
		context.write(new Text(out.toString()), new Text(""));

		/*
		 * i = 0; StringBuilder builder = new StringBuilder(); for (Text val :
		 * hackIter) { String[] split = val.toString().split(" "); if (i !=
		 * kickerIndex) { builder.append(split[0] + " "); } i++; }
		 * context.write(key, new Text(builder.toString()));
		 */
	}
}
