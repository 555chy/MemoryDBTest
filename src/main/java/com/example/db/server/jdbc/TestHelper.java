package com.example.db.server.jdbc;

import java.util.ArrayList;
import java.util.List;

import com.example.db.entity.Exec;
import com.example.db.entity.Person;
import com.example.db.entity.Score;
import com.example.db.server.db1.ExecService;
import com.example.db.server.jdbc.base.JdbcHelper;
import com.example.db.util.CostUtil;
import com.example.db.util.SpringUtil;
import com.example.db.util.sql.JdbcUtil;
import com.example.db.util.sql.SqlUtil;

public class TestHelper<T> {

	public static final String JDBC_IP = "127.0.0.1";
	// ignite@114.115.160.23 eL2GxunN8bPQN!13 密码改为fastlion
	// public static final String IGNITE_IP = "114.115.160.23";

    /** 统计字段宽度 */
	public static int fieldWidth = 15;
	/** 统计时间格式化（右对齐） */
	public String formatPadR = "%-" + fieldWidth + "s";
	/** 统计时间格式化（左对齐） */
	public static String formatPadL = "%" + fieldWidth + "s";
	public static String numPadL = "%," + fieldWidth + "d";
    public static String timePadL = "%," + (fieldWidth - 2) + "dms";

    /** 是否使用索引 */
	public static final boolean useIndex = true;
	/** 是否显示Sql */
	public static final boolean isShowSql = SqlUtil.isShowSql;
	/** 是否显示中间结果 */
	public static final boolean isShowPart = JdbcUtil.isShowPart;
	/** 将首项替换成行数 */
	public static final boolean replaceFirst = false;

	/** 每多少条打包在一起 */
	public static final int fetchSize = 100;
	/** 限制最多仅返回多少条 */
	public static final int limit = 1000;

	/** 仅首次显示标题栏 */
	public static boolean hasShowTitle = false;
	/** 是否输出中间统计结果 */
	public static boolean isShowPartStat = true;

	public static final String tablePerson = new Person().getTableName();
	public static final String tableScore = new Score().getTableName();

	/** 汇总信息 */
	public StringBuilder summary = new StringBuilder();
	/** 表头名 */
	public List<String> names = new ArrayList<>();
	/** 总耗时 */
	public List<String> costTimes = new ArrayList<>();
	/** 计时器 */
	public CostUtil cost = new CostUtil();
	/** 数据库类型 */
	public String dbtype;
	/** 表名 */
	public String tablename;
	/** 数据行数 */
	public int rows;
	/** 数据列数 */
	public int cols;
	/** 内存总大小 */
	public int memG;
	/** sql语句 */
	public String sql;
	/** 统计数据 */
	public Exec exec;
	/** JDBC控制器 */
	public JdbcHelper<T> controller;

	//SpringBoot中普通类无法通过@Autowired自动注入Service、dao等bean解决方法
	//@Autowired
	public ExecService execService;

	public TestHelper(String dbType, String tablename, int rows, int cols, int memG) {
		this.dbtype = dbType;
		this.tablename = tablename;
		this.rows = rows;
		this.cols = cols;
		this.memG = memG;
		exec = new Exec().setBase(dbtype, tablename, rows, cols, memG);
		execService = SpringUtil.getBean(ExecService.class);
	}

	public List<T> select() {
		return controller.querySync(isShowPart);
	}

	/**
	 * 填充测试数据
	 */
	public void fill() {
		controller.drop();
		controller.create(useIndex);

		cost.begin();
		sql = controller.insert(rows);
		printCost("insert", sql, rows, cost.end());
	}

	/**
	 * 重置打印格式
	 * 
	 * @param fieldWidth 字符串宽度
	 */
	public void resetFormat(int _fieldWidth) {
		hasShowTitle = false;
		fieldWidth = _fieldWidth;
		formatPadR = "%-" + fieldWidth + "s";
		formatPadL = "%" + fieldWidth + "s";
		numPadL = "%," + fieldWidth + "d";
		timePadL = "%," + (fieldWidth - 2) + "dms";
	}

	/**
	 * 打印耗费的时间
	 * 
	 * @param name      sql方法名
	 * @param count     操作数据总行数
	 * @param timestamp 操作耗时
	 */
	public void printCost(String name, String sql, int count, long timestamp) {
		if (isShowPartStat) System.out.println(String.format("%s\t%d\t%dms\n", name, count, timestamp));
		// names.add(String.format(formatPadL, name));
		// costTimes.add(String.format(timePadL, timestamp));
		execService.insert(exec.setInfo(name, sql, timestamp));
	}

	public void printCost(String name, String sql, int count, long timestamp, String rows) {
		if (isShowPartStat) System.out.println(String.format("%s\t%d\t%dms\n", name, count, timestamp));
		// names.add(String.format(formatPadL, name));
		// costTimes.add(String.format(timePadL, timestamp));
		execService.insert(exec.setInfo(name, sql, timestamp, rows));
	}

	/**
	 * 统计数据
	 * 
	 * @param replaceFirst 是否替换首行为数据行数
	 * @param count        数据行数
	 * @param batchCount   批量传输的行数
	 */
	public void statistic(boolean replaceFirst, int count, int batchCount) {
		String str;
		if (!hasShowTitle) {
			System.out.println();
			str = String.format(
					"test 20 columns, update 5 columns, batchCount %s, limit %d, use index %b, sync to disk 60s",
					batchCount, limit, useIndex);
			summary.append(str);
			summary.append("\n");
			if (replaceFirst)
				names.set(0, String.format(formatPadL, "rows"));
			str = String.join("", names);
			summary.append(str);
			summary.append("\n");
			hasShowTitle = true;
		}
		if(replaceFirst) costTimes.set(0, String.format(numPadL, count));
		str = String.join("", costTimes);
		summary.append(str);summary.append("\n");
		System.out.println(summary.toString());
		names.clear();
		costTimes.clear();
	}

	/**
	 * 将数据打印成表格式
	 */
	public void printTable(String dbType, T t) {
		try {
			for (String[] array : SqlUtil.toTable(dbType, t, true)) {
				System.out.println(String.join(",", array) + "\n");
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}

	public void close() {
		if(controller != null) {
			controller.close();
			controller = null;
		}
	}
}
