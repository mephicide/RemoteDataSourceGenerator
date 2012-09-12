(ns RemoteDataSourceGenerator.Test
  (:require [RemoteDataSourceGenerator.helpers :as helpers]))

(defn figure-out-macro
  [one-var]
  (apply (helpers/make-2-fn str one-var) " dood"))

