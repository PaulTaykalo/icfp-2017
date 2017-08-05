package org.icfp2017

import org.amshove.kluent.`should equal to`
import org.amshove.kluent.shouldContain
import org.junit.Test
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

//  @Test
//  fun helloClojure () {
//    Dummy.dummy(1) `should equal to` "Dummy 1"
//    val require = Clojure.`var`("clojure.core", "require")
//    (require as IFn).invoke(Clojure.read("icfp.core"))
//    val foobar = Clojure.`var`("icfp.core", "foobar")
//    (foobar.invoke(42) as String) `should equal to` "foo Dummy 42"
//    // Scorer test
//    require.invoke(Clojure.read("icfp.scorer"))
//    val score = Clojure.`var`("icfp.scorer", "-score")
//    val startWorld = "{\"sites\":[{\"id\":4},{\"id\":1},{\"id\":3},{\"id\":6},{\"id\":5},{\"id\":0},{\"id\":7},{\"id\":2}],\n\"rivers\":[{\"source\":3,\"target\":4},{\"source\":0,\"target\":1},{\"source\":2,\"target\":3},\n{\"source\":1,\"target\":3},{\"source\":5,\"target\":6},{\"source\":4,\"target\":5},\n{\"source\":3,\"target\":5},{\"source\":6,\"target\":7},{\"source\":5,\"target\":7},\n{\"source\":1,\"target\":7},{\"source\":0,\"target\":7},{\"source\":1,\"target\":2}],\n\"mines\":[1,5]}"
//    val sampleSequence = "[{\"claim\":{\"punter\":0,\"source\":1,\"target\":3}},{\"claim\":{\"punter\":1,\"source\":5,\"target\":6}}]"
//    (score.invoke(startWorld, 2, sampleSequence) as String) `should equal to` "[{\"punter\":0,\"score\":0},{\"punter\":1,\"score\":0}]"
//  }

}
