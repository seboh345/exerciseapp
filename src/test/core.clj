(ns test.core
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
            [test.userview :as userview]
            [test.user :as user]
            [test.todo :as testtodo]))

(def sessions (atom {}))
;; exexmpel på hur den ser ut när någon är inloggad:
;;@sessions                                                   ;;Ger oss :val{}
;;=> {"jona" {:session-start #inst "2021-03-30 12:00:00"}}

(defn main-handler [req]
  (html
    [:div
     [:h1 "This is a big header!"]
     (testtodo/mainlisthtml req)
     (userview/sessionhtml @sessions req)
     ]))

(defn session-handler [req]
  ;(println (get-in req [:params "input2"]))
  (swap! sessions (fn [ss] (assoc ss (str/lower-case (get-in req [:params "input2"]))
                                     {:timeanddate (java.util.Date.)})))
  (main-handler req))

(defn mail-handler [req]
  (testtodo/savetodoindb (get-in req [:params "input1"]))   ;;Here we get the input1 params in the returned map from and save a new entry
  {:status  200
   :headers {"Content-Type" "text/json"}                    ;(1)
   :body    ""}
  (main-handler req))

(defn delete-handler [req]
  (testtodo/removetask (get-in req [:params :id]))          ;
  {:status  200
   :headers {"Content-Type" "text/json"}                    ;(1)
   :body    ""}
  (main-handler req))

(defn remove-role-handler [req]
  ;;:id/:role
  (def tempusername
    (user/username (get-in req [:params :id])))
  (def temprole
    (get-in req [:params :role]))

  ;(println (get-in req [:params :id]))
  ;(println (get-in req [:params :role]))
  (if (user/has-role? tempusername temprole)
    (user/delete-role tempusername temprole)
    (user/add-role tempusername temprole))

  {:status  200
   :headers {"Content-Type" "text/json"}                    ;(1)
   :body    ""}
  (userview/user-handler req))

(defroutes app-routes                                       ;(3)  ;;Here we define our routes
           (GET "/" [] main-handler)
           (GET "/remove/:id" [] delete-handler)            ;;GET när vi hämtar adress? isch? google
           (POST "/postoffice" [] mail-handler)             ;;POST när vi skickar formulär etc
           (POST "/sessionoffice" [] session-handler)
           (GET "/usermanagement/:id" [] userview/user-handler)
           (GET "/usermanagement/remove-role/:id/:role" [] remove-role-handler)
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
  (user/add-user "David")
  (user/add-role "David" "Andlig vägledare")
  )




