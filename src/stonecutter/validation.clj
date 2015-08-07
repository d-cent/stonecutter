(ns stonecutter.validation
  (:require [clojure.string :as s]))

(def email-max-length 254)

(def password-min-length 8)

(def password-max-length 254)

(defn remove-nil-values [m]
  (->> m
       (remove (comp nil? second))
       (into {})))

(defn is-email-valid? [email]
  (when email
    (re-matches #"\b[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]+\b" email)))

(defn is-too-long? [string max-length]
  (> (count string) max-length))

(defn is-too-short? [string min-length]
  (< (count string) min-length))

(defn validate-registration-email [email is-duplicate-user-fn]
  (cond (is-too-long? email email-max-length) :too-long
        (not (is-email-valid? email)) :invalid
        (is-duplicate-user-fn email) :duplicate
        :default nil))

(defn validate-sign-in-email [email]
  (cond (is-too-long? email email-max-length) :too-long
        (not (is-email-valid? email)) :invalid
        :default nil))

(defn validate-password [password]
  (cond (s/blank? password) :blank
        (is-too-long? password password-max-length) :too-long
        (is-too-short? password password-min-length) :too-short
        :default nil))

(defn validate-passwords-match [password-1 password-2]
  (if (= password-1 password-2)
    nil
    :invalid))

(defn validate-passwords-are-different [password-1 password-2]
  (if (= password-1 password-2)
    :unchanged
    nil))

(defn registration-validations [params is-duplicate-user-fn]
  (let [{:keys [email password confirm-password]} params]
    {:email            (validate-registration-email email is-duplicate-user-fn)
     :password         (validate-password password)
     :confirm-password (validate-passwords-match password confirm-password)}))

(defn validate-registration [params duplicate-user-fn]
  (->> (registration-validations params duplicate-user-fn)
        remove-nil-values))

(defn sign-in-validations [params]
  (let [{:keys [email password]} params]
    {:email     (validate-sign-in-email email)
     :password  (validate-password password)}))

(defn validate-sign-in [params]
  (->> (sign-in-validations params)
       remove-nil-values))

(defn change-password-validations [params]
  (let [{:keys [current-password new-password confirm-new-password]} params]
    {:current-password     (validate-password current-password)
     :new-password         (or (validate-password new-password)
                               (validate-passwords-are-different current-password new-password))
     :confirm-new-password (validate-passwords-match new-password confirm-new-password)}))

(defn validate-change-password [params]
  (->> (change-password-validations params)
       remove-nil-values))

(defn validate-forgotten-password [params]
  (let [email (:email params)]
    (-> {:email (validate-sign-in-email email)}
        remove-nil-values)))
