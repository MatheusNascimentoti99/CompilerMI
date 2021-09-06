/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import Interface.Method;
import Model.Function;
import Model.Parser;
import Model.Struct;
import Model.Token;
import Model.Var;
import Utils.VarTable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Matheus Nascimento
 */
public class ParserController {

    private static final String ERROR_ESCAPE = "já existe valor declarado com o mesmo nome";
    private static final String ERROR_UNDEFINED = "o valor não está definido";
    private static final String ERROR_UNDEFINED_METHOD = "o método não está definido";
    private static final String ERROR_CONST = "não é possível alterar constante";
    private static final String ERROR_CASHING = "operação com tipos diferentes";
    private static final String ERROR_ARRAY = "o valor não espera um array";
    private static final String ERROR_DECLMETHOD = "já existe uma função ou procedimento com a mesma assinatura";
    private static final String ERROR_INDEX = "O valor não é um inteiro";
    private static final String ERROR_RETURN = "Tipo diferente da assinatura da função";

    private final List types = Arrays.asList("int", "real", "boolean", "string", "struct");
    private final Parser parse;
    List firstFunc_stm = Arrays.asList("if", "while", "{", "return", ";", "local", "global", "print", "read");
    List first_var_stm = Arrays.asList("local", "global", "print", "read");
    private List follow;
    VarTable simbolTable;
    TableController tableController;

    public ParserController(LinkedList<Token> tokens) {
        parse = new Parser(tokens);
        simbolTable = new VarTable();
        tableController = new TableController();
    }

    public Parser getParse() {
        return parse;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        parse.getResult().forEach((info) -> {
            result.append(info).append("\n");
        });
        result.append("\n\n-------------------------\n\n");
        return result.toString();
    }

    public void startSymbol() {
        program();

    }

    private void program() {
        structs();
        const_block();
        var_block();
        decls();
    }

    private void start_block() {

        if (parse.getCurrentToken().val.equals("start")) {
            parse.nextToken();
            if (!parse.getCurrentToken().val.equals("(")) {
                parse.includeError("(");
            } else {
                parse.nextToken();
            }
            if (!parse.getCurrentToken().val.equals(")")) {
                parse.includeError(")");
            } else {
                parse.nextToken();
            }
            func_block();
        } else {
            follow = Arrays.asList("function", "procedure");
            parse.includeError("start", follow);
        }
    }

    private void decls() {
        if (parse.getCurrentToken().val.equals("function")
                || parse.getCurrentToken().val.equals("procedure")) {
            decl();
            decls();
        }
    }

    private void decl() {
        func_decl();
        proc_decl();
    }

    private void func_decl() {
        if (parse.getCurrentToken().val.equals("function")) {
            tableController.newEscape();
            tableController.createFunction();
            parse.nextToken();
            type_return();
            if (parse.getCurrentToken().type == Token.T.IDE) {
                tableController.function.setName(parse.getCurrentToken().val.toString());
                parse.nextToken();
                if (!parse.getCurrentToken().val.equals("(")) {
                    parse.includeError("(");
                } else {
                    parse.nextToken();
                }
                params();
                if (parse.getCurrentToken().val.equals(")")) {
                    if (tableController.addFunction()) {
                        parse.includeError(ERROR_DECLMETHOD, Token.T.SEMANTIC);
                    }
                    parse.nextToken();
                    func_block();
                } else {
                    if (tableController.addFunction()) {
                        parse.includeError(ERROR_DECLMETHOD, Token.T.SEMANTIC);
                    }
                    follow = Arrays.asList("procedure", "function");
                    parse.includeError(")", follow);
                }
            } else {
                follow = Arrays.asList("procedure", "function");
                parse.includeError("IDE", follow);
            }
            tableController.removeEscape();

        }
    }

    private void proc_decl() {
        if (parse.getCurrentToken().val.equals("procedure")) {
            tableController.newEscape();
            parse.nextToken();
            if (parse.getCurrentToken().type == Token.T.IDE && !parse.getCurrentToken().val.equals("start")) {
                tableController.createProcedure();
                tableController.procedure.setName(parse.getCurrentToken().val.toString());
                parse.nextToken();
                if (!parse.getCurrentToken().val.equals("(")) {
                    parse.includeError("(");
                } else {
                    parse.nextToken();
                }
                params();
                if (parse.getCurrentToken().val.equals(")")) {
                    if (tableController.addProcedure()) {
                        parse.includeError(ERROR_DECLMETHOD, Token.T.SEMANTIC);
                    }
                    parse.nextToken();
                    func_block();
                } else {
                    if (tableController.addProcedure()) {
                        parse.includeError(ERROR_DECLMETHOD, Token.T.SEMANTIC);
                    }
                    follow = Arrays.asList("procedure", "function");
                    parse.includeError(")", follow);
                }

            } else if (parse.getCurrentToken().val.equals("start")) {
                start_block();
                if (parse.getCurrentToken().type != Token.T.EOF) {
                    follow = Arrays.asList("procedure", "function");
                    parse.includeError("EOF", follow);

                }
            } else {
                follow = Arrays.asList("procedure", "function");
                parse.includeError("IDE", follow);
            }
            tableController.removeEscape();

        }
    }

    private void param_type() {
        if (parse.getCurrentToken().type == Token.T.IDE) {
            if (!tableController.searchType(parse.getCurrentToken())) {
                parse.includeError(ERROR_UNDEFINED, Token.T.SEMANTIC);
            }
            parse.nextToken();
        } else if (types.contains(parse.getCurrentToken().val)) {
            type();
        }
    }

    private void type_return() {
        if (parse.getCurrentToken().type == Token.T.IDE) {
            if (!tableController.searchType(parse.getCurrentToken())) {
                parse.includeError(ERROR_UNDEFINED, Token.T.SEMANTIC);
            }
            tableController.function.setType(parse.getCurrentToken().val.toString());
            parse.nextToken();
        } else if (types.contains(parse.getCurrentToken().val)) {
            type();
            tableController.function.setType(tableController.type);
        }
    }

