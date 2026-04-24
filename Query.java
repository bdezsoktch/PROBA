/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package demo;

/**
 *
 * @author Tanar
 */
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.lang.annotation.*;

public class Query<T> {

    private Class<T> clazz;
    private List<Condition> conditions = new ArrayList<>();

    public Query(Class<T> clazz) {
        this.clazz = clazz;
    }

    public Query<T> where(Condition cond) {
        conditions.add(cond);
        return this;
    }

    public List<T> getResultList(Connection conn) throws Exception {

        //String table = clazz.getAnnotation(annotations.Entity.class).table();
        String table = clazz.getAnnotation(Entity.class).table();

        StringBuilder sql = new StringBuilder("SELECT * FROM " + table);

        if (!conditions.isEmpty()) {
            sql.append(" WHERE ");

            for (Condition c : conditions) {
                sql.append(c.column).append(" ").append(c.operator);

                if (!c.operator.equals("IS NULL")) {
                    sql.append(" ?");
                }

                sql.append(" AND ");
            }

            sql.delete(sql.length() - 5, sql.length());
        }

        PreparedStatement ps = conn.prepareStatement(sql.toString());

        int index = 1;

        for (Condition c : conditions) {
            if (!c.operator.equals("IS NULL")) {

                Object val = c.value;

                if (val instanceof LocalDateTime) {
                    val = Timestamp.valueOf((LocalDateTime) val);
                }

                ps.setObject(index++, val);
            }
        }

        ResultSet rs = ps.executeQuery();

        EntityManager em = new EntityManager(conn);

        List<T> result = new ArrayList<>();

        while (rs.next()) {

            // reuse mapping
            T obj = em.find(clazz, rs.getObject(1));
            result.add(obj);
        }

        return result;
    }
}
