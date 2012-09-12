(defproject RemoteDataSourceGenerator "0.1.0-SNAPSHOT"
  :description "Generates N remote datasources with randomly-generated schemas and data."
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [postgresql/postgresql "9.0-801.jdbc4"]
                 [lobos "1.0.0-SNAPSHOT"]
                 [korma "0.3.0-beta9"]
                 [log4j "1.2.15" :exclusions [javax.mail/mail
                            javax.jms/jms
                            com.sun.jdmk/jmxtools
                            com.sun.jmx/jmxri]]])
