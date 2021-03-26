(ns test.core
  (:gen-class)
  (:require [org.httpkit.server :as server]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [hiccup.core :refer [html]]
            [next.jdbc :as jdbc]
            [clojure.pprint :refer :all]
            [ring.middleware.multipart-params :as p]
            [clojure.string :as str]))

(def db {:dbtype "h2" :dbname "todo"})                      ;Define db as typ h2 name todo
(def ds (jdbc/get-datasource db))                           ;Set ds as our db

(def sessions (atom {}))
;; exexmpel på hur den ser ut när någon är inloggad:
;;@sessions                                                   ;;Ger oss :val{}
;;=> {"jona" {:session-start #inst "2021-03-30 12:00:00"}}


(comment
  ;;

  ;;KEKEdeasd
  (assoc @sessions :a 10)

  @sessions

  (swap! sessions (fn [ss] (assoc ss :a 10)))
  (deref sessions)

  (def v1 @sessions)
  v1
  (swap! sessions (fn [ss] (assoc ss :a 10 :b 20)))

  (def tempsession @sessions)
  v2
  (swap! sessions (fn [ss] (assoc ss :a 10 :b 20 :c 30)))
  ;;"vi kan byta ut värdet / vad sessions kollar på men vi muterar aldrig värdet"

  (reset! sessions {})
  ;;På atoms swap och reset                                         ;;

  ;;Ha som mål att göra något på hemsidan så jag kan förändra sessions -> ska se i repl att sessions har ändrats
  ;;det som fylls i i inputfält ska hamna i sessions

  (assoc-in {} [1 :connections 4] 2)
  (def username "Knauf")

  (def users (assoc-in {} [username :session-start] (java.util.Date.)))

  users

  (assoc-in)

  (def usersession (atom {}))
  (reset! usersession {})

  (swap! usersession (fn [ss] (assoc-in [0] ss :username "Hello")
                       ))
  @usersession
  )

;;Kommentarer nere är massa övrigt
(comment
  (def temporaryrole
    "temporaryrole")

  (jdbc/execute! ds ["
  insert into USER_ROLE(ROLE)
    values(?)" temporaryrole])

  (jdbc/execute! ds ["SELECT * FROM USER_ROLE WHERE ROLE = ?" "temporaryrole"])

  (def temporaryusername
    "temporaryusername")

  (jdbc/execute! ds ["
  insert into USER(USERNAME)
    values(?)" "testname3"])

  (jdbc/execute! ds ["
  insert into USER(USERNAME)
    values(?)" temporaryusername])


  (jdbc/execute! ds ["SELECT * FROM USER WHERE USERNAME = ?" temporaryusername])

  (jdbc/execute! ds ["SELECT * FROM USER_ROLE WHERE ROLE = ?" "Mugger"])
  (jdbc/execute! ds ["SELECT * FROM USER_ROLE WHERE USER_PK = ?" 17])

  (def wordtodelete
    "String here")

  (jdbc/execute-one! ds ["DELETE FROM USER WHERE USERNAME = ?" wordtodelete]) ;; Här deleetar vi på termen i wordtodelete


  ;;;;;Exempelsekvens


  (jdbc/execute! ds ["SELECT * FROM USER WHERE USERNAME = ?" "Bengan"])

  (first (jdbc/execute! ds ["SELECT * FROM USER WHERE USERNAME = ?" temporaryusername]))

  (get (first (jdbc/execute! ds ["SELECT * FROM USER WHERE USERNAME = ?" temporaryusername])) :USER/PK)

  ;;;;;;;;;;;;;;;;;;;
  (comment (println (first (jdbc/execute! ds ["SELECT * FROM USER_ROLE WHERE USER_PK = ?" currentpk])))
           (println currentpk)
           (println userrole)
           (println username))



  ;;;;;;;


  ;(get @sessions (str/lower-case THESTRING))

  ;(keys @sessions)

  ;str/trim ;Skit i detta : D bar bort spaces i börjar och efter
  )
;;Smidiga grejer för att testa
(comment
  ;;Smidiga grejer för att testa
  ;;Addroles osv längst ner
  (jdbc/execute! ds ["SELECT * FROM USER_ROLE"])
  (jdbc/execute! ds ["SELECT * FROM USER"])

  (delete-role "Bengan" "Bagare")
  (add-role "bum" "Mugger")

  (has-role? "Anders" "Avloppstekniker")
  (has-role? "Bertil" "Bagare")
  (has-role? "Bertil" "Brandman")
  )

(defn add-user
  "takes req and adds user in the USER table"
  [tempname]

  (jdbc/execute! ds ["
  insert into USER(USERNAME)
    values(?)" tempname])
  )

(defn add-role
  [username temprole]

  ;;Sök efter username
  (let [currentpk
        ;;Hitta PK
        (get (first (jdbc/execute! ds ["SELECT * FROM USER WHERE USERNAME = ?" username])) :USER/PK)]

    (jdbc/execute! ds ["
    insert into USER_ROLE(USER_PK, ROLE)
    values(?,?)" currentpk temprole])
    ))

(defn has-role?
  [username ROLE]

  (let [currentpk
        (get (first (jdbc/execute! ds ["SELECT * FROM USER WHERE USERNAME = ?" username])) :USER/PK)
        userrole
        (get (first (jdbc/execute! ds ["SELECT * FROM USER_ROLE WHERE USER_PK = ?" currentpk])) :USER_ROLE/ROLE)
        ]

    (if (= ROLE userrole)
      true
      false)
    )

  )

(defn delete-role
  [username temprole]
  ;;Sök efter username
  (let [currentpk
        ;;Hitta PK
        (get (first (jdbc/execute! ds ["SELECT * FROM USER WHERE USERNAME = ?" username])) :USER/PK)
        alreadyhasrole
        (jdbc/execute! ds ["SELECT * FROM USER_ROLE WHERE USER_PK = ? AND ROLE = ?" currentpk temprole])]

    (jdbc/execute-one! ds ["DELETE FROM USER_ROLE WHERE USER_PK = ?" currentpk])))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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
  (vec (map row->li (gettable1))))

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

(defn sessionhtml [req]

  (html
    [:h2 "Save a session below!"]
    [:form
     {:method  "post"
      :action  "/sessionoffice"
      :enctype "multipart/form-data"}                       ;;Change encoding to multipart/form-data
     [:label {:for "#input2"} "Type your username:"]
     [:input
      {:type "text"
       :id   "input2"
       :name "input2"}]
     [:input
      {:type  "submit"
       :value "Save"}]]
    " Atom sessions values:"
    @sessions [:br]
    ;(println (get-in req [:params "input2"]))
    ;(str/lower-case (get-in req [:params "input2"]))

    [:br])

  )


(defn imagetohtml [req]
  (html
    [:h2 "This is a copyright free picture of a random person riding an escalator in a wierd format!"]
    [:img {:src "https://cdn.stocksnap.io/img-thumbs/960w/busy-businessman_UTCYYGKHZT.jpg", :alt "It's a person?", :width "350", :height "200", :style "border:3px solid black"}])
  )

(defn main-handler [req]
  (html
    [:div
     [:h1 "This is a big header!"]
     (mainlisthtml req)
     (sessionhtml req)
     (imagetohtml req)]))



(defn session-handler [req]
  ;(println (get-in req [:params "input2"]))
  (swap! sessions (fn [ss] (assoc ss (str/lower-case (get-in req [:params "input2"]))
                                     {:timeanddate (java.util.Date.)})))
  (main-handler req))

(defn mail-handler [req]
  (savetodoindb (get-in req [:params "input1"]))            ;;Here we get the input1 params in the returned map from and save a new entry
  {:status  200
   :headers {"Content-Type" "text/json"}                    ;(1)
   :body    ""}
  (main-handler req))

(defn delete-handler [req]
  (removetask (get-in req [:params :id]))                   ;
  {:status  200
   :headers {"Content-Type" "text/json"}                    ;(1)
   :body    ""}
  (main-handler req))

(defroutes app-routes                                       ;(3)  ;;Here we define our routes
           (GET "/" [] main-handler)
           (GET "/remove/:id" [] delete-handler)            ;;
           (POST "/postoffice" [] mail-handler)
           (POST "/sessionoffice" [] session-handler)
           (route/not-found "Something went wrong! Blame me!"))

(def app
  (p/wrap-multipart-params app-routes)                      ;wrap-params on app-routesfunktionen
  )

(defn -main
  "This is our app's entry point"
  [& args]
  (let [port (Integer/parseInt (or (System/getenv "PORT") "8080"))]
    (server/run-server #'app {:port port})
    (println (str "Running webserver at http:/127.0.0.1:" port "/"))))

(comment
  (-main)                                                   ;;Boot server here

  ;;Creates the todo-table, (already created)
  (jdbc/execute! ds ["
  create table todo (
  id int auto_increment primary key,
  task varchar(255),
  taskcomplete boolean)"])

  ;;Creates the userdata-tabla
  (jdbc/execute! ds ["
  create table USER (
  PK int auto_increment primary key,
  USERNAME varchar(50))"])

  (jdbc/execute! ds ["
  create table USER_ROLE (
  USER_PK int,
  ROLE varchar(50),
    FOREIGN KEY (USER_PK)
    REFERENCES USER (PK))"])


  (add-user "Anders")
  (add-role "Anders" "Avloppstekniker")

  (add-user "Bertil")
  (add-role "Bertil" "Brandman")

  (add-user "Calle")
  (add-role "Calle" "Countrymusiker")



  )




;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(comment
  ;;;SYNTAX FÖR POSTRESQL <- använd sen!
  ;;Creates the permissiontable
  (jdbc/execute! ds ["
  create table USER_ROLE (
  USER_PK int,
  ROLE varchar(50),
  CONSTRAINT FK_USER
    FOREIGN KEY(USER_PK)
    REFERENCES USER(PK))"])

  )
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;