/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utils;

import Model.Var;
import Model.Token;
import java.util.Collection;
import java.util.HashMap;

/**
 *
 * @author Matheus Nascimento
 */
public class VarTable {

    private int escape;
    private HashMap<Integer, Var> simbolTable;

    public VarTable() {
        escape = 0;
        simbolTable = new HashMap<>();
    }

    /**
     * @return the escape
     */
    public int decrementEscape() {
        return escape--;
    }

    public int incrementEscape() {
        return escape++;
    }

    public boolean search(Token token, int escape) {
        Var var = new Var(token, escape);
        return simbolTable.containsKey(var.hashCode());
    }

    public void insert(Token token, int escape, boolean isUsing) {
        Var var = new Var(token, isUsing);
        simbolTable.put(var.hashCode(), var);
    }

    public void remove(Token token, int escape) {
        Var var = new Var(token, escape);
        simbolTable.remove(var.hashCode());
    }

    public boolean search(Token token) {
        Var var = new Var(token, 0);
        return simbolTable.containsKey(var.hashCode());
    }

    public boolean search(Var var) {
        return simbolTable.containsKey(var.hashCode());
    }

    public void insert(Token token, boolean isUsing) {
        Var var = new Var(token, isUsing);
        simbolTable.put(var.hashCode(), var);
    }

    public void insert(Var var) {
        simbolTable.put(var.hashCode(), var);
    }

    public void remove(Token token) {
        Var var = new Var(token, 0);
        simbolTable.remove(var.hashCode());
    }

    public Collection<Var> list() {
        return simbolTable.values();
    }

    public Var get(Var var) {
        return simbolTable.get(var.hashCode());
    }

    public Var get(Token token) {
        Var var = new Var(token);
        return simbolTable.get(var.hashCode());
    }

}
