/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package demo;

/**
 *
 * @author Tanar
 */
/*import annotations.*;*/

import java.lang.reflect.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class EntityManager {

    private Connection conn;

    public EntityManager(Connection conn) {
        this.conn = conn;
    }

    public void save(Object entity) throws Exception {

        Class<?> clazz = entity.getClass();
        String table = clazz.getAnnotation(Entity.class).table();
        StringBuilder cols = new StringBuilder();
        StringBuilder vals = new StringBuilder();

        for (java.lang.reflect.Field f : clazz.getDeclaredFields()) {
            if (f.isAnnotationPresent(Column.class)) {
                cols.append(f.getAnnotation(Column.class).name()).append(",");
                vals.append("?,");
            }
        }

        cols.deleteCharAt(cols.length() - 1);
        vals.deleteCharAt(vals.length() - 1);

        String sql = "INSERT INTO " + table + " (" + cols + ") VALUES (" + vals + ")";
        PreparedStatement ps = conn.prepareStatement(sql);

        int i = 1;

        for (java.lang.reflect.Field f : clazz.getDeclaredFields()) {
            if (f.isAnnotationPresent(Column.class)) {
                f.setAccessible(true);
                Object val = f.get(entity);

                if (val instanceof LocalDateTime) {
                    val = Timestamp.valueOf((LocalDateTime) val);
                }

                ps.setObject(i++, val);
            }
        }

        ps.executeUpdate();
    }

    public <T> T find(Class<T> clazz, Object id) throws Exception {

        String table = clazz.getAnnotation(Entity.class).table();

        java.lang.reflect.Field idField = null;
        String idCol = null;

        for (java.lang.reflect.Field f : clazz.getDeclaredFields()) {
            if (f.isAnnotationPresent(Id.class)) {
                idField = f;
                idCol = f.getAnnotation(Column.class).name();
            }
        }

        String sql = "SELECT * FROM " + table + " WHERE " + idCol + "=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setObject(1, id);

        ResultSet rs = ps.executeQuery();
        if (!rs.next()) return null;

        T obj = clazz.getDeclaredConstructor().newInstance();

        for (java.lang.reflect.Field f : clazz.getDeclaredFields()) {
            if (f.isAnnotationPresent(Column.class)) {

                f.setAccessible(true);
                Object val = rs.getObject(f.getAnnotation(Column.class).name());

                /*if (f.getType().equals(LocalDateTime.class) && val != null) {
                    val = ((Timestamp) val).toLocalDateTime();
                }*/
                if (f.getType().equals(LocalDateTime.class) && val != null) {
                    if (val instanceof Timestamp) {
                        val = ((Timestamp) val).toLocalDateTime();
                    } else if (val instanceof LocalDateTime) {
                        // már jó, nem kell semmit csinálni
                    } else {
                        throw new RuntimeException("Unsupported type: " + val.getClass());
                    }
                }
                f.set(obj, val);
            }
        }

        // OneToMany
        for (java.lang.reflect.Field f : clazz.getDeclaredFields()) {
            if (f.isAnnotationPresent(OneToMany.class)) {

                f.setAccessible(true);
                OneToMany otm = f.getAnnotation(OneToMany.class);

                Class<?> childType = (Class<?>) ((ParameterizedType) f.getGenericType())
                        .getActualTypeArguments()[0];

                List<?> list = findAllByField(childType, otm.mappedBy(), id);

                f.set(obj, list);
            }
        }

        return obj;
    }

    public void update(Object entity) throws Exception {

        Class<?> clazz = entity.getClass();
        String table = clazz.getAnnotation(Entity.class).table();

        StringBuilder set = new StringBuilder();
        Object idVal = null;
        String idCol = null;

        for (java.lang.reflect.Field f : clazz.getDeclaredFields()) {

            f.setAccessible(true);

            if (f.isAnnotationPresent(Id.class)) {
                idVal = f.get(entity);
                idCol = f.getAnnotation(Column.class).name();
                continue;
            }

            if (f.isAnnotationPresent(Column.class)) {
                set.append(f.getAnnotation(Column.class).name()).append("=?,");
            }
        }

        set.deleteCharAt(set.length() - 1);

        String sql = "UPDATE " + table + " SET " + set + " WHERE " + idCol + "=?";
        PreparedStatement ps = conn.prepareStatement(sql);

        int i = 1;

        for (java.lang.reflect.Field f : clazz.getDeclaredFields()) {

            if (f.isAnnotationPresent(Id.class)) continue;

            if (f.isAnnotationPresent(Column.class)) {

                f.setAccessible(true);
                Object val = f.get(entity);

                if (val instanceof LocalDateTime) {
                    val = Timestamp.valueOf((LocalDateTime) val);
                }

                ps.setObject(i++, val);
            }
        }

        ps.setObject(i, idVal);
        ps.executeUpdate();
    }

    public <T> List<T> findAllByField(Class<T> clazz, String field, Object value) throws Exception {

        String table = clazz.getAnnotation(Entity.class).table();

        String col = null;

        for (java.lang.reflect.Field f : clazz.getDeclaredFields()) {
            if (f.getName().equals(field)) {
                col = f.getAnnotation(Column.class).name();
            }
        }

        String sql = "SELECT * FROM " + table + " WHERE " + col + "=?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setObject(1, value);

        ResultSet rs = ps.executeQuery();

        List<T> list = new ArrayList<>();

        while (rs.next()) {

            T obj = clazz.getDeclaredConstructor().newInstance();

            for (java.lang.reflect.Field f : clazz.getDeclaredFields()) {
                if (f.isAnnotationPresent(Column.class)) {

                    f.setAccessible(true);
                    Object val = rs.getObject(f.getAnnotation(Column.class).name());

                    if (f.getType().equals(LocalDateTime.class) && val != null) {
                        val = ((Timestamp) val).toLocalDateTime();
                    }

                    f.set(obj, val);
                }
            }

            list.add(obj);
        }

        return list;
    }
}
