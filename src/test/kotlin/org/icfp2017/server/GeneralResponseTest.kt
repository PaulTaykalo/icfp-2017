package org.icfp2017.server

import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.google.gson.reflect.TypeToken
import org.junit.Test

class GeneralResponseTest {

//{"move":{"moves":[{"claim":{"punter":0,"source":3,"target":5}},{"pass":{"punter":1}}]},"state":{"ownedRivers":[],"unownedRivers":[{"source":3,"target":4},{"source":0,"target":1},{"source":2,"target":3},{"source":1,"target":3},{"source":5,"target":6},{"source":4,"target":5},{"source":3,"target":5},{"source":6,"target":7},{"source":5,"target":7},{"source":1,"target":7},{"source":0,"target":7},{"source":1,"target":2}],"myRivers":[],"mines":[1,5],"sitesReachedForMine":{"1":[],"5":[]},"riversForSite":{"4":[{"source":3,"target":4},{"source":4,"target":5}],"1":[{"source":0,"target":1},{"source":1,"target":3},{"source":1,"target":7},{"source":1,"target":2}],"3":[{"source":3,"target":4},{"source":2,"target":3},{"source":1,"target":3},{"source":3,"target":5}],"6":[{"source":5,"target":6},{"source":6,"target":7}],"5":[{"source":5,"target":6},{"source":4,"target":5},{"source":3,"target":5},{"source":5,"target":7}],"0":[{"source":0,"target":1},{"source":0,"target":7}],"7":[{"source":6,"target":7},{"source":5,"target":7},{"source":1,"target":7},{"source":0,"target":7}],"2":[{"source":2,"target":3},{"source":1,"target":2}]},"sitesForSite":{"4":[3,5],"1":[0,3,7,2],"3":[2,1,4,5],"6":[5,7],"5":[4,3,6,7],"0":[1,7],"7":[6,5,1,0],"2":[1,3]},"siteScores":{"4":{"1":9,"5":1},"1":{"1":0,"5":9},"3":{"1":1,"5":1},"6":{"1":16,"5":1},"5":{"1":9,"5":0},"0":{"1":1,"5":25},"7":{"1":1,"5":1},"2":{"1":1,"5":9}},"punter":1,"punters":2}}

    @Test
    fun moveSerialization() {
        val response = """{"move":{"moves":[{"claim":{"punter":0,"source":3,"target":5}},{"pass":{"punter":1}}]},"state":{"ownedRivers":[],"unownedRivers":[{"source":3,"target":4},{"source":0,"target":1},{"source":2,"target":3},{"source":1,"target":3},{"source":5,"target":6},{"source":4,"target":5},{"source":3,"target":5},{"source":6,"target":7},{"source":5,"target":7},{"source":1,"target":7},{"source":0,"target":7},{"source":1,"target":2}],"myRivers":[],"mines":[1,5],"sitesReachedForMine":{"1":[],"5":[]},"riversForSite":{"4":[{"source":3,"target":4},{"source":4,"target":5}],"1":[{"source":0,"target":1},{"source":1,"target":3},{"source":1,"target":7},{"source":1,"target":2}],"3":[{"source":3,"target":4},{"source":2,"target":3},{"source":1,"target":3},{"source":3,"target":5}],"6":[{"source":5,"target":6},{"source":6,"target":7}],"5":[{"source":5,"target":6},{"source":4,"target":5},{"source":3,"target":5},{"source":5,"target":7}],"0":[{"source":0,"target":1},{"source":0,"target":7}],"7":[{"source":6,"target":7},{"source":5,"target":7},{"source":1,"target":7},{"source":0,"target":7}],"2":[{"source":2,"target":3},{"source":1,"target":2}]},"sitesForSite":{"4":[3,5],"1":[0,3,7,2],"3":[2,1,4,5],"6":[5,7],"5":[4,3,6,7],"0":[1,7],"7":[6,5,1,0],"2":[1,3]},"siteScores":{"4":{"1":9,"5":1},"1":{"1":0,"5":9},"3":{"1":1,"5":1},"6":{"1":16,"5":1},"5":{"1":9,"5":0},"0":{"1":1,"5":25},"7":{"1":1,"5":1},"2":{"1":1,"5":9}},"punter":1,"punters":2}}"""
        val resp = Gson().fromJson(response, MovesResponse::class.java)
    }

    @Test
    fun generalResponseSerialization() {
        val responseJSON = """{"move":{"moves":[{"claim":{"punter":0,"source":3,"target":5}},{"pass":{"punter":1}}]},"state":{"ownedRivers":[],"unownedRivers":[{"source":3,"target":4},{"source":0,"target":1},{"source":2,"target":3},{"source":1,"target":3},{"source":5,"target":6},{"source":4,"target":5},{"source":3,"target":5},{"source":6,"target":7},{"source":5,"target":7},{"source":1,"target":7},{"source":0,"target":7},{"source":1,"target":2}],"myRivers":[],"mines":[1,5],"sitesReachedForMine":{"1":[],"5":[]},"riversForSite":{"4":[{"source":3,"target":4},{"source":4,"target":5}],"1":[{"source":0,"target":1},{"source":1,"target":3},{"source":1,"target":7},{"source":1,"target":2}],"3":[{"source":3,"target":4},{"source":2,"target":3},{"source":1,"target":3},{"source":3,"target":5}],"6":[{"source":5,"target":6},{"source":6,"target":7}],"5":[{"source":5,"target":6},{"source":4,"target":5},{"source":3,"target":5},{"source":5,"target":7}],"0":[{"source":0,"target":1},{"source":0,"target":7}],"7":[{"source":6,"target":7},{"source":5,"target":7},{"source":1,"target":7},{"source":0,"target":7}],"2":[{"source":2,"target":3},{"source":1,"target":2}]},"sitesForSite":{"4":[3,5],"1":[0,3,7,2],"3":[2,1,4,5],"6":[5,7],"5":[4,3,6,7],"0":[1,7],"7":[6,5,1,0],"2":[1,3]},"siteScores":{"4":{"1":9,"5":1},"1":{"1":0,"5":9},"3":{"1":1,"5":1},"6":{"1":16,"5":1},"5":{"1":9,"5":0},"0":{"1":1,"5":25},"7":{"1":1,"5":1},"2":{"1":1,"5":9}},"punter":1,"punters":2}}"""
        val generalType = object : TypeToken<LinkedTreeMap<String, Any>>() {}.type
        val response: LinkedTreeMap<String, Any> = Gson().fromJson<LinkedTreeMap<String, Any>>(responseJSON, generalType)
        val move = response.get("move")
        assert(move != null)
    }
}
