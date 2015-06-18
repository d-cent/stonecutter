(ns stonecutter.validation)

(defn is-email-valid? [email]
  (re-matches #"\b[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]+\b" email) 
  )

(defn validate-registration [{email :email}]
  (when-not (is-email-valid? email)
    "Email address is invalid"
    )  
  )