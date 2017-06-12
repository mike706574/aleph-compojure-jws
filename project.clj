(defproject mike/aleph-compojure-jws "0.0.1-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [com.taoensso/timbre "4.8.0"]
                 [com.stuartsierra/component "0.3.1"]
                 [clj-time "0.13.0"]
                 [aleph "0.4.3"]
                 [ring/ring-json "0.3.1"]
                 [buddy/buddy-auth "1.4.1"]
                 [buddy/buddy-hashers "1.2.0"]
                 [buddy/buddy-sign "1.5.0"]
                 [compojure "1.5.2"]]
  :source-paths ["src/clj"]
  :test-paths ["test/clj"]
  :profiles {:uberjar {:aot :all
                       :main example.main}
             :dev {:source-paths ["dev"]
                   :target-path "target/dev"
                   :dependencies [[org.clojure/tools.namespace "0.2.11"]
                                  [org.clojure/data.json "0.2.6"]]}}
  :repl-options {:init-ns user})
