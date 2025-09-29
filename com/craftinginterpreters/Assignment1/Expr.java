package com.craftinginterpreters.Assignment1;

import java.util.List;

abstract class Expr {
  interface Visitor<R> {
    R visitAssignExpr(Assign expr);
    R visitBinaryExpr(Binary expr);
    R visitGroupingExpr(Grouping expr);
    R visitLiteralExpr(Literal expr);
    R visitUnaryExpr(Unary expr);
    R visitVariableExpr(Variable expr);
    R visitRiverExpr(River expr); // Work on this tomorrow :)
    R visitDamExpr(Dam expr);
  }
  static class Assign extends Expr {
    Assign(Token name, Expr value) {
      this.name = name;
      this.value = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitAssignExpr(this);
    }

    final Token name;
    final Expr value;
  }
  static class Binary extends Expr {
    Binary(Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitBinaryExpr(this);
    }

    final Expr left;
    final Token operator;
    final Expr right;
  }
  static class Grouping extends Expr {
    Grouping(Expr expression) {
      this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitGroupingExpr(this);
    }

    final Expr expression;
  }
  static class Literal extends Expr {
    Literal(Object value) {
      this.value = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitLiteralExpr(this);
    }

    final Object value;
  }
  static class Unary extends Expr {
    Unary(Token operator, Expr right) {
      this.operator = operator;
      this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitUnaryExpr(this);
    }

    final Token operator;
    final Expr right;
  }
  static class Variable extends Expr {
    Variable(Token name) {
      this.name = name;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitVariableExpr(this);
    }

    final Token name;
  }

  static class River extends Expr {
    River(String name, String volume, String flowRate, String riverType, String[] POIs) {
        this.name = name;
        this.volume = volume;
        this.flowRate = flowRate;
        this.riverType = riverType;
        this.POIs = POIs;
    }
    @Override
    <R> R accept(Visitor<R> visitor) {
        return visitor.visitRiverExpr(this);
    }
    final String name;
    final String volume;
    final String flowRate;
    final String riverType;
    final String[] POIs;
}

static class Dam extends Expr {
    Dam(String name, String capacity, String parentRiver, String destinationRiver) {
        this.name = name;
        this.capacity = capacity;
        this.parentRiver = parentRiver;
        this.destinationRiver = destinationRiver;
    }
    @Override
    <R> R accept(Visitor<R> visitor) {
        return visitor.visitDamExpr(this);
    }
    final String name;
    final String capacity;
    final String parentRiver;
    final String destinationRiver;
}


  abstract <R> R accept(Visitor<R> visitor);
}
