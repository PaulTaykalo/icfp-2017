package icfp;

import java.util.*;
import clojure.java.api.Clojure;
import clojure.lang.IFn;
import java.util.function.Function;

public class Api {

    static IFn require;

    static {
        IFn require = Clojure.var("clojure.core", "require");
        require.invoke(Clojure.read("icfp.server.server"));
    }

    public static void gameLoop(String jsonMapString, List<Function<String, String>> punters) {
        IFn fn = Clojure.var("icfp.server.server", "game-loop");
        fn.invoke(jsonMapString, punters);
    }

    public static Function<String, String> randomPunter(int punterId) {
        IFn fn = Clojure.var("icfp.server.server", "random-punter");
        return (Function<String, String>)fn.invoke(punterId);
    }
}