    private void params() {
        if (parse.getCurrentToken().val.equals("int")
                || parse.getCurrentToken().val.equals("real")
                || parse.getCurrentToken().val.equals("boolean")
                || parse.getCurrentToken().val.equals("string")
                || parse.getCurrentToken().val.equals("struct")
                || parse.getCurrentToken().type == Token.T.IDE) {
            param();
            params_list();
        }
    }

    private void params_list() {
        if (parse.getCurrentToken().val.equals(",")) {
            parse.nextToken();
            param();
            params_list();
        }

    }

    private void param() {
        param_type();
        if (parse.getCurrentToken().type == Token.T.IDE) {
            if (tableController.addVar(parse.getCurrentToken())) {
                parse.includeError(ERROR_ESCAPE, Token.T.SEMANTIC);
            } else {
                tableController.addParamsDecl();
            }
            parse.nextToken();
            param_arrays();
        } else {
            follow = Arrays.asList(",", ")");
            parse.includeError(", , )", follow);
        }
    }

    private void param_arrays() {
        if (parse.getCurrentToken().val.equals("[")) {
            tableController.var.addDimessionArray();
            parse.nextToken();
            if (parse.getCurrentToken().val.equals("]")) {
                parse.nextToken();
                param_mult_arrays();
            } else {
                follow = Arrays.asList(",", ")");
                parse.includeError("]", follow);
            }
        }
    }

    private void param_mult_arrays() {
        if (parse.getCurrentToken().val.equals("[")) {
            tableController.var.addDimessionArray();
            parse.nextToken();
            if (parse.getCurrentToken().type == Token.T.INT || parse.getCurrentToken().type == Token.T.REAL) {
                parse.nextToken();
                if (parse.getCurrentToken().val.equals("]")) {
                    parse.nextToken();
                    param_mult_arrays();
                } else {
                    follow = Arrays.asList(",", ")");
                    parse.includeError("]", follow);
                }
            } else {
                follow = Arrays.asList(",", ")");
                parse.includeError("NMO", follow);
            }
        }
    }

    private void structs() {
        if (parse.getCurrentToken().val.equals("struct")) {
            struct_block();
            structs();
        }
    }

    private void struct_block() {
        if (parse.getCurrentToken().val.equals("struct")) {
            if (!tableController.addStructScape(parse.nextToken())) {
                parse.includeError("já declarado", Token.T.SEMANTIC);
            }
            if (parse.getCurrentToken().type == Token.T.IDE) {
                parse.nextToken();
                extends_();
                if (!parse.getCurrentToken().val.equals("{")) {
                    parse.includeError("{");
                } else {
                    parse.nextToken();
                }
                const_block();
                var_block();
                if (!parse.getCurrentToken().val.equals("}")) {
                    follow = Arrays.asList("const", "procedure", "struct", "var");
                    parse.includeError("}", follow);
                } else {
                    parse.nextToken();
                }
            } else {
                follow = Arrays.asList("const", "procedure", "struct", "var");
                parse.includeError("IDE", follow);
            }
            tableController.removeStructScape();
        }
    }

    private void extends_() {
        if (parse.getCurrentToken().val.equals("extends")) {
            parse.nextToken();
            if (!parse.getCurrentToken().val.equals("struct")) {
                follow = Arrays.asList("{");
                parse.includeError("struct", follow);
            } else {
                parse.nextToken();
                if (parse.getCurrentToken().type == Token.T.IDE) {
                    parse.nextToken();
                } else {
                    follow = Arrays.asList("{");
                    parse.includeError("IDE", follow);
                }
            }
        }
    }

    private void const_block() {
        if (parse.getCurrentToken().val.equals("const")) {
            parse.nextToken();
            if (!parse.getCurrentToken().val.equals("{")) {
                parse.includeError("{");
            } else {
                parse.nextToken();
            }
            const_decls();
            if (!parse.getCurrentToken().val.equals("}")) {
                follow = Arrays.asList("procedure", "var");
                parse.includeError("}", follow);
            } else {
                parse.nextToken();
            }

        }

    }

    private void var_block() {
        if (parse.getCurrentToken().val.equals("var")) {
            parse.nextToken();
            if (!parse.getCurrentToken().val.equals("{")) {
                parse.includeError("{");
            } else {
                parse.nextToken();
            }
            var_decls();
            if (!parse.getCurrentToken().val.equals("}")) {
                follow = Arrays.asList("while", "read", "{", "local", "procedure", "return", "global", "print", "if", "IDE");
                parse.includeError("}", follow);

            } else {
                parse.nextToken();
            }
        }

    }

    private void const_decls() {
        if (parse.getCurrentToken().type == Token.T.IDE
                || types.contains(parse.getCurrentToken().val)
                || parse.getCurrentToken().val.equals("typedef")
                || parse.getCurrentToken().val.equals("local")
                || parse.getCurrentToken().val.equals("global")) {
            const_decl();
            const_decls();
        }
    }

    private void var_decls() {
        if (parse.getCurrentToken().type == Token.T.IDE || types.contains(parse.getCurrentToken().val)
                || parse.getCurrentToken().val.equals("typedef")
                || parse.getCurrentToken().val.equals("local")
                || parse.getCurrentToken().val.equals("global")) {
            var_decl();
            var_decls();
        }
    }

    private void const_decl() {
        if (types.contains(parse.getCurrentToken().val)) {
            type();
            const_();
            const_list();
        } else if (parse.getCurrentToken().val.equals("typedef")) {
            typedef();
        } else if (parse.getCurrentToken().val.equals("local") || parse.getCurrentToken().val.equals("global")) {
            stm_scope();
        } else if (parse.getCurrentToken().type == Token.T.IDE) {
            if (!tableController.addType(parse.getCurrentToken())) {
                parse.includeError(ERROR_UNDEFINED, Token.T.SEMANTIC);
            }
            parse.nextToken();
            const_id();
        } else {
            follow = Arrays.asList("string", "typedef", "local", "boolean", "}", "int", "struct", "real", "global", "IDE");
            parse.includeError("string, typedef, local, boolean, int, struct, real, global, IDE", follow);
        }
    }

