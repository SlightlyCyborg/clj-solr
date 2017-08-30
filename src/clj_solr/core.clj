(ns clj-solr.core
  (:require [clj-http.client :as client]
            [cheshire.core :refer :all]
            [clojure.data.xml :refer :all])
  (:use clojure.data.xml))


(defn ?assoc
  "Same as assoc, but skip the assoc if v is nil"
  [m & kvs]
  (->> kvs
    (partition 2)
    (filter second)
    (map vec)
    (into m)))

(defn query

  [connection &
   {:keys [q      ;; query
           fq     ;; filter
           sort   ;; sort
           fl     ;; list of fields
           df     ;; default field to search
           start  ;; start page
           rows   ;; number per row
           indent ;; send response with newlines and tabs
           hl     ;; highlighting?
           hl-fl
           ]
    :or  {q "*:*" indent true}}]

   (-> (client/get (str "http://"
                     (connection :address)
                     "/solr/"
                     (connection :core)
                     "/select")
                {:basic-auth
                 [(connection :username) (connection :password)]

                 :query-params
                 (?assoc {"q"     q}
                         "fq"     fq
                         "sort"   sort
                         "fl"     fl
                         "df"     df
                         "wt"     "json" 
                         "start"  start
                         "hl"     hl
                         "hl.fl"  hl-fl
                         "indent" indent)})
       (:body)
       (parse-string true)
       ((fn [response] {:docs (get-in response [:response :docs])
                        :highlights (get response :highlighting)
                        :num-found (get-in response [:response :numFound])}))))


(def ex-doc-struct
  {:id "1234"
  :data_t "we will win here"})


(defn doc-struct->xml-struct [doc]
  (into []
   (concat
    [:doc]
    (mapv
     (fn [field]
       [:field {:name (name (first field))} (second field)])
     doc))))

(defn docs->xml
  [docs & {:keys [update-method]
           :or {update-method :add}}]
  
  (emit-str 
   (sexp-as-element
    (into []
     (concat [update-method] 
             (doall
              (mapv
               doc-struct->xml-struct
               docs))))))) 


(defn add-docs [connection docs]
  (client/post
   (str "http://"
        (connection :address)
        "/solr/"
        (connection :core)
        "/update")
   {:basic-auth
    [(connection :username) (connection :password)]
    :headers {"Content-Type" "text/xml"}
    :body (docs->xml docs)})

  (client/post
   (str "http://"
        (connection :address)
        "/solr/"
        (connection :core)
        "/update")
   {:basic-auth
    [(connection :username) (connection :password)]
    :headers {"Content-Type" "text/xml"}
    :body (emit-str (sexp-as-element [:commit {:waitSearcher "true" :softCommit "false"}]))}))
