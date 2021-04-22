(ns test.todo
  (:gen-class)
  (:require [org.httpkit.server :as server]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [hiccup.core :refer [html]]
            [next.jdbc :as jdbc]
            [clojure.pprint :refer :all]
            [ring.middleware.multipart-params :as p]
            [clojure.string :as str]
            [clojure.edn :as edn]
            [test.user :as user]))



(defn row->li
  "Takes row, adds to ordered list then adds deletebutton in the list"
  [row]
  (def tempstring
    (str "/remove/" (:TODO/ID row))
    )
  [:li (:TODO/TASK row) " " [:a {:href tempstring} "Delete"]])
;;=> [:li "Int från ID" "string from TASK"

(defn gettable1
  "Fetches table from database"
  []
  (jdbc/execute! user/ds ["select * from todo"]))

(defn printtohtmlLI
  "Fetches table, runs row-li on each element, then vectorizes the resulting LazySeq.
  Returns vector that html can read and print easily."
  []
  (vec (map row->li (gettable1))))

(defn removetask
  "Removes entry with corresponding ID in the db"
  [id]
  (jdbc/execute-one! user/ds ["DELETE FROM todo WHERE id = ?" id]))

(defn savetodoindb
  "Takes a string and saves as a new entry in the table"
  [todostring]
  (jdbc/execute! user/ds ["
  insert into todo(task,taskcomplete)
    values(?,?)" todostring false]))

(defn mainlisthtml [req]
  (html
    [:h2 "This is a header for the list!"]
    (vec (concat [:ul] (printtohtmlLI)))                    ;;Concatenates and vectorizes the vector printtohtmlLI with a vector containing unordered list to get html formatting
    [:h2 "You can add notes below!"]
    [:form
     {:method  "post"
      :action  "/postoffice"
      :enctype "multipart/form-data"}                       ;;Change encoding to multipart/form-data
     [:label {:for "#input1"} "Add new notes to the list:"]
     [:input
      {:type "text"
       :id   "input1"
       :name "input1"}]
     [:input
      {:type  "submit"
       :value "Save"}]]))



