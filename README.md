# clj-solr

A simple Solr Client

## Usage

```clojure
[clj-solr "1.2"]
```

```clojure
(ns clj-solr-example.core
   (:use clj-solr.core))


(def connection {:username "basic-auth-username"
                 :password "basic-auth-password"
                 :address "localhost:8983"
                 :core "example_core"})
				 
(add-docs connection 
          [{:id "foobar123" :content_t "Lorem Ipsum"}
           {:id "barbaz456" :content_t "dolor sit"}])
		   

(query connection {:q "*:*" :fl "content_t" :indent "true"})

;;    q      ;; query
;;    fq     ;; filter
;;    sort   ;; sort
;;    fl     ;; list of fields
;;    df     ;; default field to search
;;    start  ;; start page
;;    rows   ;; number per row
;;    indent ;; send response with newlines and tabs

```
