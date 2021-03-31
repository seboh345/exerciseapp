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

(defn link-user-roles
  [roles]
  [:ul
   (for [r roles
         ;;:let [_ (println u)]
         ]
     [:li [:a {:href (str "/usermanagement/" (:USER_ROLE/USER_PK/PK r))} (:USER_ROLE/ROLE r)]])]
  )


(defn list-user-roles
  [roles]
  [:ul
   (map (fn [r]
          [:li r])
        roles)])
;;=> [:ul [:li "Brandman"] [:li "Bagare"]]

(defn active-roles
  [user]
  (list-user-roles (user/roles (edn/read-string user))))

(defn available-roles
  [user]
  (list-user-roles (cljset/difference (user/all-roles) (user/roles (edn/read-string user)))))

(defn user-handler
  [req]
  ;(pprint (get-in req [:params :id]))

  (def tempuser
    (get-in req [:params :id]))
  (html
    [:div
     [:h1 "Current user " (user/username tempuser)]
     "Aktiva roller på nuvarande användare: "
     (active-roles tempuser)
     [:br]
     "Samtliga tillgängliga roller (som nuvarande använder totalt och helt fullständigt saknar kompetens för): "
     [:br]
     (available-roles tempuser)
     ;;"Samtliga roller på Tillfälligtnamn AB: "
     ;;(list-user-roles (user/all-roles)) [:br]
     ]))

;Länkar så vi kan swappa mellan aktiva och tillgängliga roller



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
    "All users:" [:br]
    (list-users (jdbc/execute! user/ds ["SELECT * FROM USER"])) [:br]))



