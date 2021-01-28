package com.example.db.server.jdbc.base;

import java.sql.PreparedStatement;

import com.example.db.entity.Score;
import com.example.db.util.ArrayUtil;
import com.example.db.util.sql.JdbcUtil;

public class ScoreHelper extends JdbcHelper<Score> {
  
    private int[] personIds;

    /**
     * 创建Score表数据
     * @param tableName  表名
     * @param ip               jdbc地址
     * @param fetchSize    多少条命令成一组进行发送
     * @param n                生成多少行数据
     * @param range         随机外键的范围
     */
    public ScoreHelper(String dbType, String tableName, int fetchSize, int n, int range, String ip) {
        super(dbType, tableName, fetchSize, ip);
        personIds = ArrayUtil.selectN2(range, n);
    }

    @Override
    public Score random(boolean resetId) {
        if (resetId) idGen.set(0); 
        int fk = personIds == null ? 0 : personIds[ (int) idGen.get() % personIds.length];
        Score score = new Score(idGen);
        score.personId = fk;
        return score;
    }

    @Override
    public Score newInstance() {
        return new Score();
    }

    @Override
    public void insert(PreparedStatement stat, int i) {
        Score score = random(false);
        JdbcUtil.set(stat, score, dbType);
    }

}