    private void stm_scope() {
        if (parse.getCurrentToken().val.equals("local") || parse.getCurrentToken().val.equals("global")) {
            Token aux = parse.getCurrentToken();
            parse.nextToken();
            tableController.isLeftSide = true;
            accesses(aux);
            tableController.isLeftSide = false;

            assign(aux);
        }
    }

    private void var_decl() {
        if (types.contains(parse.getCurrentToken().val)) {
            type();
            var_();
            var_list();
            if (parse.getCurrentToken().val.equals(";")) {
                parse.nextToken();
            } else {
                follow = Arrays.asList("string", "typedef", "local", "boolean", "}", "int", "struct", "real", "global", "IDE");
                parse.includeError(";, ,", follow);
            }
        } else if (parse.getCurrentToken().val.equals("typedef")) {
            typedef();
        } else if (parse.getCurrentToken().type == Token.T.IDE) {
            if (!tableController.addType(parse.getCurrentToken())) {
                parse.includeError(ERROR_UNDEFINED, Token.T.SEMANTIC);
            }
            Token tokenaux = parse.getCurrentToken();
            parse.nextToken();
            var_id(tokenaux);
        } else {
            follow = Arrays.asList("string", "typedef", "local", "boolean", "}", "int", "struct", "real", "global", "IDE");
            parse.includeError("int, real, boolean, string, struct", follow);
        }
    }

    private void const_id() {
        if (parse.getCurrentToken().type == Token.T.IDE) {
            const_();
            const_list();
            if (parse.getCurrentToken().val.equals(";")) {
                parse.nextToken();
            } else {
                follow = Arrays.asList("string", "typedef", "local", "boolean", "}", "int", "struct", "real", "global", "IDE");
                parse.includeError(";", follow);
            }
        } else if (parse.getCurrentToken().val.equals("--")
                || parse.getCurrentToken().val.equals("(")
                || parse.getCurrentToken().val.equals(".")
                || parse.getCurrentToken().val.equals("=")
                || parse.getCurrentToken().val.equals("++")
                || parse.getCurrentToken().val.equals("[")) {
            stm_id(parse.getCurrentToken());
        } else {
            follow = Arrays.asList("string", "typedef", "local", "boolean", "}", "int", "struct", "real", "global", "IDE");
            parse.includeError("--, (, ., =, ++, [, IDE", follow);
        }
    }

    private void var_id(Token token) {
        if (parse.getCurrentToken().type == Token.T.IDE) {
            var_();
            var_list();
            if (parse.getCurrentToken().val.equals(";")) {
                parse.nextToken();
            } else {
                follow = Arrays.asList("string", "typedef", "local", "boolean", "}", "int", "struct", "real", "global", "IDE");
                parse.includeError(";", follow);
            }
        } else if (parse.getCurrentToken().val.equals("--")
                || parse.getCurrentToken().val.equals("(")
                || parse.getCurrentToken().val.equals(".")
                || parse.getCurrentToken().val.equals("=")
                || parse.getCurrentToken().val.equals("++")
                || parse.getCurrentToken().val.equals("[")) {
            stm_id(token);
        } else {
            follow = Arrays.asList("string", "typedef", "local", "boolean", "}", "int", "struct", "real", "global", "IDE");
            parse.includeError("--, (, ., =, ++, [, IDE", follow);
        }
    }

    private void stm_id(Token token) {
        if (parse.getCurrentToken().val.equals("=")
                || parse.getCurrentToken().val.equals("++")
                || parse.getCurrentToken().val.equals("--")) {
            if (!tableController.searchIDE(token)) {
                parse.includeError(ERROR_UNDEFINED, token, Token.T.SEMANTIC);
            }
            if (tableController.isConst(token)) {
                parse.includeError(ERROR_CONST, token, Token.T.SEMANTIC);
            }
            assign(token);

        } else if (parse.getCurrentToken().val.equals("[")) {
            if (!tableController.searchIDE(token)) {
                parse.includeError(ERROR_UNDEFINED, token, Token.T.SEMANTIC);
            }
            if (tableController.isConst(token)) {
                parse.includeError(ERROR_CONST, token, Token.T.SEMANTIC);
            }
            array();
            arrays();

            accesses(token);
            assign(token);
        } else if (parse.getCurrentToken().val.equals(".")) {
            if (!tableController.searchIDE(token)) {
                parse.includeError(ERROR_UNDEFINED, token, Token.T.SEMANTIC);
            }
            if (tableController.isConst(token)) {
                parse.includeError(ERROR_CONST, token, Token.T.SEMANTIC);
            }
            accesses(token);
            assign(token);
        } else if (parse.getCurrentToken().val.equals("(")) {
            parse.nextToken();
            Function function = new Function();
            function.setName(token.val.toString());
            args(parse.getCurrentToken(), function);
            if (token.type != Token.T.PRE) {
                try {
                    String type = tableController.getMethod(function).getType();
                    tableController.setTypeExpr(type);
                    tableController.typesExpr.add(type);
                    if (!tableController.checkTypesExp()) {
                        parse.includeError(ERROR_CASHING, Token.T.SEMANTIC);
                    }
                } catch (NullPointerException e) {
                    parse.includeError(ERROR_UNDEFINED_METHOD, token, Token.T.SEMANTIC);
                }
            }
            if (parse.getCurrentToken().val.equals(")")) {
                parse.nextToken();
                if (parse.getCurrentToken().val.equals(";")) {
                    parse.nextToken();
                } else {
                    follow = Arrays.asList("local", "}", "int", "real", "else", "return", "typedef", "while", "string", "IDE", "read", "boolean", ";", "struct", "global", "print", "if", "{");
                    parse.includeError(";", follow);
                }
            } else {
                follow = Arrays.asList("local", "}", "int", "real", "else", "return", "typedef", "while", "string", "IDE", "read", "boolean", ";", "struct", "global", "print", "if", "{");
                parse.includeError(")", follow);
            }
        } else {
            follow = Arrays.asList("local", "}", "int", "real", "else", "return", "typedef", "while", "string", "IDE", "read", "boolean", ";", "struct", "global", "print", "if", "{");
            parse.includeError("=, ++, --, [, . , (", follow);
        }
    }

