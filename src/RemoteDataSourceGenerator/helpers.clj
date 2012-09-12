(ns RemoteDataSourceGenerator.helpers
  (:refer-clojure :exclude [alter drop time boolean float bigint fouble char double])
  (:require (lobos [schema :as schema] [connectivity as :conn] [core :as core])))
  
(defmacro with-resource
  [binding & body]
  `(let ~binding 
     (try 
       (do ~@body)
     (catch Exception e# (.getMessage e#))
     (finally (.close ~(binding 0)))))) 

(defmacro make-2-fn [function first-arg] 
  `(fn [& args#] 
    (eval `(~'~function ~'~first-arg ~@args#))))

(defmulti one-of (fn [x] (map? x)))
(defmethod one-of true [x] 
  ((one-of (vec (keys x))) x))
(defmethod one-of false [x]
  (let [coll (vec x)]
    (coll (rand-int (count coll)))))
  
(defn surrogate-key [table]
  (schema/integer table :id :auto-inc :primary-key))

(defn datetime-tracked [table]
  (-> table
      (schema/timestamp :updated_on)
      (schema/timestamp :created_on (schema/default (now)))))

(defmacro tbl [name & elements]
  `(-> (schema/table ~name
         (surrogate-key)
         (datetime-tracked))
       ~@elements))