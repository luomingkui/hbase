package com.luomk;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.mapreduce.Job;
import java.io.IOException;

public class HbaseDriver {

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {

        //1.获取Hbase的conf&封装job
        Configuration configuration = new Configuration();
        Configuration conf = HBaseConfiguration.create(configuration);

        Job job = Job.getInstance(conf);

        //2.设置主类
        job.setJarByClass(HbaseDriver.class);

        Scan scan = new Scan();

        //3.设置Mapper类
        TableMapReduceUtil.initTableMapperJob(Bytes.toBytes(args[0]), scan, HbaseMapper.class, ImmutableBytesWritable.class, Put.class, job);

        //4.设置reducer个数
        // job.setNumReduceTasks(1);

        //5.设置Reducer
        TableMapReduceUtil.initTableReducerJob(args[1], HbaseReducer.class, job);

        //提交
        boolean result = job.waitForCompletion(true);
        System.exit(result ? 0 : 1);
    }
}
