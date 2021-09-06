/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import Interface.Method;
import Model.Function;
import Model.Procedure;
import Model.Struct;
import Model.Token;
import Model.Typedef;
import Model.Var;
import Utils.VarTable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Matheus Nascimento
 */
public class TableController {

    private VarTable varLocalTable;
    private final VarTable varGlobalTable;
    private final VarTable constTable;

    private boolean global;
    private final HashMap<Integer, Struct> structTable;
    private final HashMap<Integer, Typedef> typedefTable;
    private final HashMap<Integer, Method> methodTable;

    private final List primitiveTypes = Arrays.asList("int", "real", "boolean", "string", "struct");
    private Struct structScape;
    public LinkedList<String> typesExpr;
    public boolean isCreateTypedef;
    public String type;
    public String typeVar;
    public String typeExpr = "";
    public boolean isLeftSide = false;
    Var var;
    Function function;
    Procedure procedure;

    public TableController() {
        global = true;
        isCreateTypedef = false;
        methodTable = new HashMap();
        typedefTable = new HashMap();
        constTable = new VarTable();
        varGlobalTable = new VarTable();
        structTable = new HashMap();
    }

    public void newExpr() {
        typesExpr = new LinkedList<>();
    }

    public void newEscape() {
        global = false;
        varLocalTable = new VarTable();
    }

    public VarTable getVarLocalTable() {
        return varLocalTable;
    }

    public VarTable getVarGlobalTable() {
        return varGlobalTable;
    }

    public VarTable getConstTable() {
        return constTable;
    }

    public void removeEscape() {
        function = null;
        varLocalTable = null;
    }

    public boolean addStructScape(Token struct) {
        this.structScape = new Struct(struct);
        return !structTable.containsValue(this.structScape);
    }

    public boolean searchStruct(Token token) {
        Struct structCheck = new Struct(token);
        return structTable.containsValue(structCheck);
    }

    public boolean addType(Token token) {
        Typedef type = new Typedef(token.val.toString());
        this.type = token.val.toString();
        return primitiveTypes.contains(token.val.toString()) || typedefTable.containsValue(type);

    }

    public void removeStructScape() {
        structTable.put(structScape.hashCode(), structScape);
        this.structScape = null;
    }

    public void addTypeDef(Token token) {
        Struct structCheck = new Struct(token);
        structCheck = structTable.get(structCheck.hashCode());
        Typedef type = new Typedef(token.val.toString(), structCheck);
        typedefTable.put(type.hashCode(), type);
    }

    public boolean searchType(Token token) {
        Typedef type = new Typedef(token.val.toString());
        return typedefTable.containsValue(type);
    }

    public Struct getAccessOfVar(Token token) {
        Var var = new Var(token);
        var = varLocalTable.get(var);
        if (var == null) {
            var = varGlobalTable.get(var);
            if (var == null) {
                var = constTable.get(var);
            }
        }
        Typedef type = new Typedef(var.getType());
        type = typedefTable.get(type.hashCode());
        if (type == null) {
            Struct struct = new Struct(var.getType());
            return structTable.get(struct.hashCode());
        } else {
            return type.getStruct();
        }
    }

    public boolean searchIDE(Token token) {
        boolean localVar = false;
        if (varLocalTable != null) {
            localVar = varLocalTable.search(token);

        }
        return localVar || varGlobalTable.search(token) || constTable.search(token);
    }

    public boolean isConst(Token token) {
        boolean isVar = false;
        if (varLocalTable != null) {
            isVar = varLocalTable.search(token);
        }
        return !isVar
                && !varGlobalTable.search(token)
                && constTable.search(token);
    }

    public Var getVarByIDE(Token token) {
        Var varAux = new Var(token);
        varAux = varLocalTable.get(token);
        if (varAux == null) {
            varAux = varGlobalTable.get(token);
            if (varAux == null) {
                varAux = constTable.get(token);
            }
        }
        return varAux;
    }

    public Var getVarGlobal(Token token) {
        Var varAux;
        varAux = varGlobalTable.get(token);
        if (varAux == null) {
            varAux = constTable.get(token);
        }
        return varAux;
    }

    public boolean addVar(Token token) {
        var = new Var(token, false, this.type);
        if (structScape != null && !structScape.getVarTable().search(var)) {
            structScape.getVarTable().insert(var);
            return false;
        } else if (structScape != null) {
            return true;
        } else if (global) {
            boolean exist = varGlobalTable.search(var) || constTable.search(var);
            varGlobalTable.insert(var);
            return exist;
        } else if (!varLocalTable.search(var)) {
            varLocalTable.insert(var);
            return false;
        } else {
            return true;
        }
    }

    public boolean addConst(Token token) {
        var = new Var(token, false, this.type);
        if (structScape != null && !structScape.getConstTable().search(var)) {
            structScape.getConstTable().insert(var);
            return false;
        } else if (structScape != null) {
            return true;
        } else if (!constTable.search(var)
                && !varGlobalTable.search(var)) {
            constTable.insert(var);
            return false;
        } else {
            return true;
        }
    }

    public void addParamsDecl() {
        if (function != null) {
            function.addParams(var);
        } else {
            procedure.addParams(var);
        }
    }

    public void createFunction() {
        function = new Function();
    }

    public void createProcedure() {
        procedure = new Procedure();
    }

    public boolean addFunction() {
        if (methodTable.containsValue(function)) {
            return true;
        }
        methodTable.put(function.hashCode(), function);
        return false;
    }

    public Method getMethod(Function method) {
        method.hashCode();
        return methodTable.get(method.hashCode());
    }

    public boolean addProcedure() {
        if (methodTable.containsValue(procedure)) {
            procedure = null;
            return true;
        }
        methodTable.put(procedure.hashCode(), procedure);
        procedure = null;
        return false;
    }
    

    public HashMap<Integer, Struct> getStructTable() {
        return structTable;
    }

    public void setType(Token token) {
        this.type = this.getVarByIDE(token).getType();
    }

    public boolean checkType(Var var) {
        if ("".equals(type)) {
            return true;
        } else {
            return var.getType().equals(type);
        }
    }

    public String getTypeVar(Token token) {
        if (null != token.type) {
            switch (token.type) {
                case CAD:
                    return "string";
                case REAL:
                    return "real";
                case INT:
                    return "int";
                case PRE:
                    if(token.val.toString().equals("true")|| token.val.toString().equals("false"));
                        return "boolean";
                case IDE:
                    Var varaux = new Var(token);
                    varaux = varLocalTable.get(varaux);
                    if (varaux == null) {
                        varaux = varGlobalTable.get(varaux);
                        if (varaux == null) {
                            varaux = constTable.get(varaux);
                        }
                        return varaux.getType();
                    } else {
                        return varaux.getType();
                    }
            }
        }
        return "";
    }

    public boolean checkTypesExp() {
        Iterator i = typesExpr.iterator();
        while (i.hasNext()) {
            for (int index = 0; index < typesExpr.size(); index++) {
                String typeDefault = typesExpr.get(index);
                if (!typeDefault.equals((String) i.next())) {
                    return false;
                }
            }
        }
        return true;
    }

    public String getTypeExpr() {
        return typeExpr;
    }

    public void setTypeExpr(String typeExpr) {
        this.typeExpr = typeExpr;
    }

}
