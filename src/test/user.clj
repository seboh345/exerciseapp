(ns test.user)



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


(defn username
  [pk]
  (get (first (jdbc/execute! ds ["SELECT * FROM USER WHERE PK = ?" pk])) :USER/USERNAME)
  )

(defn has-role?
  [username ROLE]

  (let [currentpk
        (get (first (jdbc/execute! ds ["SELECT * FROM USER WHERE USERNAME = ?" username])) :USER/PK)
        userroles
        (get (first (jdbc/execute! ds ["SELECT * FROM USER_ROLE WHERE USER_PK = ? AND ROLE = ?" currentpk ROLE])) :USER_ROLE/ROLE)
        ]
    (if (= userroles ROLE)
      true
      false))
  )

