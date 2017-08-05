package org.icfp2017

import org.amshove.kluent.`should equal to`
import org.amshove.kluent.shouldContain
import org.junit.Test

class SimServerTest {

  @Test
  fun randomRunTest() {
      // Api.gameLoop("{\"sites\":[{\"id\":4},{\"id\":1},{\"id\":3},{\"id\":6},{\"id\":5},{\"id\":0},{\"id\":7},{\"id\":2}],\n \"rivers\":[{\"source\":3,\"target\":4},{\"source\":0,\"target\":1},{\"source\":2,\"target\":3},\n           {\"source\":1,\"target\":3},{\"source\":5,\"target\":6},{\"source\":4,\"target\":5},\n           {\"source\":3,\"target\":5},{\"source\":6,\"target\":7},{\"source\":5,\"target\":7},\n           {\"source\":1,\"target\":7},{\"source\":0,\"target\":7},{\"source\":1,\"target\":2}],\n \"mines\":[1,5]}\n",
      //              listOf(Api.randomPunter(0), Api.randomPunter(1)));
  }

}
