package org.icfp2017

import org.amshove.kluent.`should equal to`
import org.amshove.kluent.shouldContain
import org.junit.Test

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

}
