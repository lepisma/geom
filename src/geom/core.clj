(ns geom.core
  (:require [quil.core :as q]))

(defn setup []
  ; The sample from quil main repo
  (q/frame-rate 1)
  (q/smooth)
  (q/background 200))

(defn draw []
  (q/stroke (q/random 255))
  (q/stroke-weight (q/random 10))
  (q/fill (q/random 255))

  (let [diam (q/random 100)
        x    (q/random (q/width))
        y    (q/random (q/height))]
    (q/ellipse x y diam diam)))

(q/defsketch geom
  :title "quil test"
  :size [500 300]
  :setup setup
  :draw draw)

(defn -main [& args])
