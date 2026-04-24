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
import java.util.*;

public class Session {
    private Connection conn;
    private Map<Object, Boolean> dirty = new HashMap<>();
    private boolean inTransaction = false;

    public Session(Connection conn) { this.conn = conn; }

    public void beginTransaction() throws SQLException { 
        conn.setAutoCommit(false); inTransaction=true; 
    }
    /*public void commit() throws SQLException,Exception {
        for(Object o: dirty.keySet()) update(o);
        conn.commit(); dirty.clear(); inTransaction=false;
    }*/
    public void flush() throws SQLException,Exception {
        for(Object o: dirty.keySet()) update(o);
        dirty.clear();
    }
    public void commit() throws SQLException,Exception {
        flush();
        conn.commit(); 
        inTransaction=false;
    }
    public void rollback() throws SQLException { conn.rollback(); dirty.clear(); inTransaction=false; }

    public <T> T find(Class<T> clazz, Object id) throws Exception {
        String table = clazz.getAnnotation(Entity.class).table();
        String idCol = Arrays.stream(clazz.getDeclaredFields())
                .filter(f->f.isAnnotationPresent(Id.class))
                .findFirst().get().getAnnotation(Column.class).name();

        String sql = "SELECT * FROM "+table+" WHERE "+idCol+"=?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setObject(1, id);
        ResultSet rs = stmt.executeQuery();
        if(rs.next()){
            T obj = clazz.getDeclaredConstructor().newInstance();
            for(var f: clazz.getDeclaredFields()){
                if(f.isAnnotationPresent(Column.class)){
                    f.setAccessible(true);
                    f.set(obj, rs.getObject(f.getAnnotation(Column.class).name()));
                }
            }
            return obj;
        }
        return null;
    }

    public void save(Object o) throws Exception {
        Class<?> clazz = o.getClass();
        String table = clazz.getAnnotation(Entity.class).table();
        List<String> cols = new ArrayList<>();
        List<Object> vals = new ArrayList<>();
        for(var f: clazz.getDeclaredFields()){
            if(f.isAnnotationPresent(Column.class)){
                f.setAccessible(true);
                cols.add(f.getAnnotation(Column.class).name());
                vals.add(f.get(o));
            }
        }
        String colStr = String.join(",", cols);
        String qMarks = String.join(",", Collections.nCopies(cols.size(), "?"));
        String sql = "INSERT INTO "+table+" ("+colStr+") VALUES ("+qMarks+")";
        PreparedStatement stmt = conn.prepareStatement(sql);
        for(int i=0;i<vals.size();i++) stmt.setObject(i+1, vals.get(i));
        stmt.executeUpdate();
    }

    public void markDirty(Object o){ 
        dirty.put(o,true); 
    }

    /*****private void update(Object o) throws Exception {
        Class<?> clazz = o.getClass();
        String table = clazz.getAnnotation(Entity.class).table();
        String idCol = Arrays.stream(clazz.getDeclaredFields()).filter(f->f.isAnnotationPresent(Id.class)).findFirst()
                .get().getAnnotation(Column.class).name();
        Object idVal = Arrays.stream(clazz.getDeclaredFields()).filter(f->f.isAnnotationPresent(Id.class)).findFirst()
                .get().get(o);  
        
        
        List<String> sets = new ArrayList<>();
        List<Object> vals = new ArrayList<>();
        for(var f: clazz.getDeclaredFields()){
            if(f.isAnnotationPresent(Column.class) && !f.isAnnotationPresent(Id.class)){
                f.setAccessible(true);
                sets.add(f.getAnnotation(Column.class).name()+"=?");
                vals.add(f.get(o));
            }
        }
        String sql = "UPDATE "+table+" SET "+String.join(",", sets)+" WHERE "+idCol+"=?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        for(int i=0;i<vals.size();i++) stmt.setObject(i+1, vals.get(i));
        stmt.setObject(vals.size()+1, idVal);
        stmt.executeUpdate();
    }*/

    public void update(Object o) throws SQLException, IllegalAccessException {
        Class<?> clazz = o.getClass();
        String table = clazz.getAnnotation(Entity.class).table();

        java.lang.reflect.Field idField = Arrays.stream(clazz.getDeclaredFields())
            .filter(f -> f.isAnnotationPresent(Id.class))
            .findFirst()
        .orElseThrow();
        idField.setAccessible(true);
        String idCol = idField.getAnnotation(Column.class).name();
        Object idVal = idField.get(o);

        List<String> sets = new ArrayList<>();
        List<Object> vals = new ArrayList<>();
        for (java.lang.reflect.Field f : clazz.getDeclaredFields()) {
            if (f.isAnnotationPresent(Column.class) && !f.isAnnotationPresent(Id.class)) {
                f.setAccessible(true);
                sets.add(f.getAnnotation(Column.class).name() + "=?");
                vals.add(f.get(o));
            }
        }

        String sql = "UPDATE " + table + " SET " + String.join(",", sets) + " WHERE " + idCol + "=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < vals.size(); i++)
                stmt.setObject(i + 1, vals.get(i));
            stmt.setObject(vals.size() + 1, idVal);
            //System.out.println(stmt);
            stmt.executeUpdate();
        }
    }
    
}

