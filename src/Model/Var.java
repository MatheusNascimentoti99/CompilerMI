/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import java.util.LinkedList;

/**
 *
 * @author Matheus Nascimento
 */
public class Var {

    public enum Type {
        REAL, INT, BOOLEAN, STRING, STRUCT;
    };
    private String type;
    private String name;
    private Struct varStruct;
    private Token token;
    private boolean isUsing;
    private int dimessionArray;

    public Var(Token token, boolean isUsing) {
        this.dimessionArray = 0;
        this.name = token.val.toString();
        this.token = token;
        this.isUsing = isUsing;
    }

    public Var(Token token, boolean isUsing, String type) {
        this.token = token;
        this.name = token.val.toString();
        this.isUsing = isUsing;
        this.type = type;
    }

    public Var(String type) {
        this.type = type;
    }

    public Var(Token token, int escope) {
        this.token = token;
        this.name = token.val.toString();
        this.isUsing = false;
    }

    public Var(Token token) {
        this.token = token;
        this.name = token.val.toString();
        this.isUsing = false;
    }

    public String getType() {
        return type;
    }

    public int getDimessionArray() {
        return dimessionArray;
    }

    public void addDimessionArray() {
        dimessionArray++;
    }

    public boolean isArray() {
        return dimessionArray > 0;
    }

    /**
     * @return the token
     */
    public Token getToken() {
        return token;
    }

    /**
     * @param token the token to set
     */
    public void setToken(Token token) {
        this.token = token;
    }

    /**
     * @return the isUsing
     */
    public boolean isIsUsing() {
        return isUsing;
    }

    /**
     * @param isUsing the isUsing to set
     */
    public void setIsUsing(boolean isUsing) {
        this.isUsing = isUsing;
    }

    @Override
    public int hashCode() {
        return this.name.hashCode() + this.dimessionArray;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Var other = (Var) obj;
        return this.token.val.equals(other.token.val.toString());
    }

    /**
     * @return the varStruct
     */
    public Struct getVarStruct() {
        return varStruct;
    }

    /**
     * @param varStruct the varStruct to set
     */
    public void setVarStruct(Struct varStruct) {
        this.varStruct = varStruct;
    }

}
