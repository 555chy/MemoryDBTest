package com.example.db.server.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.example.db.callback.DataCallback2;
import com.example.db.entity.Exec;
import com.example.db.entity.Score;
import com.example.db.server.jdbc.base.ScoreHelper;
import com.example.db.util.sql.SqlUtil;

public class TestScoreHelper extends TestHelper<Score> {

	private int fkRange;
	/**
	 * 初始化
	 * @param fetchSize		每多少条打包在一起
	 * @param rows			 插入行数
	 * @param fkRange	   外键范围
	 */
    public TestScoreHelper(String dbType, int fetchSize, int rows, int fkRange, String remark) {
		super(dbType, tableScore, rows, 6, 20, remark);
		this.fkRange = fkRange;
        controller = new ScoreHelper(dbType, tablename, fetchSize, rows, fkRange, JDBC_IP);
    }

    /**
     * 常规测试（千万行下，增删改查）
     */
    public void test() {
		resetFormat(20);
	
		//筛选
		String where = "math > 75";
		//排序
		String orderBy = "chinese";
		//分组
		String groupBy = "classId";
		//计算
		String calcItem = "english";
		//不重复的
		String distinctCol = "classId";
		//更新总条数
		int updateCount = limit;

		//ignite不支持存储过程 
		// cost.begin();
		// sql = controller.createProducedure();
		// printCost("createProducedure", sql, rows, cost.end());
		
		// cost.begin();
		// sql = controller.query(false);
		// printCost("query", sql, rows, cost.end());

		cost.begin();
		sql = controller.query(false, where, orderBy, 0, limit);
		printCost("query" + limit, sql, rows, cost.end());

		cost.begin();
		sql = controller.queryDistinct(false, where, 0, limit, distinctCol);
		printCost("distinct", sql, rows, cost.end());
		
		cost.begin();
		sql = controller.update(updateCount, new String[]{"englishName", "chinese", "math", "english", "classId"}, new DataCallback2<PreparedStatement, Integer>(){

			@Override
			public void onData(PreparedStatement stat, Integer i) {
				Score obj = (Score) controller.random(true);
				try {
					stat.setString(1, obj.englishName);
					stat.setShort(2, obj.chinese);
					stat.setShort(3, obj.math);
					stat.setShort(4, obj.english);
					stat.setByte(5, obj.classId);
					stat.setLong(6, obj.id);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		});
		printCost("update"+limit, sql, rows, cost.end());

		cost.begin();
		sql = controller.group(isShowPart, groupBy);
		printCost("group", sql, rows, cost.end());

		cost.begin();
		sql = controller.group(isShowPart, groupBy, where);
		printCost("groupWhere", sql, rows, cost.end());
		
		cost.begin();
		sql = controller.count(isShowPart);
		printCost("count", sql, rows, cost.end());

		cost.begin();
		sql = controller.min(isShowPart, calcItem);
		printCost("min", sql, rows, cost.end());

		cost.begin();
		sql = controller.max(isShowPart, calcItem);
		printCost("max", sql, rows, cost.end());

		cost.begin();
		sql = controller.average(isShowPart, calcItem);
		printCost("average", sql, rows, cost.end());

		cost.begin();
		sql = controller.sum(isShowPart, calcItem);
		printCost("sum", sql, rows, cost.end());

		cost.begin();
		sql = controller.sum(isShowPart, calcItem, where);
		printCost("sumWhere", sql, rows, cost.end());

		cost.begin();
		sql = controller.order(false, orderBy);
		printCost("order", sql, rows, cost.end());

		cost.begin();
		sql = controller.order(false, orderBy, where);
		printCost("orderWhere", sql, rows, cost.end());

		cost.begin();
		sql = controller.order(false, orderBy, where, 0, limit);
		printCost("order"+limit, sql, rows, cost.end());

		// cost.begin();
		// sql = controller.delete();
		// printCost("delete", sql, rows, cost.end());

		// cost.begin();
		// sql = controller.insert(rows);
		// printCost("insert", sql, rows, cost.end());

		// cost.begin();
		// sql = controller.callProducedure(rows, batchrows);
		// printCost("callProducedure", sql, rows, cost.end());

		// statistic(true, rows, fetchSize);
    }

    /**
	 * 测试两表连接（左边小表，右边大表）
	 */
	public void testJoin() {
		resetFormat(20);

		String tableA = tableScore;
		String tableB = tablePerson;
		int rowA = rows;
		int rowB = fkRange;
		String rowStr = Exec.getStrRow(rowA, rowB);
        
		// String[] columnA = new String[]{"id", "name", "age"};
		// String[] columnB = new String[]{"englishName", "scoreTotal"};
		String[] columnA = null;
		String[] columnB = null;

		// String joinAddition = " ORDER BY Person.age";

		// controller.drop();
		// sql = controller.create(useIndex);
		// controller.insert(rows);

		cost.begin();
		sql = controller.join(SqlUtil.SQL_INNER_JOIN, tableA, tableB, columnA, columnB);
		printCost("innerJoinAB", sql, rows, cost.end(), rowStr);

		cost.begin();
		sql =controller.join(SqlUtil.SQL_INNER_JOIN, tableA, tableB, columnA, columnB, null, 0, limit);
		printCost("innerJoinAB"+limit, sql, rows, cost.end(), rowStr);
		
		// cost.begin();
		// sql = controller.join(SqlUtil.SQL_INNER_JOIN, tableA, tableB, columnA, columnB, null, 0, limit, joinAddition);
		// printCost("innerJoinAB"+limit+"Order", sql, rows, cost.end(), rowStr);

		cost.begin();
		sql = controller.join(SqlUtil.SQL_LEFT_JOIN, tableA, tableB, columnB, columnA);
		printCost("leftJoinAB", sql, rows, cost.end(), rowStr);

		cost.begin();
		sql = controller.join(SqlUtil.SQL_LEFT_JOIN, tableA, tableB, columnB, columnA, null, 0, limit);
		printCost("leftJoinAB"+limit, sql, rows, cost.end(), rowStr);

		// cost.begin();
		// sql = controller.join(SqlUtil.SQL_LEFT_JOIN, tableA, tableB, columnA, columnB, null, 0, limit, joinAddition);
		// printCost("leftjoinAB"+limit+"Order", sql, rows, cost.end(), rowStr);

		// cost.begin();
		// sql = controller.join(SqlUtil.SQL_RIGHT_JOIN, tableA, tableB, columnA, columnB);
		// printCost("rightJoin", sql, rows, cost.end(), rowStr);

		// cost.begin();
		// sql = controller.join(SqlUtil.SQL_RIGHT_JOIN, tableA, tableB, columnA, columnB, null, 0, limit);
		// printCost("right"+limit, sql, rows, cost.end(), rowStr);

		// cost.begin();
		// sql = controller.join(SqlUtil.SQL_RIGHT_JOIN, tableA, tableB, columnA, columnB, null, 0, limit, joinAddition);
		// printCost("right"+limit+"Order", sql, rows, cost.end(), rowStr);

		// cost.begin();
		// sql = controller.join(SqlUtil.SQL_OUTER_JOIN, tableA, tableB, columnA, columnB);
		// printCost("outerJoin", rows, cost.end(), rowStr);

		// cost.begin();
		// sql = controller.join(SqlUtil.SQL_OUTER_JOIN, tableA, tableB, columnA, columnB, null, 0, limit);
		// printCost("outerJoin"+limit, sql, rows, cost.end(), rowStr);

		// cost.begin();
		// sql = controller.join(SqlUtil.SQL_OUTER_JOIN, tableA, tableB, columnA, columnB, null, 0, limit, joinAddition);
		// printCost("outerJoin"+limit+"Order", sql, rows, cost.end(), rowStr);

		// statistic(replaceFirst, rows, fetchSize);
	}
}
