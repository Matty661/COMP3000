package com.craftinginterpreters.Assignment1;

import java.util.Arrays;

import com.craftinginterpreters.Assignment1.Expr.Assign;
import com.craftinginterpreters.Assignment1.Expr.Dam;
import com.craftinginterpreters.Assignment1.Expr.River;
import com.craftinginterpreters.Assignment1.Expr.TimeUnits;
import com.craftinginterpreters.Assignment1.Expr.Variable;
import com.craftinginterpreters.Assignment1.Stmt.Block;
import com.craftinginterpreters.Assignment1.Stmt.Expression;
import com.craftinginterpreters.Assignment1.Stmt.Print;
import com.craftinginterpreters.Assignment1.Stmt.Var;

class AstPrinter implements Expr.Visitor<String>, Stmt.Visitor<String> {
    String print(Stmt statement) {
        return statement.accept(this);
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize(expr.operator.lexeme,
                expr.left, expr.right);
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return parenthesize("group", expr.expression);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null)
            return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.right);
    }

    private String parenthesize(String name, Expr... exprs) {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for (Expr expr : exprs) {
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }

    public static void main(String[] args) {
    }

    @Override
    public String visitAssignExpr(Assign expr) {
        return "(assign " + expr.name.lexeme + " " + expr.value.accept(this) + ")";
    }

    @Override
    public String visitVariableExpr(Variable expr) {
        return expr.name.lexeme;
    }

    @Override
    public String visitRiverExpr(River expr) {
        return "River " + expr.name + " | Volume " + expr.volume + " | Flow Rate " + expr.flowRate + " | Type " + expr.riverType + " | Point Of Interests " + Arrays.toString(expr.POIs);
    }

    @Override
    public String visitDamExpr(Dam expr) {
        return "Dam " + expr.name + " | Capacity " + expr.capacity + " | Parent River " + expr.parentRiver + " | Destination River " + expr.destinationRiver;
    }

    @Override
    public String visitBlockStmt(Block stmt) {
        StringBuilder builder = new StringBuilder();
        builder.append("{ ");
        for (Stmt statement : stmt.statements) {
            builder.append(statement.accept(this));
            builder.append(" ");
        }
        builder.append("}");
        return builder.toString();
    }

    @Override
    public String visitExpressionStmt(Expression stmt) {

        if (stmt.expression instanceof Expr.Literal &&
                ((Expr.Literal) stmt.expression).value.toString().contains("->")) {
            return parenthesize("connection", stmt.expression);
        }

        return parenthesize("print", stmt.expression);
    }

    @Override
    public String visitPrintStmt(Print stmt) {
        return parenthesize("print", stmt.expression);
    }

    @Override
    public String visitVarStmt(Var stmt) {
        if (stmt.initializer != null) {
            return parenthesize("var " + stmt.name.lexeme, stmt.initializer);
        }
        return "(var " + stmt.name.lexeme + ")";
    }

    @Override
    public String visitTimeUnitsExpr(TimeUnits timeUnits) {
        return "Flow Units: " + timeUnits.unit + " | #Days: " + timeUnits.numDays;
    }

}
