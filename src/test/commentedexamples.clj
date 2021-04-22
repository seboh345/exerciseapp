(ns test.commentedexamples)


(comment
  (:USER_ROLE/ROLE (first [#:USER_ROLE{:USER_PK 2, :ROLE "Brandman"} #:USER_ROLE{:USER_PK 2, :ROLE "Bagare"}]))


  (map :USER_ROLE/ROLE #:USER_ROLE{:USER_PK 2, :ROLE "Brandman"})
  (map identity #:USER_ROLE{:USER_PK 2, :ROLE "Brandman"})

  ;;Enskilda fallet vs lista av flera... kolla för ref
  (:USER_ROLE/ROLE #:USER_ROLE{:USER_PK 2, :ROLE "Brandman"})
  (map :USER_ROLE/ROLE [#:USER_ROLE{:USER_PK 2, :ROLE "Brandman"} #:USER_ROLE{:USER_PK 2, :ROLE "Bagare"}])

  (jdbc/execute! ds ["SELECT * FROM USER_ROLE WHERE USER_PK = ?" 2])
  (list-user-roles (roles username))
  (roles "Anders")                                          ;;snackar med db


  ;;Alltid när vi vill kagga med VECTORer MF(f)R.
  (map (fn [v] (+ v v)) [1 2 3])
  (filter (fn [v] (= v 2)) [1 2 3])
  (first)
  (reduce (fn [acc v] (+ acc v)) 0 [1 2 3])


  (reduce (fn [acc v]
            (println acc " - " v)
            acc) [1 2 3])                                   ;; first
  (reduce (fn [acc v] v) [1 2 3])                           ;; last

  )



;;Smidiga grejer för att testa
;;Addroles osv längst ner
(comment
  (jdbc/execute! user/ds ["SELECT * FROM USER_ROLE"])
  (jdbc/execute! user/ds ["SELECT * FROM USER"])

  (delete-role "Bengan" "Bagare")
  (add-role "Bertil" "Bagare")

  (has-role? "Anders" "Avloppstekniker")
  (has-role? "Bertil" "Bagare")
  (has-role? "Bertil" "Brandman")
  (has-role? "Bertil" "Bankrånare")
  )



(comment
  (list-users (jdbc/execute! ds ["SELECT * FROM USER"]))
  ;;I länken använd PK, "vi vet alltid", men visa användarnamnet
  )


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





(comment

  (if (= ROLE userrole)
    true
    false)



  (jdbc/execute-one! ds ["SELECT * FROM USER_ROLE WHERE USER_PK = ? AND ROLE = ?" 2 "Bagare"])

  (#{1 2 3} 123)
  )




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