    private void assign(Token token) {
        if (parse.getCurrentToken().val.equals("=")) {
            parse.nextToken();
            tableController.newExpr();
            expr(parse.getCurrentToken());
            if (!token.val.equals("local") && !token.val.equals("global")) {
                try {
                    if (!tableController.getTypeExpr().equals(tableController.getVarByIDE(token).getType())) {
                        parse.includeError(ERROR_CASHING, token, Token.T.SEMANTIC);
                    }
                } catch (NullPointerException e) {
                    parse.includeError(ERROR_UNDEFINED, token, Token.T.SEMANTIC);
                }
            }
            if (parse.getCurrentToken().val.equals(";")) {
                parse.nextToken();
            } else {
                follow = Arrays.asList("local", "}", "int", "real", "else", "return", "typedef", "while", "string", "IDE", "read", "boolean", "struct", "global", "print", "if", "{");
                parse.includeError(";", follow);
            }
        } else if (parse.getCurrentToken().val.equals("++")
                || parse.getCurrentToken().val.equals("--")) {
            parse.nextToken();
            if (parse.getCurrentToken().val.equals(";")) {
                parse.nextToken();
            } else {
                follow = Arrays.asList("local", "}", "int", "real", "else", "return", "typedef", "while", "string", "IDE", "read", "boolean", "struct", "global", "print", "if", "{");
                parse.includeError(";", follow);
            }
        } else {
            follow = Arrays.asList("local", "}", "int", "real", "else", "return", "typedef", "while", "string", "IDE", "read", "boolean", "struct", "global", "print", "if", "{");
            parse.includeError("=, ++, --", follow);

        }
    }

    private void type() {
        if (types.contains(parse.getCurrentToken().val)
                || parse.getCurrentToken().type == Token.T.IDE) {
            if (parse.getCurrentToken().val.equals("struct")) {
                parse.nextToken();
                if (parse.getCurrentToken().type != Token.T.IDE) {
                    follow = Arrays.asList("IDE");
                    parse.includeError("IDE", follow);
                } else {
                    if (!tableController.searchStruct(parse.getCurrentToken())) {
                        parse.includeError(ERROR_UNDEFINED, Token.T.SEMANTIC);
                    } else {
                        tableController.addType(parse.getCurrentToken());
                        Struct struct = tableController.getStructTable().get((new Struct(parse.getCurrentToken()).hashCode()));
                        tableController.var.setVarStruct(struct);
                    }
                    parse.nextToken();
                }
            } else {
                if (tableController.isCreateTypedef) {
                    if (!tableController.addType(parse.getCurrentToken())) {
                        parse.includeError(ERROR_UNDEFINED, Token.T.SEMANTIC);
                    }
                } else if (types.contains(parse.getCurrentToken().val)) {
                    tableController.addType(parse.getCurrentToken());
                }
                parse.nextToken();
            }
        }
    }

    private void typedef() {
        if (parse.getCurrentToken().val.equals("typedef")) {
            tableController.isCreateTypedef = true;
            parse.nextToken();
            type();
            if (parse.getCurrentToken().type == Token.T.IDE) {
                tableController.addTypeDef(parse.getCurrentToken());
                parse.nextToken();
                if (parse.getCurrentToken().val.equals(";")) {
                    parse.nextToken();
                } else {
                    follow = Arrays.asList("local", "}", "int", "real", "IDE", "string", "typedef", "boolean", "struct", "global");
                    parse.includeError(";", follow);
                }
            } else {
                follow = Arrays.asList("local", "}", "int", "real", "IDE", "string", "typedef", "boolean", "struct", "global");
                parse.includeError("IDE", follow);
            }
            tableController.isCreateTypedef = false;

        }
    }

    private void var_() {
        if (parse.getCurrentToken().type != Token.T.IDE) {
            follow = Arrays.asList(";", ",");
            parse.includeError("IDE", follow);
        } else {
            if (tableController.addVar(parse.getCurrentToken())) {
                parse.includeError(ERROR_ESCAPE, Token.T.SEMANTIC);
            }
            parse.nextToken();
            arrays();
        }
    }

    private void var_list() {
        if (parse.getCurrentToken().val.equals(",")) {
            parse.nextToken();
            var_();
            var_list();
        }
    }

    private void const_() {
        if (parse.getCurrentToken().type != Token.T.IDE) {
            follow = Arrays.asList("=", ",", ";");
            parse.includeError("IDE", follow);
        } else {
            if (tableController.addConst(parse.getCurrentToken())) {
                parse.includeError(ERROR_ESCAPE, Token.T.SEMANTIC);
            }
            parse.nextToken();
            arrays();
        }
    }

    private void const_list() {
        if (parse.getCurrentToken().val.equals(",")) {
            parse.nextToken();
            const_();
            const_list();
        } else if (parse.getCurrentToken().val.equals("=")) {
            parse.nextToken();
            decl_atribute();
            if (parse.getCurrentToken().val.equals(";")) {
                parse.nextToken();
            } else {
                follow = Arrays.asList("string", "typedef", "local", "boolean", "}", "int", "struct", "real", "global", "IDE");
                parse.includeError(";", follow);
            }
        } else {
            follow = Arrays.asList("string", "typedef", "local", "}", "boolean", "int", "struct", "real", "global", "IDE");
            parse.includeError(", , =", follow);
        }
    }

