;; Copyright (c) Philipp Meier (meier@fnogol.de). All rights reserved.
;; The use and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which
;; can be found in the file epl-v10.html at the root of this distribution. By
;; using this software in any fashion, you are agreeing to be bound by the
;; terms of this license. You must not remove this notice, or any other, from
;; this software.

(ns test
  (:use compojure)
  (:use compojure-rest))

(defn hello-resource [request]
  ((-> (method-not-allowed)
       (wrap-generate-body (fn [r] (str "Hello " ((request :params) :who "stranger"))))
       (wrap-etag (comp :who :params))
       (wrap-expiry (constantly 10000))	
       (wrap-last-modified -1000)
       (wrap-exists (comp not #(some #{%} ["cat"]) :who :params))
       (wrap-auth (comp not #(some #{%} ["evil"]) :who :params))
       (wrap-allow (comp not #(some #{%} ["scott"]) :who :params)))
   request))

(defroutes my-app
  (ANY "/hello/:who" hello-resource)
  (GET "/simple" (str "simple"))
  (GET "/echo/:foo" (fn [req] {:headers { "Content-Type" "text/plain" } :body (str (dissoc req :servlet-request))}))
  (GET "*" (page-not-found)))

(defn main []
  (do
    (defserver test-server {:port 8080} "/*" (servlet my-app))
    (start test-server)))


