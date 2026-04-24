/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package demo;

/**
 *
 * @author Tanar
 */
public class Field<T> {

    private String column;

    public Field(String column) {
        this.column = column;
    }

    public String getColumn() {
        return column;
    }

    public Condition eq(T value) {
        return new Condition(column, "=", value);
    }

    public Condition isNull() {
        return new Condition(column, "IS NULL", null);
    }
}