    private void func_block() {
        if (!parse.getCurrentToken().val.equals("{")) {
            parse.includeError("{");
        } else {
            parse.nextToken();
        }
        var_block();
        func_stms();
        if (parse.getCurrentToken().val.equals("}")) {
            parse.nextToken();
        } else {
            follow = Arrays.asList("function", "procedure");
            parse.includeError("}", follow);
        }
    }

    private void func_stms() {
        if (firstFunc_stm.contains(parse.getCurrentToken().val) || parse.getCurrentToken().type == Token.T.IDE) {
            func_stm();
            func_stms();
        }
    }

    private void func_stm() {
        follow = Arrays.asList("local", "}", "else", "return", "while", "id", "read", "global", "print", "{", "if");
        if (parse.getCurrentToken().val.equals("if")) {
            parse.nextToken();
            if (!parse.getCurrentToken().val.equals("(")) {
                parse.includeError("(");
            } else {
                parse.nextToken();
            }
            log_expr();
            if (!parse.getCurrentToken().val.equals(")")) {
                parse.includeError(")");
            } else {
                parse.nextToken();
            }
            if (!parse.getCurrentToken().val.equals("then")) {
                parse.includeError("then");
            } else {
                parse.nextToken();
            }
            func_stm();
            else_stm();
            func_stm();
        } else if (parse.getCurrentToken().val.equals("while")) {
            parse.nextToken();
            if (!parse.getCurrentToken().val.equals("(")) {
                parse.includeError("(");
            } else {
                parse.nextToken();
            }
            log_expr();
            if (parse.getCurrentToken().val.equals(")")) {
                parse.nextToken();
                func_stm();
            } else {
                follow = Arrays.asList("local", "}", "else", "return", "while", "IDE", "read", ";", "global", "print", "{", "if");
                parse.includeError("), IDE, NRO, LOG, ART, REL, DEL, CAD", follow);
            }
        } else if (parse.getCurrentToken().val.equals("{")
                || parse.getCurrentToken().val.equals("local")
                || parse.getCurrentToken().val.equals("global")
                || parse.getCurrentToken().val.equals("print")
                || parse.getCurrentToken().val.equals("read")
                || parse.getCurrentToken().val.equals("return")
                || parse.getCurrentToken().val.equals(";")
                || parse.getCurrentToken().type == Token.T.IDE) {
            func_normal_stm();
        }
    }

    private void func_normal_stm() {

        if (parse.getCurrentToken().val.equals("{")) {
            parse.nextToken();
            func_stms();
            if (parse.getCurrentToken().val.equals("}")) {
                parse.nextToken();
            } else {
                follow = Arrays.asList("local", "}", "else", "return", "while", "IDE", "read", ";", "global", "print", "{", "if");
                parse.includeError("}", follow);
            }
        } else if (first_var_stm.contains(parse.getCurrentToken().val)
                || parse.getCurrentToken().type == Token.T.IDE) {
            var_stm(parse.getCurrentToken());

        } else if (parse.getCurrentToken().val.equals(";")) {
            parse.nextToken();
        } else if (parse.getCurrentToken().val.equals("return")) {
            Token token = parse.getCurrentToken();
            parse.nextToken();
            tableController.newExpr();
            expr(parse.getCurrentToken());
            try {
                if (!tableController.function.getType().equals(tableController.typeExpr)) {
                    parse.includeError(ERROR_RETURN, token, Token.T.SEMANTIC);
                }
            } catch (NullPointerException e) {
                parse.includeError(ERROR_RETURN, token, Token.T.SEMANTIC);
            }
            if (parse.getCurrentToken().val.equals(";")) {
                parse.nextToken();
            } else {
                follow = Arrays.asList("local", "}", "else", "return", "while", "IDE", "read", ";", "global", "print", "{", "if");
                parse.includeError(";", follow);
            }
        }
    }

    private void else_stm() {
        if (parse.getCurrentToken().val.equals("else")) {
            parse.nextToken();
        }
    }

    private void var_stm(Token token) {
        if (parse.getCurrentToken().val.equals("local")
                || parse.getCurrentToken().val.equals("global")) {
            stm_scope();
        } else if (parse.getCurrentToken().type == Token.T.IDE) {
            Token aux = parse.getCurrentToken();
            parse.nextToken();
            stm_id(aux);
        } else if (parse.getCurrentToken().val.equals("read")
                || parse.getCurrentToken().val.equals("print")) {
            parse.nextToken();
            if (parse.getCurrentToken().val.equals("(")) {
                parse.nextToken();
                Function function = new Function();
                function.setName(token.val.toString());
                args(parse.getCurrentToken(), function);
                follow = Arrays.asList(";");
                if (parse.getCurrentToken().val.equals(")")) {
                    parse.nextToken();
                    if (token.type != Token.T.PRE) {
                        try {
                            String type = tableController.getMethod(function).getType();
                        } catch (NullPointerException e) {
                            parse.includeError(ERROR_UNDEFINED_METHOD, token, Token.T.SEMANTIC);
                        }
                    }
                    if (parse.getCurrentToken().val.equals(";")) {
                        parse.nextToken();
                    } else {
                        follow = Arrays.asList(";", "return", "else", "IDE", "global",
                                "local", "print", "if", "read", "}", "while", "{");
                        parse.includeError(";", follow);
                    }
                } else {
                    follow = Arrays.asList(";", "return", "else", "IDE", "global",
                            "local", "print", "if", "read", "}", "while", "{");
                    parse.includeError(")", follow);
                }
            } else {
                follow = Arrays.asList(";", "return", "else", "IDE", "global",
                        "local", "print", "if", "read", "}", "while", "{");
                parse.includeError("(", follow);
            }
        }
    }

