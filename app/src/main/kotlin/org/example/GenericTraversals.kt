package org.example

object GenericTraversals {
    sealed class Lit
    data class StrLit(val value: String) : Lit()
    data class IntLit(val value: Int) : Lit()
    data class Ident(val value: String) : Lit()

    sealed class Expr<out T>
    data class Index<out T>(val target: T, val idx: T) : Expr<T>()
    data class Call<out T>(val func: T, val args: List<T>) : Expr<T>()
    data class Unary<out T>(val operator: String, val expr: T) : Expr<T>()
    data class Binary<out T>(val expr1: T, val operator: String, val expr2: T) : Expr<T>()
    data class Paren<out T>(val expr: T) : Expr<T>()
    data class Literal<out T>(val lit: Lit) : Expr<T>()

    fun <T, R> fmap(expr: Expr<T>, f: (T) -> R): Expr<R> = when (expr) {
        is Literal -> Literal(expr.lit)
        is Paren -> Paren(f(expr.expr))
        is Index -> Index(f(expr.target), f(expr.idx))
        is Call -> Call(f(expr.func), expr.args.map(f))
        is Unary -> Unary(expr.operator, f(expr.expr))
        is Binary -> Binary(f(expr.expr1), expr.operator, f(expr.expr2))
    }

    private fun <T : Expr<T>> bottomUp(f: (Expr<T>) -> Expr<T>, expr: Expr<T>): Expr<T> = f(fmap(expr) { bottomUp(f, it) } as Expr<T>)

    private fun <T : Expr<T>> topDown(f: (Expr<T>) -> Expr<T>, expr: Expr<T>): Expr<T> = fmap(f(expr)) { topDown(f, it) } as Expr<T>

    private fun <T : Expr<T>> flattenTerm(expr: Expr<T>): Expr<T> = when(expr) {
        is Paren -> expr.expr
        else -> expr
    }

    fun <T : Expr<T>> flatten(expr: Expr<T>): Expr<T> = bottomUp(::flattenTerm, expr)

    private fun <R, T : Expr<T>> cata(f: (Expr<R>) -> R, expr: Expr<T>): R = f(fmap(expr) { cata(f, it) })

    private fun countNodes(expr: Expr<Int>): Int = when(expr) {
        is Unary -> expr.expr + 1
        is Binary -> expr.expr1 + expr.expr2 + 1
        is Call -> expr.func + expr.args.sum() + 1
        is Index -> expr.target + expr.idx + 1
        is Paren -> expr.expr + 1
        is Literal -> 1
    }

    fun <T : Expr<T>> count(expr: Expr<T>): Int = cata(::countNodes, expr)

    private fun <T : Expr<T>> bottomUpViaCata(f: (Expr<T>) -> Expr<T>, expr: Expr<T>): Expr<T> = cata({ f(it as Expr<T>) }, expr)
    fun <T : Expr<T>> flattenViaCata(expr: Expr<T>): Expr<T> = bottomUpViaCata(::flattenTerm, expr)
    fun <T : Expr<T>> flattenViaCata2(expr: Expr<T>): Expr<T> = cata({ flattenTerm(it) as T }, expr)

}


