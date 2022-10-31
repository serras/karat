package fp.serrano.karat

import edu.mit.csail.sdg.ast.Expr
import edu.mit.csail.sdg.ast.ExprConstant
import edu.mit.csail.sdg.ast.ExprQt

interface TFormula

open class KFormula(expr: Expr): KExpr<TFormula>(expr)

// paragraphs are sets of formulae in conjunction

class KParagraphBuilder {
  val formulae = mutableListOf<KFormula>()

  operator fun KFormula.unaryPlus() {
    formulae.add(this)
  }

  fun build(): KFormula = and(formulae)
}

fun (KParagraphBuilder.() -> Unit).build(): KFormula =
  KParagraphBuilder().also(this).build()

fun <A> (KParagraphBuilder.(A) -> Unit).build(x: A): KFormula =
  KParagraphBuilder().apply { this@build(x) }.build()

fun <A, B> (KParagraphBuilder.(A, B) -> Unit).build(x: A, y: B): KFormula =
  KParagraphBuilder().apply { this@build(x, y) }.build()

fun paragraph(block: KParagraphBuilder.() -> Unit): KFormula =
  block.build()

// basic boolean constructions

object Constants {
  val TRUE: KFormula  = KFormula(ExprConstant.TRUE)
  val FALSE: KFormula = KFormula(ExprConstant.FALSE)
}

fun not(formula: KFormula): KFormula =
  KFormula(formula.expr.not())

infix fun KFormula.or(other: KFormula): KFormula =
  KFormula(this.expr.or(other.expr))

fun or(formulae: Collection<KFormula>): KFormula =
  when {
    formulae.isEmpty() -> Constants.FALSE
    else -> formulae.reduce { acc, f -> acc.or(f) }
  }

infix fun KFormula.and(other: KFormula): KFormula =
  KFormula(this.expr.and(other.expr))

fun and(formulae: Collection<KFormula>): KFormula =
  when {
    formulae.isEmpty() -> Constants.TRUE
    else -> formulae.reduce { acc, f -> acc.and(f) }
  }

fun and(block: KParagraphBuilder.() -> Unit): KFormula =
  paragraph(block)

infix fun KFormula.iff(other: KFormula): KFormula =
  KFormula(this.expr.iff(other.expr))

infix fun KFormula.implies(other: KFormula): KFormula =
  KFormula(this.expr.implies(other.expr))

// comparisons

infix fun <A> KExpr<A>.`==`(other: KExpr<A>): KFormula =
  KFormula(this.expr.equal(other.expr))

// quantification

fun <A> `for`(op: ExprQt.Op, x: Pair<String, KSet<A>>, block: (KArg<A>) -> KFormula): KFormula {
  val arg = x.second.arg(x.first)
  return KFormula(op.make(null, null, listOf(arg.decl), block(arg).expr))
}