    private void decl_atribute() {
        if (parse.getCurrentToken().val.equals("{")) {
            array_decl(parse.getCurrentToken());
        } else if (parse.getCurrentToken().type == Token.T.IDE
                || parse.getCurrentToken().type == Token.T.INT
                || parse.getCurrentToken().type == Token.T.REAL
                || parse.getCurrentToken().type == Token.T.ART
                || parse.getCurrentToken().type == Token.T.CAD
                || parse.getCurrentToken().val.equals("!")
                || parse.getCurrentToken().val.equals("(")
                || parse.getCurrentToken().val.equals("local")
                || parse.getCurrentToken().val.equals("global")
                || parse.getCurrentToken().val.equals("true")
                || parse.getCurrentToken().val.equals("false")) {
            tableController.newExpr();
            expr(parse.getCurrentToken());
        } else {
            follow = Arrays.asList(";");
            parse.includeError("{, (, NRO, IDE, CAD", follow);
        }

    }

    private void arrays() {
        if (parse.getCurrentToken().val.equals("[")) {
            if (tableController.var != null) {
                tableController.var.addDimessionArray();
            }
            array();
            arrays();
        }
    }

    private void array() {
        if (parse.getCurrentToken().val.equals("[")) {
            parse.nextToken();
            index();
            if (parse.getCurrentToken().val.equals("]")) {
                parse.nextToken();
            }
        }
    }
//incompleta

    private void index() {
        if (parse.getCurrentToken().val.toString().equals("true")
                || parse.getCurrentToken().val.toString().equals("false")
                || parse.getCurrentToken().type == Token.T.IDE
                || parse.getCurrentToken().type == Token.T.ART
                || parse.getCurrentToken().type == Token.T.INT
                || parse.getCurrentToken().type == Token.T.REAL
                || parse.getCurrentToken().type == Token.T.CAD
                || parse.getCurrentToken().val.equals("!")
                || parse.getCurrentToken().val.equals("(")) {
            tableController.newExpr();
            Token token = parse.getCurrentToken();
            expr(parse.getCurrentToken());
            try {
                String type = tableController.getTypeVar(token);
                tableController.setTypeExpr(type);
                tableController.typesExpr.add(type);
                if (!tableController.checkTypesExp()) {
                    parse.includeError(ERROR_CASHING, Token.T.SEMANTIC);
                }
            } catch (NullPointerException e) {
                parse.includeError(ERROR_UNDEFINED, Token.T.SEMANTIC);
            }
            if (!tableController.getTypeExpr().equals("int")) {
                parse.includeError(ERROR_INDEX, Token.T.SEMANTIC);
            }
        }
    }

    private void array_decl(Token token) {

        if (parse.getCurrentToken().val.equals("{")) {
            if (!tableController.var.isArray()) {
                parse.includeError(ERROR_ARRAY, Token.T.SEMANTIC);
            }
            parse.nextToken();
            array_def(token);
            if (parse.getCurrentToken().val.equals("}")) {
                parse.nextToken();
                array_vector();
            } else {
                follow = Arrays.asList(";");
                parse.includeError("}, ,", follow);
            }
        }
    }

    private void array_vector() {
        if (parse.getCurrentToken().val.equals(",")) {
            parse.nextToken();
            array_decl(parse.getCurrentToken());
        }
    }

    private void array_def(Token token) {
        tableController.newExpr();
        expr(token);
        try {
            String type = tableController.getTypeVar(parse.getCurrentToken());
            tableController.setTypeExpr(type);
            tableController.typesExpr.add(type);
            if (!tableController.checkTypesExp()) {
                parse.includeError(ERROR_CASHING, Token.T.SEMANTIC);
            }
        } catch (NullPointerException e) {
            parse.includeError(ERROR_UNDEFINED, Token.T.SEMANTIC);
        }
        array_expr(token);
    }

    private void array_expr(Token token) {
        if (parse.getCurrentToken().val.equals(",")) {
            parse.nextToken();
            array_def(token);
        }
    }

    private String expr(Token token) {
        or(token);
        return "";
    }

    private void or(Token token) {
        and(token);
        or_(token);
    }

    private void or_(Token token) {
        if (parse.getCurrentToken().val.equals("||")) {
            parse.nextToken();
            and(token);
            or_(token);
            tableController.setTypeExpr("boolean");
        }
    }

    private void and(Token token) {
        equate(token);
        and_(token);
    }

    private void and_(Token token) {
        if (parse.getCurrentToken().val.equals("&&")) {
            parse.nextToken();
            equate(token);
            and_(token);
            tableController.setTypeExpr("boolean");
        }
    }

    private void equate(Token token) {
        compare(token);
        equate_(token);
    }

    private void equate_(Token token) {
        if (parse.getCurrentToken().val.equals("==") || parse.getCurrentToken().val.equals("!=")) {
            parse.nextToken();
            compare(token);
            equate_(token);
            tableController.setTypeExpr("boolean");
        }
    }

    private void compare(Token token) {
        add(token);
        compare_(token);
    }

    private void compare_(Token token) {
        if (parse.getCurrentToken().type == Token.T.REL) {
            parse.nextToken();
            add(token);
            compare_(token);
        }
    }

    private void add(Token token) {
        mult(token);
        add_(token);
    }

    private void add_(Token token) {
        if (parse.getCurrentToken().val.equals("+")
                || parse.getCurrentToken().val.equals("-")) {
            parse.nextToken();
            mult(token);
            add_(token);
        }
    }

    private void mult(Token token) {
        unary(token);
        mult_(token);
    }

    private void mult_(Token token) {
        if (parse.getCurrentToken().val.equals("*") || parse.getCurrentToken().val.equals("/")) {
            parse.nextToken();
            if (parse.getCurrentToken().val.equals("/")) {
                tableController.typesExpr.add("real");
            };
            unary(token);
            mult_(token);
        }
    }

    private void unary(Token token) {
        if (parse.getCurrentToken().val.equals("!")) {
            parse.nextToken();
            unary(token);
            tableController.setTypeExpr("boolean");
        } else {
            value(token);
        }
    }

