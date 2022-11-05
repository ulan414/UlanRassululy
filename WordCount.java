import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

public class WordCount {

    public static class IPBlockMapper extends Mapper<Object, Text, Text, IntWritable>
    {

        private final static IntWritable one = new IntWritable(1);
        private Text add = new Text();

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException
        {
            StringTokenizer st = new StringTokenizer(value.toString(), ".");
            if(st.countTokens() == 4)
            {
                String[] nums = new String[4];
                int count = 0;
                while(st.hasMoreElements())
                {
                    nums[count++] = st.nextToken();
                }
                int n1 = Integer.parseInt(nums[0]);
                int n2 = Integer.parseInt(nums[1]);
                int n3 = Integer.parseInt(nums[2]);
                int n4 = Integer.parseInt(nums[3]);
                // IP Address block 2.72.0.0  to 2.79.255.255
                if(n1 == 2 && n2 >= 72 && n2 <= 79 && n3 >= 0 && n3 <= 255 && n4 >=0 && n4 <= 255)
                {
                    add.set(value);
                    context.write(add, one);
                }

            }
        }
    }

    public static class IPCountReducer extends Reducer<Text,IntWritable,Text,IntWritable>
    {
        private IntWritable count = new IntWritable();

        public void reduce(Text key, Iterable<IntWritable> values, Context context)
            throws IOException, InterruptedException {
            int sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }
            count.set(sum);
            context.write(key, count);
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration c = new Configuration();
        String[] rargs = new GenericOptionsParser(c, args).getRemainingArgs();
        if (rargs.length != 2) {
            System.err.println("Usage: ipcount <in> <out>");
            System.exit(2);
        }
        Job job = new Job(c, "ip count");
        job.setJarByClass(WordCount.class);
        job.setMapperClass(IPBlockMapper.class);
        job.setCombinerClass(IPCountReducer.class);
        job.setReducerClass(IPCountReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        FileInputFormat.addInputPath(job, new Path(rargs[0]));
        FileOutputFormat.setOutputPath(job, new Path(rargs[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}