package com.luomk;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import java.io.IOException;
import java.util.ArrayList;

public class HbaseDemo {

    public static Configuration conf;
    static private Admin admin;
    static private Connection connection = null;

    static {
        //使用HBaseConfiguration的单例方法实例化
        conf = HBaseConfiguration.create();
        conf.set("hbase.zookeeper.quorum", "10.211.55.102");
        conf.set("hbase.zookeeper.property.clientPort", "2181");

        try {
            //获取连接
            connection = ConnectionFactory.createConnection(conf);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            admin = connection.getAdmin();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void close() throws IOException {
        connection.close();
        admin.close();
    }

    /**
     * 1.判断表名是否存在
     *
     * @param tableName
     * @return
     */
    public static boolean isTableExist(String tableName) throws IOException {
        boolean b = admin.tableExists(TableName.valueOf(tableName));
        System.out.println(b);
        return b;
    }


    /**
     * 2.创建表
     */
    public static void createTable(String tableName, String... columeFamily) throws IOException {
        if (isTableExist(tableName)) {
            System.out.println("表" + tableName + "已经存在!");
        } else {
            HTableDescriptor hTableDescriptor = new HTableDescriptor(TableName.valueOf(tableName));
            for (String cf : columeFamily) {
                hTableDescriptor.addFamily(new HColumnDescriptor(cf));
            }
            admin.createTable(hTableDescriptor);
        }
    }


    /**
     * 3.删除表
     */
    public static void delTable(String tableName) throws IOException {
        if (isTableExist(tableName)) {
            TableName name = TableName.valueOf(tableName);
            admin.disableTable(TableName.valueOf(tableName));
            admin.deleteTable(TableName.valueOf(tableName));
        } else {
            System.out.println("表" + tableName + "已经存在");
        }
    }


    /**
     * 4.插入一条数据
     */
    public static void createData(String tableName, String rowKey, String cf, String cn, String value) throws IOException {
        //获取表的对象
        HTable hTable = new HTable(TableName.valueOf(tableName), connection);
        //获取put对象
        Put put = new Put(Bytes.toBytes(rowKey));
        //向put中插入数据
        put.addColumn(Bytes.toBytes(cf), Bytes.toBytes(cn), Bytes.toBytes(value));
        //向表中插入数据
        hTable.put(put);
        //关闭资源
        hTable.close();
    }


    /**
     * 5.插入多条数据
     */
    public static void createsTable(String tableName, String rowKey, String cf, String[] cn, String[] value) throws IOException {
        //获取表对象
        Table table = connection.getTable(TableName.valueOf(tableName));
        //获取put对象
        Put put = new Put(Bytes.toBytes(rowKey));
        //插入多条数据
        for (int i = 0; i < cn.length; i++) {
            put.addColumn(Bytes.toBytes(cf), Bytes.toBytes(cn[i]), Bytes.toBytes(value[i]));
        }
        table.put(put);
        table.close();

    }


    /**
     * 6.获取一条数据
     */
    public static void getData(String tableName,String rowkey) throws IOException {
        //获取表的对象
        Table table = connection.getTable(TableName.valueOf(tableName));
        //获取get对象
       Get get =  new Get(Bytes.toBytes(rowkey));
       //设置最大的版本数量
        get.setMaxVersions(2);
        //执行获取数据的操作
        Result result = table.get(get);
        Cell[] cells = result.rawCells();
        for (Cell cell:cells) {
            System.out.println("rowKey:"+rowkey+";"+
                    "ColumnFamily:"+Bytes.toString(CellUtil.cloneFamily(cell))+";"+
                    "ColumnName:"+Bytes.toString(CellUtil.cloneQualifier(cell))+";"+
                    "values:"+Bytes.toString(CellUtil.cloneValue(cell))+";"+
                    "TS:"+cell.getTimestamp());
        }
    }


    /**
     * 7.获取指定列簇的值
     */
    private static void getRowByCF(String tableName, String rowkey, String cf, String cn) throws IOException {
        //获取表对象
        Table hTable = connection.getTable(TableName.valueOf(tableName));
        //获取get对象
        Get get = new Get(Bytes.toBytes(rowkey));
        get.addColumn(Bytes.toBytes(cf), Bytes.toBytes(cn));
        //执行获取数据操作
        Result result = hTable.get(get);
        Cell[] cells = result.rawCells();

        for (Cell cell : cells) {
            System.out.println(
                    "RowKey:" + rowkey +
                            ",ColumnFamily:" + Bytes.toString(CellUtil.cloneFamily(cell)) +
                            ",ColumnName:" + Bytes.toString(CellUtil.cloneQualifier(cell)) +
                            ",Value:" + Bytes.toString(CellUtil.cloneValue(cell)) +
                            ",TS:" + cell.getTimestamp());
        }
    }

    /**
     * 8.扫描一张表
     */
    public static void scanTable(String tableName) throws IOException {
        //获取表对象
        Table hTable = connection.getTable(TableName.valueOf(tableName));

        Scan scan = new Scan();
        ResultScanner scanner = hTable.getScanner(scan);

        for (Result result : scanner) {
            Cell[] cells = result.rawCells();
            for (Cell cell : cells) {
                System.out.println(
                        "RowKey:" + Bytes.toString(CellUtil.cloneRow(cell)) +
                                ",ColumnFamily:" + Bytes.toString(CellUtil.cloneFamily(cell)) +
                                ",ColumnName:" + Bytes.toString(CellUtil.cloneQualifier(cell)) +
                                ",Value:" + Bytes.toString(CellUtil.cloneValue(cell)) +
                                ",TS:" + cell.getTimestamp());
            }
        }
    }

    /**
     * 9.删除一条数据
     */
    private static void delTableData(String tableName,String rowKey) throws IOException {
        //获取表对象
        Table hTable = connection.getTable(TableName.valueOf(tableName));
        //封装删除对象
        Delete delete = new Delete(Bytes.toBytes(rowKey));
        hTable.delete(delete);
        hTable.close();
    }


    /**
     * 10.删除多条是数据
     */
    private static void deleteDatas(String tableName, String... rows) throws IOException {

        //获取表对象
        Table table = connection.getTable(TableName.valueOf(tableName));

        ArrayList deletes = new ArrayList();

        for (String row : rows) {
            Delete delete = new Delete(Bytes.toBytes(row));
            deletes.add(delete);
        }
        table.delete(deletes);
        table.close();
    }


    /**
     * 测试
     *
     * @param args
     */
    public static void main(String[] args) throws IOException {
        //1.判断表名是否存在
        isTableExist("student");
        //2.创建表
        //createTable("test","info");
        //3.删除表
        //delTable("test");
        //4.插入数据
        //createData("staff", "1001", "info", "age1", "18");
        //createData("staff", "1001", "info", "age2", "20");
        //5.插入多条数据
        //createsTable("staff", "1005", "info", new String[]{"name", "sex","address","like"}, new String[]{"sunxueni", "nv","jindong","lovewo"});
        //createsTable("staff", "1004", "info", new String[]{"name", "sex"}, new String[]{"luomk", "nan"});
        //6.获取一条数据
        //getData("staff","1002");
        //7.获取某一行的列数据
        // getRowByCF("staff","1002","info","name");
        //8.扫描一张表
        // scanTable("staff");
        //9.删除一条数据
        // delTableData("staff","1003");
        //10。删除多条数据
        // deleteDatas("staff","1001","1002");
    }
}
