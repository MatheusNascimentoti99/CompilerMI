/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import Utils.VarTable;
import java.util.Objects;

/**
 *
 * @author Matheus Nascimento
 */
public class Struct {

    private String name;
    private VarTable varTable;
    private VarTable constTable;

    public Struct(Token token) {
        this.name = token.val.toString();
        varTable = new VarTable();
        constTable = new VarTable();

    }
    public Struct(String name) {
        this.name = name;
        varTable = new VarTable();
        constTable = new VarTable();

    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.name);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        return this.name.equals(((Struct) obj).name);
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the varTable
     */
    public VarTable getVarTable() {
        return varTable;
    }

    /**
     * @param varTable the varTable to set
     */
    public void setVarTable(VarTable varTable) {
        this.varTable = varTable;
    }

    /**
     * @return the constTable
     */
    public VarTable getConstTable() {
        return constTable;
    }

    /**
     * @param constTable the constTable to set
     */
    public void setConstTable(VarTable constTable) {
        this.constTable = constTable;
    }

}
