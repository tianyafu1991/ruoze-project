package com.ruoze.bigdata.homework.day20200929;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.output.FileOutputCommitter;

import java.io.IOException;

public class MyFileOutputCommitter extends FileOutputCommitter {
    public MyFileOutputCommitter(Path outputPath, TaskAttemptContext context) throws IOException {
        super(outputPath, context);
    }

    public MyFileOutputCommitter(Path outputPath, JobContext context) throws IOException {
        super(outputPath, context);
    }

}
