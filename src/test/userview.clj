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
            [test.user :as user]))

(defn list-users
  [users]
  [:ul
   (for [u users
         ;;:let [_ (println u)]
         ]
     [:li [:a {:href (str "/usermanagement/" (:USER/PK u))} (:USER/USERNAME u)]
      ])])

(defn list-user-roles
  [roles]
  [:ul
   (map (fn [r]
          [:li r])
        roles)])
;;=> [:ul [:li "Brandman"] [:li "Bagare"]]

(defn user-handler
  [req]
  ;(pprint (get-in req [:params :id]))

  (html
    [:div
     [:h1 "Current user " (user/username (get-in req [:params :id]))]
     "Aktiva roller på nuvarande användare: "
     (list-user-roles (user/roles (edn/read-string (get-in req [:params :id]))))
     ]
    )
  )


;;Bygg #() med samtliga roller, visa vilka som finns på respektive user samt vilka som inte finns



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
    (list-users (jdbc/execute! user/ds ["SELECT * FROM USER"]))
    [:br])
  )



