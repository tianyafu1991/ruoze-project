package com.ruozedata.bigdata.hive;

import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDTF;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.StructObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;

import java.util.ArrayList;
import java.util.List;

// a,a,a,a,a,b,b,b
public class RuozedataSplitUDTF extends GenericUDTF {

    List<String> list = new ArrayList<>();

    @Override
    public StructObjectInspector initialize(StructObjectInspector argOIs) throws UDFArgumentException {
        List<String> structFieldNames = new ArrayList<>();
        List<ObjectInspector> structFieldObjectInspectors = new ArrayList<>();

        structFieldNames.add("word");
        structFieldObjectInspectors.add(PrimitiveObjectInspectorFactory.javaStringObjectInspector);

        return ObjectInspectorFactory.getStandardStructObjectInspector(structFieldNames,structFieldObjectInspectors);
    }

    @Override
    public void process(Object[] args) throws HiveException {
        String line = args[0].toString();
        String split = args[1].toString();
        String[] fields = line.split(split);

        for(String field : fields) {
            list.clear();
            list.add(field);
            forward(list);
        }

    }

    @Override
    public void close() throws HiveException {

    }
}
