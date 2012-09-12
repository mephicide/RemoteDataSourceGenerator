(ns RemoteDataSourceGenerator.core
  (:refer-clojure :exclude [alter drop time boolean float bigint fouble char double])
	(:use [RemoteDataSourceGenerator.helpers :as helpers]
           [korma.core :as korma-core]
           [korma.db :as korma-db]
           (lobos [schema :as schema] [connectivity :as conn] [core :as lobos-core])))

(use '(lobos schema connectivity core))
(import 'java.lang.Exception)
(import 'java.sql.Connection)
(import 'java.sql.DriverManager)
(import 'java.sql.PreparedStatement)

(def postgres-driver "org.postgresql.Driver")
(def postgres-admin-user "postgres")
(def postgres-admin-password "bald")
(def test-db-prefix "test_db_")
(def main-postgres-url "jdbc:postgresql://localhost/")

(def db-descriptions {:postgres {:classname postgres-driver
														      :subprotocol "postgresql"
														      :user postgres-admin-user
														      :password postgres-admin-password
														      :subname main-postgres-url}})
(def column-types {:simple #{integer} :complex #{varchar}})
(def complex-values {varchar 100})

(defn make-subname
  [current db-num]
 (str  (.substring current (+ (.lastIndexOf current ":") 1)) test-db-prefix db-num))

(defn create-db 
  [db-number descr]
  (let [db-name (str test-db-prefix db-number)]
		(do (Class/forName (descr :classname))
		  (helpers/with-resource [conn (DriverManager/getConnection (descr :subname) (descr :user) (descr :password))]
		    (helpers/with-resource [stmt (.createStatement conn)]
		      (.executeUpdate stmt (str "CREATE DATABASE " db-name))
		        (println (descr :subprotocol) " database " db-name " created!"))))))

(defn make-columns 
  [max]
  (let [true-max (Math/max (rand-int max) 1)]
    (reverse
      (loop [x 0 sequence '()]
		     (if (= x true-max) sequence
		       (let [col-type (helpers/one-of (helpers/one-of column-types))] 
		         (recur (inc x) (cons (filter #(not (nil? %)) 
		                                 (list 
		                                   col-type
		                                   (keyword (str "column_" x)) 
		                                   (if ((column-types :complex) col-type) (complex-values col-type))));might be nil
		                              sequence))))))))

(defn make-db-values
  [cols]
  (apply assoc (cons {} (interleave cols (range 1 (+ (count cols) 1))))))

(defn add-facts-to-db 
  [db-name table-name columns max-rows descr]
  (let [column-names (map #(nth % 1) columns)]
		 (do
		    (korma-db/defdb the-db descr)
		    (korma-core/defentity this-table 
		                         (korma-core/table table-name)
		                         (apply korma-core/entity-fields column-names))
		    (dotimes [n max-rows] 
		      (korma-core/insert this-table
		                     (korma-core/values (make-db-values real-columns)))))))

(defn populate-db
  [db-number max-tables max-table-width max-rows descr]
  (do
    (try 
      (let [new-db-name (make-subname (descr :subname) db-number)
            new-db  (assoc descr :subname new-db-name)] 
	        (conn/open-global new-db)
	          (dotimes [n max-tables] 
	            (let [columns (make-columns max-table-width)
                   table-name (keyword (str "table_" n))]
		            (do 
                  (lobos-core/create 
	                   (apply (helpers/make-2-fn helpers/tbl table-name) columns))
                 
		              (add-facts-to-db new-db table-name columns max-rows descr)))))
    (finally 
        (conn/close-global)))))
  
(defn -main
  "The main entry point."
  [& args]
  (dotimes [n (first args)] 
    (let [descr (helpers/one-of db-descriptions)]
             (do (create-db n descr)
                 (populate-db n (nth args 1) (nth args 2) (nth args 3) descr)))))

(-main 1 1 10 4);;num_dbs, num_tables, max_columns_in_each_table, max_rows_in_each_table 

;(create
;  (tbl :users
 ;   (varchar :name 100))