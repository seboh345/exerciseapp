(ns test.userview
  (:require [org.httpkit.server :as server]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [hiccup.core :refer [html]]
            [next.jdbc :as jdbc]
            [clojure.pprint :refer :all]
            [ring.middleware.multipart-params :as p]
            [clojure.string :as str]
            [clojure.edn :as edn]
            [clojure.set :as cljset]
            [test.user :as user]))

(defn list-users
  [users]
  [:ul
   (for [u users
         ;;:let [_ (println u)]
         ]
     [:li [:a {:href (str "/usermanagement/" (:USER/PK u))} (:USER/USERNAME u)]])])

(defn test-html
  [userid role]
  (html
    [:form
     {:method  "post"
      :action  (str "/usermanagement/add-organisation-to-role/" userid "/" role)
      :enctype "multipart/form-data"}
     [:label {:for "#input5"} "Add organisation to role (integer): "]
     [:input
      {:type "text"
       :id   "input5"
       :name "input5"}]
     [:input
      {:type  "submit"
       :value "Save"}]]
    ;;input5 motsvarar organisation
    ))

(defn list-user-roles
  [roles userpk]
  [:ul
   (map (fn [r]
          [:li [:a {:href (str "/usermanagement/remove-role/" userpk "/" r)} r]
           (test-html userid r)
           ])
        roles)])

;;"Bara lös skiten med fulhack, generalisera sen?"
;;Ska ha in nuvarande roll och userid

;(pprint roles)
; (pprint userpk)
;;Vårt form hänger direkt på en roll. Så vi borde ha role-id. Då behöver vi skicka in vår org-pk till route

(defn active-roles
  [userpk]
  (list-user-roles (user/roles (edn/read-string userpk))
                   userpk))

(defn available-roles
  [userpk]
  (list-user-roles (cljset/difference (user/all-roles)
                                      (user/roles (edn/read-string userpk)))
                   userpk))

(defn display-email
  [userdata userid]
  (html
    "E-post till nuvarande användare: "
    (:USER/EMAIL userdata)
    [:br]
    [:form
     {:method  "post"
      :action  (str "/usermanagement/add-mail/" userid)
      :enctype "multipart/form-data"}                       ;;Change encoding to multipart/form-data
     [:label {:for "#input4"} "Type your e-mail:"]
     [:input
      {:type "text"
       :id   "input4"
       :name "input4"}]
     [:input
      {:type  "submit"
       :value "Save"}]]))

(defn display-status
  [userdata userid]
  (html
    "User is active? (true/false): "
    (if (:USER/ACTIVE userdata)
      "User is Active"
      "User is Inactive")
    [:br]
    "Swap user status"
    [:a {:href (str "/usermanagement/swap-status/" userid)} " Here"]
    [:br]))

(defn organisation-html
  [userdata userid]
  (html
    [:h2 "Add an organisation to your user below!"]
    [:form
     {:method  "post"
      :action  (str "/usermanagement/add-organisation-to-role/" userid)
      :enctype "multipart/form-data"}
     [:label {:for "#input5"} "Type the new organisation (integer): "]
     [:input
      {:type "text"
       :id   "input5"
       :name "input5"}]
     [:input
      {:type  "submit"
       :value "Save"}]]

  ;;Organisationerna får bara vara INTegers ( testa integer?)
    ))


(defn user-handler
  [req]
  (def userid
    (get-in req [:params :id]))
  (def userdata
    (user/user userid))
  (html
    [:div
     [:h1 "Current user " (:USER/USERNAME userdata)]
     [:br]
     "Aktiva roller på nuvarande användare: "
     (active-roles userid)
     [:br]
     "Samtliga tillgängliga roller (som nuvarande använder totalt och helt fullständigt saknar kompetens för): "
     [:br]
     (available-roles userid)
     [:br]
     (display-email userdata userid)
     (display-status userdata userid)
     (organisation-html userdata userid)
     ]))

(defn sessionhtml [sessions req]
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
    "Active sessions:" [:br]
    sessions [:br]
    ))

(defn user-html
  [req]
  (html
    [:h2 "Add a user below!"]
    [:form
     {:method  "post"
      :action  "/adduseroffice"
      :enctype "multipart/form-data"}
     [:label {:for "#input3"} "Type the new user: "]
     [:input
      {:type "text"
       :id   "input3"
       :name "input3"}]
     [:input
      {:type  "submit"
       :value "Save"}]]
    "All users:" [:br]
    (list-users (jdbc/execute! user/ds ["SELECT * FROM USER ORDER BY USERNAME"])) [:br]
    ))

(comment


  (jdbc/execute! user/ds ["SELECT * FROM USER"])
  (jdbc/execute! user/ds ["SELECT * FROM USER_ROLE"])
  (jdbc/execute! user/ds ["SELECT * FROM ROLE"])
  (jdbc/execute! user/ds ["SELECT * FROM ORGANISATION"])
  )


