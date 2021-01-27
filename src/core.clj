(ns unicorn.core
  (:gen-class)
  (:require [org.httpkit.server :as server]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [hiccup.core :refer [html]]
            [next.jdbc :as jdbc]
            [clojure.pprint :refer :all] 
            [ring.middleware.multipart-params :as p])) 

(def db {:dbtype "h2" :dbname "todo"}) ;Define db as typ h2 name todo
(def ds (jdbc/get-datasource db)) ;Set ds as our db

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
(jdbc/execute! ds ["select * from todo"]))

(defn printtohtmlLI
  "Fetches table, runs row-li on each element, then vectorizes the resulting LazySeq.
  Returns vector that html can read and print easily."
  []
    (vec(map row->li (gettable1))))

(defn removetask
  "Removes entry with corresponding ID in the db"
  [id]
  (jdbc/execute-one! ds ["DELETE FROM todo WHERE id = ?" id]))

(defn savetodoindb
  "Takes a string and saves as a new entry in the table"
  [todostring]
   (jdbc/execute! ds ["
  insert into todo(task,taskcomplete)
    values(?,?)" todostring false]))

(defn main-handler [req]
  (html
    [:div
      [:h1 "This is a big header"]
      [:h2 "This is a header for the list!"]
        (vec(concat [:ul ] (printtohtmlLI))) ;;Concatenates and vectorizes the vector printtohtmlLI with a vector containing unordered list to get html formatting
      [:h2 "You can add notes below!"]
      [:form
        {:method "post"
          :action "/postoffice"
          :enctype "multipart/form-data"} ;;Change encoding to multipart/form-data
        [:label {:for "#input1"} "Add new notes to the list:"]
        [:input
          {:type "text"
          :id "input1"
          :name "input1"}]
        [:input
          {:type "submit"
          :value "Save"}]]]))

(defn mail-handler [req]
  (savetodoindb (get-in req [:params "input1"]))  ;;Here we get the input1 params in the returned map from and save a new entry
    {:status  200
    :headers {"Content-Type" "text/json"} ;(1)
    :body    ""}
  (main-handler req))

(defn delete-handler [req]
  (removetask (get-in req [:params :id]));
    {:status  200
    :headers {"Content-Type" "text/json"} ;(1)
    :body    ""}
  (main-handler req))

(defroutes app-routes ;(3)  ;;Here we define our routes
  (GET "/" [] main-handler)
  (GET "/remove/:id" [] delete-handler) ;;
  (POST "/postoffice" [] mail-handler)
  (route/not-found "Something went wrong! Blame me!")) 

(def app
  (p/wrap-multipart-params app-routes) ;wrap-params on app-routesfunktionen
  )

(defn -main 
  "This is our app's entry point"
  [& args]
  (let [port (Integer/parseInt (or (System/getenv "PORT") "8080"))] 
    (server/run-server #'app {:port port})
    (println (str "Running webserver at http:/127.0.0.1:" port "/"))))

(comment
  (-main) ;;Boot server here

  

  ;;Creates the todo-table, (already created)
  (jdbc/execute! ds ["
  create table todo (
  id int auto_increment primary key,
  task varchar(255),
  taskcomplete boolean)"])

  ;;Inserts a demo-value in the table, (already in the table)
  (jdbc/execute! ds ["
  insert into todo(task,taskcomplete)
  values('Your notes will be saved here!', false)"]))