    private String value(Token token) {
        if (parse.getCurrentToken().type == Token.T.INT
                || parse.getCurrentToken().type == Token.T.REAL
                || parse.getCurrentToken().type == Token.T.CAD
                || parse.getCurrentToken().val.toString().equals("true")
                || parse.getCurrentToken().val.toString().equals("false")) {
            try {
                String type = tableController.getTypeVar(parse.getCurrentToken());
                tableController.setTypeExpr(type);
                tableController.typesExpr.add(type);
                if (!tableController.checkTypesExp()) {
                    parse.includeError(ERROR_CASHING, Token.T.SEMANTIC);
                }
            } catch (NullPointerException e) {
                parse.includeError(ERROR_UNDEFINED, Token.T.SEMANTIC);

            }
            parse.nextToken();
        } else if (parse.getCurrentToken().val.equals("local")
                || parse.getCurrentToken().val.equals("global")) {
            Token tokenAux = parse.getCurrentToken();
            parse.nextToken();
            return access(tokenAux);
        } else if (parse.getCurrentToken().type == Token.T.IDE) {
            Token tokenaux = parse.getCurrentToken();
            parse.nextToken();
            id_value(tokenaux);
        } else if (parse.getCurrentToken().val.equals("(")) {
            parse.nextToken();
            expr(token);
            try {
                String type = tableController.getTypeVar(parse.getCurrentToken());
                tableController.setTypeExpr(type);
                tableController.typesExpr.add(type);
                if (!tableController.checkTypesExp()) {
                    parse.includeError(ERROR_CASHING, Token.T.SEMANTIC);
                }
            } catch (NullPointerException e) {
                parse.includeError(ERROR_UNDEFINED, Token.T.SEMANTIC);
            }
            if (parse.getCurrentToken().val.equals(")")) {
                parse.nextToken();
            } else {
                follow = Arrays.asList("*", "}", "]", "<", "-", "<=", ">", "!=", "/", "&&", "||", ">=", ";", ")", "+", ",", "==");
                parse.includeError(")", follow);
            }
        } else if (parse.getCurrentToken().val.equals("true") || parse.getCurrentToken().val.equals("false")) {
            tableController.setTypeExpr("boolean");
            parse.nextToken();
        } else {
            follow = Arrays.asList("*", "}", "]", "<", "-", "<=", ">", "!=", "/", "&&", "||", ">=", ";", ")", "+", ",", "==");
            parse.includeError("LOG, CAD, NRO, 'local, 'global', IDE, (", follow);
        }
        return "";
    }

    private void id_value(Token token) {
        if (parse.getCurrentToken().val.equals("[")
                || parse.getCurrentToken().val.equals(".")) {
            if (!tableController.searchIDE(token)) {
                parse.includeError(ERROR_UNDEFINED, token, Token.T.SEMANTIC);
            }
            try {
                String type = tableController.getTypeVar(token);
                tableController.setTypeExpr(type);
                tableController.typesExpr.add(type);
                if (!tableController.checkTypesExp()) {
                    parse.includeError(ERROR_CASHING, Token.T.SEMANTIC);
                }
            } catch (NullPointerException e) {
                parse.includeError(ERROR_UNDEFINED, Token.T.SEMANTIC);
            }
            arrays();
            accesses(token);
        } else if (parse.getCurrentToken().val.equals("(")) {
            Function function = new Function();
            function.setName(token.val.toString());
            parse.nextToken();
            args(parse.getCurrentToken(), function);
            if (token.type != Token.T.PRE) {
                try {
                    String type = tableController.getMethod(function).getType();
                    tableController.setTypeExpr(type);
                    tableController.typesExpr.add(type);
                    if (!tableController.checkTypesExp()) {
                        parse.includeError(ERROR_CASHING, Token.T.SEMANTIC);
                    }
                } catch (NullPointerException e) {
                    parse.includeError(ERROR_UNDEFINED_METHOD, token, Token.T.SEMANTIC);
                }
            }
            if (parse.getCurrentToken().val.equals(")")) {
                parse.nextToken();
            } else {
                follow = Arrays.asList("*", "}", "]", "<", "-", "<=", ">", "!=", "/", "&&", "||", ">=", ";", ")", "+", ",", "==");
                parse.includeError(")", follow);
            }
        } else {
            if (!tableController.searchIDE(token)) {
                parse.includeError(ERROR_UNDEFINED, token, Token.T.SEMANTIC);
            } else {
                try {
                    String type = tableController.getTypeVar(token);
                    tableController.setTypeExpr(type);
                    tableController.typesExpr.add(type);
                    if (!tableController.checkTypesExp()) {
                        parse.includeError(ERROR_CASHING, Token.T.SEMANTIC);
                    }
                } catch (NullPointerException e) {
                    parse.includeError(ERROR_UNDEFINED, Token.T.SEMANTIC);

                }
            }
        }
    }

    private void args(Token token, Method function) {
        if (parse.getCurrentToken().val.toString().equals("true")
                || parse.getCurrentToken().val.toString().equals("false")
                || parse.getCurrentToken().type == Token.T.IDE
                || parse.getCurrentToken().type == Token.T.INT
                || parse.getCurrentToken().type == Token.T.REAL
                || parse.getCurrentToken().type == Token.T.CAD
                || parse.getCurrentToken().type == Token.T.ART
                || parse.getCurrentToken().val.equals("!")
                || parse.getCurrentToken().val.equals("(")) {
            expr(token);
            try {
                String type = tableController.getTypeVar(token);
                tableController.setTypeExpr(type);
                tableController.typesExpr.add(type);
                if (!tableController.checkTypesExp()) {
                    parse.includeError(ERROR_CASHING, Token.T.SEMANTIC);
                }
            } catch (NullPointerException e) {
                parse.includeError(ERROR_UNDEFINED, Token.T.SEMANTIC);
            }
            Var var = new Var(tableController.typeExpr);
            function.addParams(var);
            args_list(token, function);
        }
    }

