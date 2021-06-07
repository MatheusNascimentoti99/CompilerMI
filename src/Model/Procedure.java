/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import Interface.Method;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;

/**
 *
 * @author Matheus Nascimento
 */
public class Procedure implements Method {

    private String type;
    private LinkedList<Var> params;
    private String name;
    private boolean isUsing;

    public Procedure() {
        params = new LinkedList<>();
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean isFunction() {
        return false;
    }

    @Override
    public LinkedList<Var> params() {
        return params;
    }

    @Override
    public String typeReturn() {
        return "void";
    }

    @Override
    public void addParams(Var var) {
        params.add(var);
    }

    @Override
    public boolean checkParams(LinkedList<Var> params) {
        int index = 0;
        for (Iterator<Var> i = this.params.iterator(); i.hasNext() && index < params.size();) {
            Var aux = i.next();
            if (!params.get(index).getType().equals(aux.getType())) {
                return false;
            }
        }
        return params.size() == this.params.size();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 73 * hash + Objects.hashCode(this.type);
        hash = 73 * hash + Objects.hashCode(this.params);
        hash = 73 * hash + Objects.hashCode(this.name);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!((Method) obj).getName().equals(this.name)) {
            return false;
        }
        if (obj == null) {
            return false;
        }
        final Method other = (Method) obj;
        return this.checkParams(((Method) obj).params());

    }

}
