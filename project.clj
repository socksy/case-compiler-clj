(defproject case "0.1.0-SNAPSHOT"
  :description "A compiler for the Case language (not a parser)"
  :url "http://hackage.haskell.org/package/HaRe-0.7.0.2/docs/src/Language-Haskell-Refact-Case.html"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]]
  :main case.core
  :profiles {:uberjar {:aot :all}})
