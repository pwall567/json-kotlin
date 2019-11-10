package net.pwall.json

// Note - these classes are copied from the Kotlin documentation:
// https://kotlinlang.org/docs/reference/sealed-classes.html

sealed class Expr

data class Const(val number: Double) : Expr()

data class Sum(val e1: Expr, val e2: Expr) : Expr()

object NotANumber : Expr()
