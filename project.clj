(defproject MapperBot "1.0.0-SNAPSHOT"
  :description "Simple IRC bot"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [overtone/at-at "1.0.0"]
                 [seesaw "1.4.1"]
                 [com.github.insubstantial/substance "7.1"]]
  :dev-dependencies [[lein-eclipse "1.0.0"]]
  :aot [mapper.core]
  :main mapper.core)