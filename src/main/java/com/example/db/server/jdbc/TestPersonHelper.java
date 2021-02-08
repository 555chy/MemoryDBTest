package com.example.db.server.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.example.db.callback.DataCallback2;
import com.example.db.entity.Person;
import com.example.db.server.jdbc.base.PersonHelper;

public class TestPersonHelper extends TestHelper<Person> {

	/**
	 * 初始化
	 * @param fetchSize		每多少条打包在一起
	 * @param rows			 插入行数
	 * @param fkRange	   外键范围
	 */
	public TestPersonHelper(String dbType, int fetchSize, int rows, String remark) {
		super(dbType, tablePerson, rows, 20, 25, remark);
        controller = new PersonHelper(dbType, tablename, fetchSize, JDBC_IP);
	}

	/**
	 * 常规测试（千万行下，增删改查）
	 */
	public void test() {
		resetFormat(20);

		//筛选
		String where = "height > 1.5";
		//排序
		String orderBy = "age";
		//分组
		String groupBy = "province";
		//计算
		String calcItem = "wages";
		//不重复的
		String distinctCol = "age";
		//时间戳字段
		String timeCol = "birthday";
		//更新总条数
		int updateCount = limit;

		//ignite不支持存储过程 
		// cost.begin();
		// sql = controller.createProducedure();
		// printCost("createProducedure", sql, rows, cost.end());

		// cost.begin();
		// sql = controller.insert(rows);
		// printCost("insert", sql, rows, cost.end());
		
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
		sql = controller.update(updateCount, new String[]{"male", "name", "age", "phone", "province"}, new DataCallback2<PreparedStatement, Integer>(){

			@Override
			public void onData(PreparedStatement stat, Integer i) {
				Person person = (Person) controller.random(true);
				try {
					stat.setBoolean(1, person.isMale());
					stat.setString(2, person.getName());
					stat.setShort(3, person.getAge());
					stat.setString(4, person.getPhone());
					stat.setString(5, person.getProvince());
					stat.setLong(6, person.getId());
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
		sql = controller.countYearMonth(isShowPart, timeCol);
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
		// sql = controller.callProducedure(rows, batchCount);
		// printCost("callProducedure", sql, rows, cost.end());

		// cost.begin();
		// sql = controller.drop();
		// printCost("drop", sql, rows, cost.end());

		// statistic(true, rows, fetchSize);
	}
}
