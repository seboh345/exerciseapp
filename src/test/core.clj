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

(comment
  " Nästa steg: Skapa användartabell (USER) med pk och användarnamn
  pk är primarykey unikt id har inget - tänk sqlnummret - tänkpersonnummer, du kan byta namn och annat men ej nummer
  pk kan ha auto-increment
  användarnamn kan vara varchar [50]


  med tabell definerar vi en databastabell

  Sen vill vi ha:
  en permissionstabell -ska heta  USER_ROLE
  Ska ha 2 fält, user-pk referens till användaren och ett fält för role  -varchar [50] tex admin eller rektor
    user-pk ska vara en " foreignkey " med samma typ som pk


  alla kolumnnamn ska vara UPPERCASE

  tricky med permissions - grova eller granulära permissions. Det blir alltid fel  : D




  Nästa steg:
  funktioner vi behöveR:

  1. Funktion för att Lägga till användare -- OK
  2. Funktion för att Lägga till roller på användare  (ska kunna ta bort) -- Kan lägga till roll, kan hitta användare?
  3. Funktion som tar ett Username och en ROLE och ger (has-role?) boolean beroende på access
  4. -----"

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



  ;(get @sessions (str/lower-case THESTRING))

  ;(keys @sessions)

  ;str/trim ;Skit i detta : D bar bort spaces i börjar och efter
  (add-user "Carl")
  (add-role "Anders" "Avloppstekniker")

  )

(defn add-user
  "takes req and adds user in the USER table"
  [tempname]

  (jdbc/execute! ds ["
  insert into USER(USERNAME)
    values(?)" tempname])
  )

(defn has-role?
  [username ROLE]


  )

;;;;;;;;
;; 3. Funktion som tar ett Username och en ROLE och ger (has-role?) boolean beroende på access
;;nedan funkar FUNKAR kollade i DBeaver
;;;;;;;;

(defn add-role
  [username temprole]

  ;;Sök efter username
  (let [currentpk
        ;;Hitta PK
        (get (first (jdbc/execute! ds ["SELECT * FROM USER WHERE USERNAME = ?" username])) :USER/PK)
        alreadyhasrole
        (jdbc/execute! ds ["SELECT * FROM USER_ROLE WHERE USER_PK = ? AND ROLE = ?" currentpk temprole])]

    (jdbc/execute! ds ["
    insert into USER_ROLE(USER_PK, ROLE)
    values(?,?)" currentpk temprole])
    ;;Insert ROLE med matachnde PK i USER_role (sätt pk och role här)
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



(comment

  (delete-role "Bum" "Mugger")

  (add-role "bum" "Mugger")                                 ;;Funkar ofc bara när vi lagt till värden först : ) så testname1,23 finns att testa på so far!

  )

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


  ;;;SYNTAX FÖR POSTRESQL <- använd sen!
  ;;Creates the permissiontable
  (jdbc/execute! ds ["
  create table USER_ROLE (
  USER_PK int,
  ROLE varchar(50),
  CONSTRAINT FK_USER
    FOREIGN KEY(USER_PK)
    REFERENCES USER(PK))"])





  ;;;;OJOJ not in prod pils OJOJ
  (jdbc/execute! ds ["
  drop table USER
    "])

  (jdbc/execute! ds ["
  drop table USER_ROLE
    "])


  "















    Nästa steg: Skapa användartabell (USER) med pk och användarnamn\n  pk är primarykey unikt id har inget - tänk sqlnummret - tänkpersonnummer, du kan byta namn och annat men ej nummer\n  pk kan ha auto-increment\n  användarnamn kan vara varchar [50]\n\n\n  med tabell definerar vi en databastabell\n\n  Sen vill vi ha:\n  en permissionstabell -ska heta  USER_ROLE\n  Ska ha 2 fält, user-pk referens till användaren och ett fält för role  -varchar [50] tex admin eller rektor\n  user-pk ska vara en \"foreignkey\" med samma typ som pk\n\n\n  alla kolumnnamn ska vara UPPERCASE\n\n  tricky med permissions - grova eller granulära permissions. Det blir alltid fel  : D



    "


  ;;Inserts a demo-value in the table, (already in the table)
  (jdbc/execute! ds ["
  insert into todo(task,taskcomplete)
  values('Your notes will be saved here!', false)"]))