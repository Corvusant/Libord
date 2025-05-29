package com.atomic_crucible.libord.optional

sealed class Optional<out A>

data class Some<out A>(val get: A) : Optional<A>()

object None : Optional<Nothing>()

fun <A, B> Optional<A>.map(f: (A) -> B): Optional<B>
    = when (this) {
        is Some<A> -> Some(f(this.get))
        is None -> None
    }

fun <A, B> Optional<A>.bind(f: (A) -> Optional<B>): Optional<B>
    = when (this) {
        is Some<A> -> f(this.get)
        is None -> None
    }

fun <A> Optional<A>.executeIfSet(f: (A) -> Unit ) : Unit
        = when (this) {
        is Some<A> -> f(this.get)
        is None -> Unit
}

fun <A, B> Optional<A>.flatten(fSet: (A) -> B, fUnset: () -> B): B
    = when (this) {
        is Some<A> -> fSet(this.get)
        is None -> fUnset()
    }

fun <A> Optional<A>.getOrElse(default: () -> A): A
    = when (this) {
        is Some<A> -> this.get
        is None -> default()
    }

fun <A, B> Optional<A>.flatMap(f: (A) -> Optional<B>): Optional<B>
    = this.map(f).getOrElse { None }


fun <A> Optional<A>.orElse(ob: () -> Optional<A>): Optional<A>
    = this.map{Some(it)}.getOrElse { ob() }

fun <A> Optional<A>.filter(f: (A) -> Boolean): Optional<A>
    = this.flatMap {
        a -> when(f(a)) {
            true -> Some(a)
            false -> None
        }
    }

fun <A> fromNullable(i: A?): Optional<A>
    = when (i){
        null -> None
        else -> Some(i)
    }