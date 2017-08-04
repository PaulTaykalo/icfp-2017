package org.icfp2017

import org.amshove.kluent.`should equal to`
import org.amshove.kluent.shouldContain
import org.junit.Test
import icfp.Dummy
import clojure.java.api.Clojure
import clojure.lang.IFn

class MainKtTest {

  @Test
  fun helloTest() {
    "hello" `should equal to` "hello"
  }

  @Test
  fun helloCollections() {
    data class Person(val name: String, val surname: String)
    val alice = Person("Alice", "Bob")
    val jon = Person("Jon", "Doe")
    val list = listOf(alice, jon)
    list shouldContain jon
  }

  @Test
  fun helloClojure () {
    Dummy.dummy(1) `should equal to` "Dummy 1"
    val require = Clojure.`var`("clojure.core", "require")
    (require as IFn).invoke(Clojure.read("icfp.core"))
    val foobar = Clojure.`var`("icfp.core", "foobar")
    (foobar.invoke(42) as String) `should equal to` "foo Dummy 42"
  }

}
