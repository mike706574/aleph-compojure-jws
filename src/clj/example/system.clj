(ns example.system
  (:require [compojure.route :as route]
            [compojure.core :refer :all]
            [compojure.response :refer [render]]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [example.service :as service]
            [ring.util.response :refer [response redirect content-type]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [clj-time.core :as time]
            [buddy.auth :refer [authenticated? throw-unauthorized]]
            [buddy.auth.backends.token :refer [jws-backend]]
            [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
            [buddy.hashers :as hashers]
            [buddy.sign.jwt :as jwt]))

(def secret "mysupersecret")

(defonce next-id (atom 0))
(defonce users (atom {}))

(defn add-user!
  [username password]
  (swap! users assoc (swap! next-id inc)
         {:username username
          :password (hashers/encrypt password)}))

(add-user! "mike" "robot")

(defn find-user
  [username]
  (when-let [user (first (filter (fn [[user-id user]] (= (:username user) username)) @users))]
    (val user)))

(defn home
  [request]
  (if-not (authenticated? request)
    (throw-unauthorized)
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body (json/write-str {:message (str "Hello, " (-> request :identity :username) "!")})}))

(defn token
  [{:keys [username password]}]
  (when-let [user (find-user username)]
    (when (hashers/check password (:password user))
      (let [claims {:username username
                    :exp (time/plus (time/now) (time/days 1))}
            token (jwt/sign claims secret {:alg :hs512})]
        token))))

(defroutes app
  (GET "/" [] home)
  (POST "/tokens" req
        (let [credentials (-> req
                              :body
                              slurp
                              (json/read-str :key-fn keyword))]
          (if-let [token (token credentials)]
            {:status 201
             :headers {"Content-Type" "text/plain"}
             :body token}
            {:status 401}))))

(def auth-backend (jws-backend {:secret secret :options {:alg :hs512}}))

(defn system [config]
  {:app (service/aleph-service config (-> app
                                          (wrap-authorization auth-backend)
                                          (wrap-authentication auth-backend)))})
