package fp.serrano.karat

import edu.mit.csail.sdg.ast.ExprQt
import fp.serrano.karat.ast.*
import kotlin.reflect.*

object model {
  operator fun <S, A> getValue(thisRef: S, property: KProperty<*>): A =
    throw IllegalStateException("this should never be de-referenced")
  operator fun <S, A> setValue(thisRef: S, property: KProperty<*>, value: A) { }
}

interface ReflectedModule {
  fun <A: Any> set(klass: KClass<A>): KSig<A>
  fun <A, F> field(property: KProperty1<A, F>): KField<A, F>
  fun <F> global(property: KProperty0<F>): KSet<F>

  operator fun <A, B> KSet<A>.div(other: KProperty1<A, B>): KSet<B> =
    this / field(other)
  operator fun <A, B, C> KProperty1<A, B>.div(other: KProperty1<B, C>): KRelation<A, C> =
    field(this) / field(other)
  operator fun <A, B> KProperty1<A, B>.div(other: KSet<B>): KSet<A> =
    field(this) / other
  operator fun <A: Any, F> KProperty1<A, F>.get(other: KSet<A>): KSet<F> =
    field(this)[other]

  infix fun <A: Any, B: A> KExpr<Set<A>>.`==`(other: KClass<B>): KFormula =
    this `==` set(other)

  fun nextUnique(klass: KClass<*>): String
}

// indicates a global fact
interface Fact: ReflectedModule
// indicates a fact which applies to each instance of the class
interface InstanceFact<A>: ReflectedModule {
  val self: KThis<A>
}

fun <A: Any> ReflectedModule.someOf(klass: KClass<A>): KSet<A> = someOf(set(klass))
fun <A: Any> ReflectedModule.loneOf(klass: KClass<A>): KSet<A> = loneOf(set(klass))
fun <A: Any> ReflectedModule.oneOf(klass: KClass<A>): KSet<A> = oneOf(set(klass))
fun <A: Any> ReflectedModule.setOf(klass: KClass<A>): KSet<A> = setOf(set(klass))

inline fun <reified A: Any> ReflectedModule.set(): KSig<A> = set(A::class)
inline fun <reified A: Any> ReflectedModule.element(): KSig<A> = set(A::class)

inline fun <reified A: Any> ReflectedModule.empty(): KFormula = empty(set<A>())
fun <A, F> ReflectedModule.empty(property: KProperty1<A, F>): KFormula = empty(field(property))
fun <F> ReflectedModule.empty(property: KProperty0<F>): KFormula = empty(global(property))

inline fun <reified A: Any> ReflectedModule.atMostOne(): KFormula = atMostOne(set<A>())
fun <A, F> ReflectedModule.atMostOne(property: KProperty1<A, F>): KFormula = atMostOne(field(property))
fun <F> ReflectedModule.atMostOne(property: KProperty0<F>): KFormula = atMostOne(global(property))

fun <A, F> ReflectedModule.next(property: KProperty1<A, F>): KSet<Pair<A, F>> = next(field(property))
fun <F> ReflectedModule.next(property: KProperty0<F>): KSet<F> = next(global(property))

fun <A, F> ReflectedModule.current(property: KProperty1<A, F>): KSet<Pair<A, F>> = current(field(property))
fun <F> ReflectedModule.current(property: KProperty0<F>): KSet<F> = current(global(property))

fun <A, F> ReflectedModule.stays(property: KProperty1<A, F>): KFormula = stays(field(property))
fun <F> ReflectedModule.stays(property: KProperty0<F>): KFormula = stays(global(property))

fun <A> ReflectedModule.closure(p: KProperty1<A, A>): KRelation<A, A> = closure(field(p))
fun <A> ReflectedModule.closureOptional(p: KProperty1<A, A?>): KRelation<A, A> = closureOptional(field(p))
fun <A> ReflectedModule.oneOrMore(p: KProperty1<A, A?>): KRelation<A, A> = closureOptional(field(p))

fun <A> ReflectedModule.reflexiveClosure(p: KProperty1<A, A>): KRelation<A, A> = reflexiveClosure(field(p))
fun <A> ReflectedModule.reflexiveClosureOptional(p: KProperty1<A, A?>): KRelation<A, A> = reflexiveClosureOptional(field(p))
fun <A> ReflectedModule.zeroOrMore(p: KProperty1<A, A?>): KRelation<A, A> = reflexiveClosureOptional(field(p))

inline fun <reified A: Any> ReflectedModule.`for`(
  op: ExprQt.Op,
  x: String,
  noinline block: (KArg<A>) -> KFormula
): KFormula = `for`(op, x to set(A::class), block)

inline fun <reified A: Any, reified B: Any> ReflectedModule.`for`(
  op: ExprQt.Op,
  x: String,
  y: String,
  noinline block: (KArg<A>, KArg<B>) -> KFormula
): KFormula = `for`(op, x to set(A::class), y to set(B::class), block)

inline fun <reified A: Any> ReflectedModule.forAll(
  x: String,
  noinline block: (KArg<A>) -> KFormula
): KFormula = `for`(ExprQt.Op.ALL, x, block)

inline fun <reified A: Any> ReflectedModule.forAll(
  noinline block: (KArg<A>) -> KFormula
): KFormula = `for`(ExprQt.Op.ALL, nextUnique(A::class), block)

inline fun <reified A: Any> ReflectedModule.forAll(
  s: KSet<A>,
  noinline block: (KArg<A>) -> KFormula
): KFormula = `for`(ExprQt.Op.ALL, nextUnique(A::class) to s, block)

inline fun <reified A: Any, reified B: Any> ReflectedModule.forAll(
  x: String,
  y: String,
  noinline block: (KArg<A>, KArg<B>) -> KFormula
): KFormula = `for`(ExprQt.Op.ALL, x, y, block)

inline fun <reified A: Any, reified B: Any> ReflectedModule.forAll(
  noinline block: (KArg<A>, KArg<B>) -> KFormula
): KFormula = `for`(ExprQt.Op.ALL, nextUnique(A::class), nextUnique(B::class), block)

inline fun <reified A: Any> ReflectedModule.forSome(
  x: String,
  noinline block: (KArg<A>) -> KFormula
): KFormula = `for`(ExprQt.Op.SOME, x, block)

inline fun <reified A: Any> ReflectedModule.forSome(
  s: KSet<A>,
  noinline block: (KArg<A>) -> KFormula
): KFormula = `for`(ExprQt.Op.SOME, nextUnique(A::class) to s, block)

inline fun <reified A: Any> ReflectedModule.forSome(
  noinline block: (KArg<A>) -> KFormula
): KFormula = `for`(ExprQt.Op.SOME, nextUnique(A::class), block)

inline fun <reified A: Any, reified B: Any> ReflectedModule.forSome(
  x: String,
  y: String,
  noinline block: (KArg<A>, KArg<B>) -> KFormula
): KFormula = `for`(ExprQt.Op.SOME, x, y, block)

inline fun <reified A: Any, reified B: Any> ReflectedModule.forSome(
  noinline block: (KArg<A>, KArg<B>) -> KFormula
): KFormula = `for`(ExprQt.Op.SOME, nextUnique(A::class), nextUnique(B::class), block)