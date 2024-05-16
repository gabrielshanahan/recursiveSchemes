package org.example

object Initial {
    sealed class Lit
    data class StrLit(val value: String) : Lit()
    data class IntLit(val value: Int) : Lit()
    data class Ident(val value: String) : Lit()

    sealed class Expr
    data class Index(val target: Expr, val idx: Expr) : Expr()
    data class Call(val func: Expr, val args: List<Expr>) : Expr()
    data class Unary(val operator: String, val expr: Expr) : Expr()
    data class Binary(val expr1: Expr, val operator: String, val expr2: Expr) : Expr()
    data class Paren(val expr: Expr) : Expr()
    data class Literal(val lit: Lit) : Expr()

    fun applyExpr(f: (Expr) -> Expr, expr: Expr): Expr = when (expr) {
        is Literal -> expr
        is Paren -> Paren(f(expr.expr))
        is Index -> Index(f(expr.target), f(expr.idx))
        is Call -> Call(f(expr.func), expr.args.map(f))
        is Unary -> Unary(expr.operator, f(expr.expr))
        is Binary -> Binary(f(expr.expr1), expr.operator, f(expr.expr2))
    }

    fun flatten(expr: Expr): Expr = when (expr) {
        is Paren -> flatten(expr.expr)
        else -> applyExpr(::flatten, expr)
    }
}


