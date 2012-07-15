(ns mapper.core
  (:use seesaw.core)
  (:import org.pushingpixels.substance.api.SubstanceLookAndFeel)
  (:import org.pushingpixels.substance.api.SubstanceConstants$FocusKind)
  (:gen-class)
  (:use [mapper.connection :only [connect-to-server server-event-loop join-channel]])
  (:use [clojure.string :only [split]])
  (:use [mapper.routing :only [say-in-channel]])
)
(def IRC-SERVER "se.quakenet.org")
(def PORT 6667)

(def CHANNEL "#rgrd-ot")
(native!)

(def f (frame :title "IRC Mapper" :on-close :exit :size [600 :by 300]))
(defn display [content]
  (config! f :content content)
  content)
(defn acquire [kw] (select (to-root f) kw))

(comment :listen [
                      :mouse-pressed #(config! % :background "#66F" :foreground "#000")
                      :mouse-entered #(config! % :foreground (shades shade-index))
                      :mouse-exited  #(config! % :foreground lightest)]
                   :action (action :handler (fn [e] (config! (.getSource e) :background "#00F" :foreground lightest) (alert (str "Clicked " phrase)))
                                         :name (deref (defenses phrase)))
)
(defn mb 
  [id inner]
      (text
             :id id
             :editable? true
             :text inner
             :columns 3
             :listen [#{:remove-update :insert-update}  (fn [e] config! (acquire [(keyword (str \# (name id)))]) :text
                                                                  (subs
                                                                    (format "%3s"
                                                                            (config (acquire [(keyword (str \# (name id)))]) :text)) 0 3))]
      ))
(declare get-lines)
(declare say-map)
(defn make-window [connection]
  (border-panel
  :north (grid-panel
    :id :the-grid
    :border [10 10]
    :hgap 1
    :vgap 1
    :columns 10
    :items [(mb :0x0yleft  "JOE") (mb :0x0yright ""   ) (mb :1x0yleft  ""   ) (mb :1x0yright ""   ) (mb :2x0yleft  ""   )
            (mb :2x0yright ""   ) (mb :3x0yleft  ""   ) (mb :3x0yright ""   ) (mb :4x0yleft  ""   ) (mb :4x0yright ""   )
            
            (mb :0x1yleft  ""   ) (mb :0x1yright ""   ) (mb :1x1yleft  "JIM") (mb :1x1yright ""   ) (mb :2x1yleft  ""   )
            (mb :2x1yright ""   ) (mb :3x1yleft  ""   ) (mb :3x1yright ""   ) (mb :4x1yleft  ""   ) (mb :4x1yright ""   )
            
            (mb :0x2yleft  ""   ) (mb :0x2yright ""   ) (mb :1x2yleft  ""   ) (mb :1x2yright ""   ) (mb :2x2yleft  ""   )
            (mb :2x2yright ""   ) (mb :3x2yleft  ""   ) (mb :3x2yright ""   ) (mb :4x2yleft  "BOB") (mb :4x2yright ""   )
            
            (mb :0x3yleft  ""   ) (mb :0x3yright ""   ) (mb :1x3yleft  ""   ) (mb :1x3yright ""   ) (mb :2x3yleft  ""   )
            (mb :2x3yright ""   ) (mb :3x3yleft  ""   ) (mb :3x3yright ""   ) (mb :4x3yleft  "BIB") (mb :4x3yright ""   )
            
            (mb :0x4yleft  ""   ) (mb :0x4yright ""   ) (mb :1x4yleft  ""   ) (mb :1x4yright ""   ) (mb :2x4yleft  ""   )
            (mb :2x4yright ""   ) (mb :3x4yleft  ""   ) (mb :3x4yright ""   ) (mb :4x4yleft  "BUB") (mb :4x4yright ""   )
            ])
    :south (button :action (action :handler (fn [e] (say-map connection));(say-in-channel connection CHANNEL (get-lines)))  ;(config (acquire [:#the-grid]) :items)));  ;(say-in-channel connection CHANNEL (get-lines) ))
                                   :name "Go!"))
    ))
(defn get-lines []
  (let [one-line (flatten (interpose " | " (map #(interpose " " %1) (partition 2 (map #(subs (format "%3s" (config %1 :text)) 0 3)(config (acquire [:#the-grid]) :items))))))]
    (apply str (apply str (doall (take 19 one-line))) "|\n" (apply str (doall (take 19 (drop 20 one-line)))) "|\n" (apply str (doall (take 19 (drop 40 one-line)))) "|\n" (apply str (doall (take 19 (drop 60 one-line)))) "|\n" (apply str (doall (drop 80 one-line))) "|")
))
(defn say-map [conn]
  (let [all-lines (split (get-lines) #"\n")]
    (say-in-channel conn CHANNEL (nth all-lines 0))
    (say-in-channel conn CHANNEL (nth all-lines 1))
    (say-in-channel conn CHANNEL (nth all-lines 2))
    (say-in-channel conn CHANNEL (nth all-lines 3))
    (say-in-channel conn CHANNEL (nth all-lines 4))
    ))
(defn -main [& args]
  (let [conn (connect-to-server IRC-SERVER PORT)]

  (invoke-later
    (javax.swing.UIManager/setLookAndFeel "org.pushingpixels.substance.api.skin.SubstanceGraphiteLookAndFeel")
    (javax.swing.UIManager/put SubstanceLookAndFeel/FOCUS_KIND SubstanceConstants$FocusKind/NONE)
    (display (make-window conn))
    (-> f pack! show!))
  (server-event-loop conn CHANNEL)
))


;(defn -main []
 ; (let [conn (connect-to-server IRC-SERVER PORT)]
       ; conn @svr]
  ;  (server-event-loop conn CHANNEL)
  ;  (.close (:in conn))))

;; code for playing with the bot:
;; (def s (connect-to-server IRC-SERVER PORT))
;; (server-event-loop s CHANNEL)
;; (say-in-channel s CHANNEL "foo")
;; (disconnect-from-server s)
