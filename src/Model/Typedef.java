/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

/**
 *
 * @author Matheus Nascimento
 */
public class Typedef {

    private final String name;
    private final Struct struct;
    public Typedef(String name) {
        this.name = name;
        this.struct = null;
    }
    
    public Typedef(String name, Struct struct) {
        this.name = name;
        this.struct = struct;
    }

    public Struct getStruct() {
        return struct;
    }
    
    
    public String getName(){
        return name;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        return name.equals(((Typedef)obj).name);
    }

}
