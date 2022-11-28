package fp.serrano.karat.ast

import edu.mit.csail.sdg.ast.Expr
import edu.mit.csail.sdg.ast.ExprConstant
import edu.mit.csail.sdg.ast.ExprQt
import fp.serrano.karat.ReflectedModule

interface TFormula

open class KFormula(expr: Expr): KExpr<TFormula>(expr)

// paragraphs are sets of formulae in conjunction

class KParagraphBuilder {
  private val formulae = mutableListOf<KFormula>()

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
  val TRUE: KFormula = KFormula(ExprConstant.TRUE)
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

fun <A> `for`(
  op: ExprQt.Op,
  x: Pair<String, KSet<A>>,
  block: (KArg<A>) -> KFormula
): KFormula {
  val arg = x.second.arg(x.first)
  return KFormula(op.make(null, null, listOf(arg.decl), block(arg).expr))
}

inline fun <reified A: Any> ReflectedModule.`for`(
  op: ExprQt.Op,
  x: String,
  noinline block: (KArg<A>) -> KFormula
): KFormula = `for`(op, x to set(A::class), block)

fun <A> `for`(
  op: ExprQt.Op,
  t1: KSet<A>,
  fn: kotlin.reflect.KFunction1<KArg<A>, KFormula>
): KFormula = `for`(op, fn.parameters[0].name!! to t1, fn)

fun <A, B> `for`(
  op: ExprQt.Op,
  x: Pair<String, KSet<A>>,
  y: Pair<String, KSet<B>>,
  block: (KArg<A>, KArg<B>) -> KFormula
): KFormula {
  val arg1 = x.second.arg(x.first)
  val arg2 = y.second.arg(y.first)
  return KFormula(op.make(null, null, listOf(arg1.decl, arg2.decl), block(arg1, arg2).expr))
}

inline fun <reified A: Any, reified B: Any> ReflectedModule.`for`(
  op: ExprQt.Op,
  x: String,
  y: String,
  noinline block: (KArg<A>, KArg<B>) -> KFormula
): KFormula = `for`(op, x to set(A::class), y to set(B::class), block)

fun <A, B> `for`(
  op: ExprQt.Op,
  t1: KSet<A>,
  t2: KSet<B>,
  fn: kotlin.reflect.KFunction2<KArg<A>, KArg<B>, KFormula>
): KFormula = `for`(op, fn.parameters[0].name!! to t1, fn.parameters[1].name!! to t2, fn)

// builder for all and some

fun <A> forAll(
  x: Pair<String, KSet<A>>,
  block: (KArg<A>) -> KFormula
): KFormula = `for`(ExprQt.Op.ALL, x, block)

inline fun <reified A: Any> ReflectedModule.forAll(
  x: String,
  noinline block: (KArg<A>) -> KFormula
): KFormula = `for`(ExprQt.Op.ALL, x, block)

fun <A> forAll(
  t1: KSet<A>,
  fn: kotlin.reflect.KFunction1<KArg<A>, KFormula>
): KFormula = `for`(ExprQt.Op.ALL, t1, fn)

fun <A, B> forAll(
  x: Pair<String, KSet<A>>,
  y: Pair<String, KSet<B>>,
  block: (KArg<A>, KArg<B>) -> KFormula
): KFormula = `for`(ExprQt.Op.ALL, x, y, block)

inline fun <reified A: Any, reified B: Any> ReflectedModule.forAll(
  x: String,
  y: String,
  noinline block: (KArg<A>, KArg<B>) -> KFormula
): KFormula = `for`(ExprQt.Op.ALL, x, y, block)

fun <A, B> forAll(
  t1: KSet<A>,
  t2: KSet<B>,
  fn: kotlin.reflect.KFunction2<KArg<A>, KArg<B>, KFormula>
): KFormula = `for`(ExprQt.Op.ALL, t1, t2, fn)

fun <A> forSome(
  x: Pair<String, KSet<A>>,
  block: (KArg<A>) -> KFormula
): KFormula = `for`(ExprQt.Op.SOME, x, block)

inline fun <reified A: Any> ReflectedModule.forSome(
  x: String,
  noinline block: (KArg<A>) -> KFormula
): KFormula = `for`(ExprQt.Op.SOME, x, block)

fun <A> forSome(
  t1: KSet<A>,
  fn: kotlin.reflect.KFunction1<KArg<A>, KFormula>
): KFormula = `for`(ExprQt.Op.SOME, t1, fn)

fun <A, B> forSome(
  x: Pair<String, KSet<A>>,
  y: Pair<String, KSet<B>>,
  block: (KArg<A>, KArg<B>) -> KFormula
): KFormula = `for`(ExprQt.Op.SOME, x, y, block)

inline fun <reified A: Any, reified B: Any> ReflectedModule.forSome(
  x: String,
  y: String,
  noinline block: (KArg<A>, KArg<B>) -> KFormula
): KFormula = `for`(ExprQt.Op.SOME, x, y, block)

fun <A, B> forSome(
  t1: KSet<A>,
  t2: KSet<B>,
  fn: kotlin.reflect.KFunction2<KArg<A>, KArg<B>, KFormula>
): KFormula = `for`(ExprQt.Op.SOME, t1, t2, fn)