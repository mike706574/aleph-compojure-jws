(ns example.system-test
  (:require [example.system :as system]
            [aleph.http :as http]
            [clojure.test :refer [deftest testing is]]
            [clojure.data.json :as json]
            [com.stuartsierra.component :as component]))

(def config {:id "example" :port 8003})

(defmacro with-system
  [& body]
  (let [port (:port config)]
    `(let [~'system (component/start-system (system/system config))
           ~'url (str "http://localhost:" ~port)]
       (try
         ~@body
         (finally (component/stop-system ~'system))))))

(defn token
  [url username password]
  (let [body (json/write-str {:username username
                              :password password})
        response @(http/post (str url "/tokens")
                             {:body body
                              :throw-exceptions false})]
    (if (= (:status response) 201)
      (update response :body slurp)
      response)))

(defn root
  [url token]
  (let [response @(http/get url {:throw-exceptions false
                                 :headers {"Authorization" (str "Token " token)}})]
    (if (= (:status response) 200)
      (update response :body #(-> % slurp (json/read-str :key-fn keyword)))
      response)))

(deftest auth
  (with-system
    (let [{status :status
           token :body} (token url "mike" "robot")]
      (is (= 201 status))
      (is (string? token))
      (let [{:keys [status body]} (root url token)]
        (is (= 200 status))
        (is (= {:message "Hello, mike!"} body))))))

(deftest bad-username
  (with-system
    (let [{status :status
           token :body} (token url "kljkral" "password")]
      (is (= 401 status)))))

(deftest bad-password
  (with-system
    (let [{status :status
           token :body} (token url "mike" "opqwqpoj")]
      (is (= 401 status)))))
