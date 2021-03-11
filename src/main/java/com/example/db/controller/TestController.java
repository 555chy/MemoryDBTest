package com.example.db.controller;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.websocket.server.PathParam;

import com.example.db.entity.Exec;
import com.example.db.entity.Person;
import com.example.db.entity.Score;
import com.example.db.server.db1.ExecService;
import com.example.db.server.jdbc.TestPersonHelper;
import com.example.db.server.jdbc.TestScoreHelper;
import com.example.db.util.sql.SqlUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
 
@RestController
public class TestController {
    //final String[] dbTypes = { SqlUtil.DB_IGNITE, SqlUtil.DB_EXASOL };
    //final String[] dbTypes = { SqlUtil.DB_EXASOL };
    final String[] dbTypes = { SqlUtil.DB_POSTGRES };
    
    private TestPersonHelper person;
    private TestScoreHelper score;
    
    @Autowired
    private ExecService userService;

    private String remark;

    public void randomRemark() {
        remark = UUID.randomUUID().toString().substring(0, 8);
    }

    // localhost:8080/mysql/exec/select
    @GetMapping("/{dbType}/exec/select")
    public List<Exec> execSelect(@PathVariable(value = "dbType") String dbType) {
        System.out.println("exec select");
        return userService.selectAll();
    }

    // localhost:8080/mysql/exec/delete
    @GetMapping("/{dbType}/exec/delete")
    public List<Exec> execDelete(@PathVariable(value = "dbType") String dbType) {
        System.out.println("exec delete");
        //userService.delete();
        userService.truncate();
        return userService.selectAll();
    }

    private AtomicBoolean isDeal = new AtomicBoolean(false);

    @GetMapping(value = { "/testall", "/testall/{personRow}/{scoreRow}" })
    public String testall(@PathVariable(value = "personRow", required = false) Integer personRow,
            @PathVariable(value = "scoreRow", required = false) Integer scoreRow) {
        if (personRow == null)
            personRow = 100 * 10000;
        if (scoreRow == null)
            scoreRow = 1000;
        final int fetchSize = 200, fkRange = personRow, pRow = personRow, sRow = scoreRow;
        String str = String.format("person row = %d, score row = %d", personRow, scoreRow);
        if (isDeal.get()) {
            System.out.println("isDealing, " + str);
            return "dealing";
        }
        randomRemark();
        System.out.println(remark + " begin, " + str);
        isDeal.set(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < dbTypes.length; i++) {
                    String dbType = dbTypes[i];
                    personTest(dbType, fetchSize, pRow);
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    scoreJoin(dbType, fetchSize, sRow, fkRange);
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                isDeal.set(false);
                System.out.print(remark + " exec finish, " + str);
                remark = null;
            }
        }).start();
        return str;
    }

    @GetMapping("/{dbType}/person/select")
    public List<Person> personSelect(@PathVariable(value = "dbType") String dbType,
            @PathParam(value = "fetchSize") int fetchSize, @PathParam(value = "rows") int rows) {
        System.out.println("person select : fetchSize=" + fetchSize + ", rows=" + rows);
        if (person == null) {
            person = new TestPersonHelper(dbType, fetchSize, rows, remark);
            List<Person> data = person.select();
            person.close();
            person = null;
            return data;
        }
        return null;
    }

    // localhost:8080/ignite/person/insert?fetchSize=5&rows=20
    @GetMapping("/{dbType}/person/insert")
    public List<Exec> personInsert(@PathVariable(value = "dbType") String dbType,
            @PathParam(value = "fetchSize") int fetchSize, @PathParam(value = "rows") int rows) {
        System.out.println("person insert : fetchSize=" + fetchSize + ", rows=" + rows);
        if (person == null) {
            person = new TestPersonHelper(dbType, fetchSize, rows, remark);
            person.fill();
            person.close();
            person = null;
        }
        return userService.selectAll();
    }

    // localhost:8080/ignite/person/test?fetchSize=5&rows=20
    @GetMapping("/{dbType}/person/test")
    public List<Exec> personTest(@PathVariable(value = "dbType") String dbType,
            @PathParam(value = "fetchSize") int fetchSize, @PathParam(value = "rows") int rows) {
        System.out.println("person test : fetchSize=" + fetchSize + ", rows=" + rows);
        if (person == null) {
            person = new TestPersonHelper(dbType, fetchSize, rows, remark);
            person.fill();
            person.test();
            person.close();
            person = null;
        }
        return userService.selectAll();
    }

    @GetMapping("/{dbType}/score/select")
    public List<Score> scoreSelect(@PathVariable(value = "dbType") String dbType,
            @PathParam(value = "fetchSize") int fetchSize, @PathParam(value = "rows") int rows,
            @PathParam(value = "fkRange") int fkRange) {
        System.out.println("score select : fetchSize=" + fetchSize + ", rows=" + rows);
        if (score == null) {
            score = new TestScoreHelper(dbType, fetchSize, rows, fkRange, remark);
            List<Score> data = score.select();
            score.close();
            score = null;
            return data;
        }
        return null;
    }

    @GetMapping("/{dbType}/score/insert")
    public List<Exec> scoreInsert(@PathVariable(value = "dbType") String dbType,
            @PathParam(value = "fetchSize") int fetchSize, @PathParam(value = "rows") int rows,
            @PathParam(value = "fkRange") int fkRange) {
        System.out.println("score insert : fetchSize=" + fetchSize + ", rows=" + rows + ", fkRange=" + fkRange);
        if (score == null) {
            score = new TestScoreHelper(dbType, fetchSize, rows, fkRange, remark);
            score.fill();
            score.close();
            score = null;
        }
        return userService.selectAll();
    }

    // localhost:8080/ignite/score/test?fetchSize=2&rows=5&fkRange=20
    @GetMapping("/{dbType}/score/test")
    public List<Exec> scoreTest(@PathVariable(value = "dbType") String dbType,
            @PathParam(value = "fetchSize") int fetchSize, @PathParam(value = "rows") int rows,
            @PathParam(value = "fkRange") int fkRange) {
        System.out.println("score test : fetchSize=" + fetchSize + ", rows=" + rows + ", fkRange=" + fkRange);
        if (score == null) {
            score = new TestScoreHelper(dbType, fetchSize, rows, fkRange, remark);
            score.fill();
            score.test();
            score.close();
            score = null;
        }
        return userService.selectAll();
    }

    // localhost:8080/ignite/score/join?fetchSize=2&rows=5&fkRange=20
    @GetMapping("/{dbType}/score/join")
    public List<Exec> scoreJoin(@PathVariable(value = "dbType") String dbType,
            @PathParam(value = "fetchSize") int fetchSize, @PathParam(value = "rows") int rows,
            @PathParam(value = "fkRange") int fkRange) {
        System.out.println("score join : fetchSize=" + fetchSize + ", rows=" + rows + ", fkRange=" + fkRange);
        if (score == null) {
            score = new TestScoreHelper(dbType, fetchSize, rows, fkRange, remark);
            score.fill();
            score.testJoin();
            score.close();
            score = null;
        }
        return userService.selectAll();
    }

}