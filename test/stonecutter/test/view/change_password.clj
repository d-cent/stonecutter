(ns stonecutter.test.view.change-password
  (:require [midje.sweet :refer :all]
            [net.cgrand.enlive-html :as html]
            [stonecutter.test.view.test-helpers :as th]
            [stonecutter.translation :as t]
            [stonecutter.routes :as r]
            [stonecutter.helper :as helper]
            [stonecutter.view.change-password :refer [change-password-form]]))

(fact "should return some html"
      (let [page (-> (th/create-request)
                     change-password-form)]
        (html/select page [:body]) =not=> empty?))

(fact "work in progress should be removed from page"
      (let [page (-> (th/create-request) change-password-form)]
        page => th/work-in-progress-removed))

(fact "there are no missing translations"
      (let [translator (t/translations-fn t/translation-map)
            page (-> (th/create-request) change-password-form (helper/enlive-response {:translator translator}) :body)]
        page => th/no-untranslated-strings))

(fact "form posts to correct endpoint"
      (let [page (-> (th/create-request) change-password-form)]
        (-> page (html/select [:form]) first :attrs :action) => (r/path :change-password)))

(fact "cancel link should go to correct endpoint"
      (let [page (-> (th/create-request) change-password-form)]
        (-> page (html/select [:.clj--change-password-cancel__link]) first :attrs :href) => (r/path :show-profile)))

(facts "about removing elements when there are no errors"
       (let [page (-> (th/create-request) change-password-form)]
        (fact "no elements have class for styling errors"
            (html/select page [:.form-row--validation-error]) => empty?)
        (fact "validation summary element is removed"
            (html/select page [:.clj--validation-summary]) => empty?)))

(facts "about displaying errors"
       (tabular
         (facts "when current-password is incorrect"
                (let [errors {:current-password ?current-password-error}
                      page (-> (th/create-request {} errors) change-password-form)]
                  (fact "validation-summary--show class is added to the validation summary element"
                        (-> (html/select page [:.clj--validation-summary])
                            first :attrs :class) => (contains "validation-summary--show"))
                  (fact "confirm password validation is present as a validation summary item"
                        (html/select page [:.clj--validation-summary__item]) =not=> empty?)
                  (fact "correct error message is displayed"
                        (html/select page [[:.clj--validation-summary__item (html/attr= :data-l8n "content:change-password-form/current-password-invalid-validation-message")]]) =not=> empty?)))
         ?current-password-error
         :blank
         :too-short
         :too-long
         :invalid))
