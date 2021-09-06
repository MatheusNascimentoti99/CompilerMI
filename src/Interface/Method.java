/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Interface;

import Model.Var;
import java.util.Collection;
import java.util.LinkedList;

/**
 *
 * @author Matheus Nascimento
 */
public interface Method {


    public String getName();
    public void setName(String name);

    public String getType();

    public boolean isFunction();

    public LinkedList<Var> params();

    public String typeReturn();

    public void addParams(Var var);

    @Override
    public boolean equals(Object obj);

    /**
     *
     * @param params
     * @return
     */
    public boolean checkParams(LinkedList<Var> params);
    
    @Override
    public int hashCode();
}
