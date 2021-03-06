(ns mapper.connection
  "Functions for connecting to an IRC server."
  (:use [mapper.routing :only [dispatch-command]]
  	[overtone.at-at]))

;; todo: automatically generate names when there's a name clash
(def NICK "MapBot5x10")
(def USER-NAME "MapBot")
(def HOST-NAME "MapBot")
(def SERVER-NAME "MapBot")
(def REAL-NAME "A bot for displaying maps")

(def my-pool (mk-pool))

(defn open-socket [host port]
  (let [socket (java.net.Socket. host port)
        from-server (java.io.BufferedReader.
            (java.io.InputStreamReader. (.getInputStream socket)))
        to-server (java.io.PrintWriter. (.getOutputStream socket))]
    {:from-server from-server :to-server to-server}))

;; todo: format message automatically rather than requiring use of 'format
(defn send-to-server [server-connection raw-message]
  (let [to-server (:to-server server-connection)]
    (.print to-server (format "%s\r\n" raw-message))
    (println (str "!!!RAW!!!   " raw-message))
    (.flush to-server)))

(defn connect-to-server [host port]
  (let [server-connection (open-socket host port)]
    (send-to-server server-connection (format "NICK %s" NICK))
    (send-to-server server-connection
                    (format "USER %s %s %s :%s"
                            USER-NAME HOST-NAME SERVER-NAME REAL-NAME))
    server-connection))

(defn disconnect-from-server [server-connection]
  (.close (:from-server server-connection)))

(defn join-channel [server-connection channel]
  (send-to-server server-connection (format "JOIN %s" channel)) "[JOINED]")

;; todo: nicely separate out raw functions from chat functions
(defn answer-ping [message]
  "Respond to a ping from the IRC server so we don't get disconnected."
  (format "PONG%s" (.substring message 4)))

(defn channel-message? [raw-message]
  "Return true if RAW-MESSAGE is a message from a channel."
  (boolean (re-find #".*?!.*? PRIVMSG" raw-message)))

;; todo: proper logging
(defn server-event-loop [server-connection channel]
  "Join CHANNEL, and respond to the users there."
  ;; todo: this shouldn't be part of the event loop
  (at (+ 15000 (now)) #(println (join-channel server-connection channel)) my-pool)
  (while true
    (let [message (.readLine (:from-server server-connection))]
      (do
        (if (not (nil? message))
          (println message))
        (cond
         ;; FIXME: PINGs aren't channel specific
         (.startsWith message "PING")
         (send-to-server server-connection (answer-ping message))
         ;; todo: handle direct messages too
         (channel-message? message)
         (dispatch-command server-connection message)
         )))))
