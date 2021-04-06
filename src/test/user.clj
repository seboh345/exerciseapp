(ns test.user
  (:require [org.httpkit.server :as server]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [hiccup.core :refer [html]]
            [next.jdbc :as jdbc]
            [clojure.pprint :refer :all]
            [ring.middleware.multipart-params :as p]
            [clojure.string :as str]
            [clojure.edn :as edn]))

(def db {:dbtype "h2" :dbname "todo"})                      ;Define db as typ h2 name todo
(def ds (jdbc/get-datasource db))                           ;Set ds as our db

(defn userid-to-pk
  "Takes username and returns PK"
  [name]
  (get (jdbc/execute-one! ds ["SELECT * FROM USER WHERE USERNAME = ?" name]) :USER/PK))
;;=> 1 (en int dvs)

(defn has-role?
  [username ROLE]
  (let [currentpk
        (get (first (jdbc/execute! ds ["SELECT * FROM USER WHERE USERNAME = ?" username])) :USER/PK)
        userroles
        (get (first (jdbc/execute! ds ["SELECT * FROM USER_ROLE WHERE USER_PK = ? AND ROLE = ?" currentpk ROLE])) :USER_ROLE/ROLE)
        ]
    (if (= userroles ROLE)
      true
      false)))

(defn add-user
  "takes req and adds user in the USER table"
  [tempname]
  (if-not (= (str/lower-case tempname)
             (get (first (jdbc/execute! ds ["SELECT * FROM USER WHERE USERNAME = ?" (str/lower-case tempname)])) :USER/USERNAME))
    (jdbc/execute! ds ["
    insert into USER(USERNAME)
    values(?)" (str/lower-case tempname)])))

;(get (first (jdbc/execute! ds ["SELECT * FROM USER WHERE USERNAME = ?" "Anders"])) :USER/USERNAME)

(defn add-role
  [username temprole]
  ;;Sök efter username
  (let [currentpk
        ;;Hitta PK
        (get (first (jdbc/execute! ds ["SELECT * FROM USER WHERE USERNAME = ?" (str/lower-case username)])) :USER/PK)]
    (jdbc/execute! ds ["
    insert into USER_ROLE(USER_PK, ROLE)
    values(?,?)" currentpk (str/lower-case temprole)])))

(defn add-master-role
  [role]
  (jdbc/execute! ds ["
    insert into ROLE(ROLE)
    values(?)" (str/lower-case role)]))

(defn username
  [pk]
  (get (first (jdbc/execute! ds ["SELECT * FROM USER WHERE PK = ?" pk])) :USER/USERNAME))

(defn roles
  [username-or-id]
  (let [currentpk (if (string? username-or-id)
                    (get (first (jdbc/execute! ds ["SELECT * FROM USER WHERE USERNAME = ?" username-or-id])) :USER/PK)
                    username-or-id)
        userroles
        (jdbc/execute! ds ["SELECT * FROM USER_ROLE WHERE USER_PK = ?" currentpk])
        ]
    (apply sorted-set (map :USER_ROLE/ROLE userroles))))
;;=> ("Bagare" "Brandman")

(defn delete-role
  [username temprole]
  ;;Sök efter username
  (let [currentpk
        ;;Hitta PK
        (get (first (jdbc/execute! ds ["SELECT * FROM USER WHERE USERNAME = ?" username])) :USER/PK)]
    (jdbc/execute-one! ds ["DELETE FROM USER_ROLE WHERE USER_PK = ? AND ROLE = ?" currentpk temprole])))


(defn all-roles
  []
  (->> (jdbc/execute! ds ["SELECT * FROM ROLE"])
       (map :ROLE/ROLE)
       (apply sorted-set)
       ))


(comment                                                    ;;Creates the todo-table, (already created)
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

  ;;Creates the ROLE-data table
  (jdbc/execute! ds ["
  create table ROLE (
  ROLE varchar (50),
  )"])


  ;;Links together USER- and ROLE-table
  (jdbc/execute! ds ["
  create table USER_ROLE (
  USER_PK int,
  ROLE varchar(50),
    FOREIGN KEY (USER_PK) REFERENCES USER (PK),
    FOREIGN KEY (ROLE) REFERENCES ROLE (ROLE))"])


  (jdbc/execute! ds ["DELETE FROM ROLE"])
  (jdbc/execute! ds ["SELECT * FROM ROLE"])



  (add-user "Anders")
  (add-role "Anders" "Antiantipasti")

  (add-user "Bertil")
  (add-role "Bertil" "Brandman")

  (add-user "Calle")
  (add-role "Calle" "Countrymusiker")

  (add-user "Hassle Hoff")
  (add-role "Hassle Hoff" "Wallsmasher")


  (add-master-role "Avdelningschef")

  (add-master-role "Administratör")

  (add-master-role "Lärare")

  (add-master-role "Rektor")

  (jdbc/execute! ds ["SELECT * FROM ROLE"])

  (jdbc/execute! ds ["SELECT * FROM USER"])

  (jdbc/execute! ds ["SELECT * FROM USER_ROLE"])

  (all-roles)

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
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


;;;;;;;;;;;;;;;;;;;;;;;;;;;
(comment
  (jdbc/execute! ds ["DROP TABLE USER_ROLE"])
  (jdbc/execute! ds ["DROP TABLE USER"])
  (jdbc/execute! ds ["DROP TABLE ROLE"])
  )