    private void args_list(Token token, Method function) {
        if (parse.getCurrentToken().val.equals(",")) {
            parse.nextToken();
            Token aux = parse.getCurrentToken();
            expr(aux);
            try {
                String type = tableController.getTypeVar(aux);
                tableController.setTypeExpr(type);
                tableController.typesExpr.add(type);
                if (!tableController.checkTypesExp()) {
                    parse.includeError(ERROR_CASHING, Token.T.SEMANTIC);
                }
            } catch (NullPointerException e) {
                parse.includeError(ERROR_UNDEFINED, Token.T.SEMANTIC);
            }
            Var var = new Var(tableController.typeExpr);
            function.addParams(var);
            args_list(token, function);
        }
    }

    private String access(Token token) {
        Var var = null;
        if (parse.getCurrentToken().val.equals(".")) {
            parse.nextToken();

            if (parse.getCurrentToken().type == Token.T.IDE) {
                if (!token.val.equals("local") && !token.val.equals("global")) {

                    try {
                        var = tableController.getAccessOfVar(token).getVarTable().get(parse.getCurrentToken());
                        if (var == null) {
                            var = tableController.getAccessOfVar(token).getConstTable().get(parse.getCurrentToken());
                            if (var == null) {
                                parse.includeError(ERROR_UNDEFINED, parse.getCurrentToken(), Token.T.SEMANTIC);
                            }
                        }

                    } catch (NullPointerException e) {
                        parse.includeError(ERROR_UNDEFINED, parse.getCurrentToken(), Token.T.SEMANTIC);
                    }
                } else {
                    if (token.val.equals("local")) {
                        var = tableController.getVarLocalTable().get(parse.getCurrentToken());
                    } else if (token.val.equals("global")) {
                        var = tableController.getVarGlobal(parse.getCurrentToken());
                        if (tableController.isLeftSide && tableController.getConstTable().get(parse.getCurrentToken()) != null) {
                            parse.includeError(ERROR_CONST, Token.T.SEMANTIC);
                        }
                    }

                    try {
                        if (tableController.isLeftSide || tableController.getTypeExpr().equals(var.getType())) {
                        } else {
                            parse.includeError(ERROR_CASHING, parse.getCurrentToken(), Token.T.SEMANTIC);
                        }
                    } catch (NullPointerException e) {
                        parse.includeError(ERROR_UNDEFINED, parse.getCurrentToken(), Token.T.SEMANTIC);
                    }
                }
                parse.nextToken();
                arrays();

            }
        } else {
            follow = Arrays.asList("*", "}", "]", "<", "-", "<=", ">", "!=", "/", "&&", "||", ">=", ";", ")", "+", ",", "==");
            parse.includeError(".", follow);
        }
        return var != null ? var.getType() : "";
    }

    private void accesses(Token token) {
        if (parse.getCurrentToken().val.equals(".")) {
            access(token);
            accesses(token);
        }
    }

    private void log_expr() {
        log_or();
    }

    private void log_or() {
        log_and();
        log_or_();
    }

    private void log_or_() {
        if (parse.getCurrentToken().val.equals("||")) {
            parse.nextToken();
            log_and();
            log_or_();
        }
    }

    private void log_and() {
        log_equate();
        log_and_();
    }

    private void log_and_() {
        if (parse.getCurrentToken().val.equals("&&")) {
            parse.nextToken();
            log_equate();
            log_and_();
        }
    }

    private void log_equate() {
        log_compare();
        log_equate_();
    }

    private void log_equate_() {
        if (parse.getCurrentToken().val.equals("==")
                || parse.getCurrentToken().val.equals("!=")) {
            parse.nextToken();
            log_compare();
            log_equate_();
        }
    }

    private void log_compare() {
        log_unary();
        log_compare_();
    }

    private void log_compare_() {
        if (parse.getCurrentToken().val.equals("<")
                || parse.getCurrentToken().val.equals(">")
                || parse.getCurrentToken().val.equals(">=")
                || parse.getCurrentToken().val.equals("<=")) {
            parse.nextToken();
            log_unary();
            log_compare_();
        }
    }

    private void log_unary() {
        if (parse.getCurrentToken().val.equals("!")) {
            parse.nextToken();
            log_unary();
        } else if (parse.getCurrentToken().type == Token.T.INT
                || parse.getCurrentToken().type == Token.T.REAL
                || parse.getCurrentToken().type == Token.T.IDE
                || parse.getCurrentToken().type == Token.T.CAD
                || parse.getCurrentToken().val.equals("false")
                || parse.getCurrentToken().val.equals("true")
                || parse.getCurrentToken().val.equals("local")
                || parse.getCurrentToken().val.equals("global")
                || parse.getCurrentToken().val.equals("(")) {
            log_value();
        }
    }

    private void log_value() {
        if (parse.getCurrentToken().type == Token.T.INT
                || parse.getCurrentToken().type == Token.T.REAL
                || parse.getCurrentToken().val.equals("false")
                || parse.getCurrentToken().val.equals("true")
                || parse.getCurrentToken().type == Token.T.CAD) {
            parse.nextToken();
        } else if (parse.getCurrentToken().val.equals("local")
                || parse.getCurrentToken().val.equals("global")) {
            Token token = parse.getCurrentToken();
            parse.nextToken();
            access(token);
        } else if (parse.getCurrentToken().type == Token.T.IDE) {
            id_value(parse.nextToken());
        } else if (parse.getCurrentToken().val.equals("(")) {
            parse.nextToken();
            log_expr();
            if (parse.getCurrentToken().val.equals(")")) {
                parse.nextToken();
            } else {
                follow = Arrays.asList(")");
                parse.includeError(")", follow);
            }
        } else {
            follow = Arrays.asList(")");
            parse.includeError("false, local, str, true, IDE, NMO, global", follow);
        }
    }
}
