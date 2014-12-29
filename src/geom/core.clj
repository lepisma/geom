(ns geom.core
  (:require [quil.core :as q]
            [quil.middleware :as m])
  (:import (ddf.minim Minim))
  (:import (ddf.minim.analysis FFT BeatDetect)))

;; Minim objects and stuff
;; -----------------------

;; Audio in object
(def audio-in (.getLineIn (Minim.)))

;; The fft object
(def fft (FFT. (.size (.left audio-in)) 44100))

;; Beat detector
(def beat (BeatDetect.))
(def width 800)
(def height 600)

;; Constants
;; FFT smoothing
(def fft-smoothing 0.8)
;; Frequencies to get fft data for
(def freqs [60 170 310 600 1000 3000 6000 12000 14000 16000])

;; Drawing functions
;; ---------------

(defn circles [scaled-fft]
  ;; Return a vector of map of data for plotting circles
  [{:x (+
        (* (nth scaled-fft 0) width)
        (* width 0.5))
    :y (+
        (* (nth scaled-fft 1) height)
        (* height 0.5))
    :red (* 255 (nth scaled-fft 2))
    :green (* 255 (nth scaled-fft 3))
    :blue (* 255 (nth scaled-fft 4))}

   {:x (-
        (* 0.5 width)
        (* (nth scaled-fft 5) width))
    :y (-
        (* 0.5 height)
        (* (nth scaled-fft 6) height))
    :red (* 255 (nth scaled-fft 7))
    :green (* 255 (nth scaled-fft 8))
    :blue (* 255 (nth scaled-fft 9))}]
  )

(defn plot-circle [data size]
  ;; Plot circle with the data (map)
  (q/fill (:red data) (:green data) (:blue data))
  (q/stroke (+ 20 (:red data)) (+ 20 (:green data)) (+ 20 (:blue data)))
  (q/ellipse (:x data) (:y data) size size)
  )

;; Sketch functions
;; ----------------
;; state contains the fft vectors
(defn setup []
  ;; The initial setup
  (q/smooth)
  (q/background 10)
  {:current (take (count freqs) (repeat 0.001))
   :previous (take (count freqs) (repeat 0.001))
   :max (take (count freqs) (repeat 0.001))
   :scaled (take (count freqs) (repeat 0.001))
   :beat (.detect beat (.left audio-in))}
  )

(defn update [state]
  ;; Grab next audio chunk and find fft
  (.forward fft (.left audio-in))

  (let [new-fft (map #(.getFreq fft %) freqs)]
    (let [max-fft (map max new-fft (:max state))
          current-fft (map +
                           (map #(* (- 1 fft-smoothing) %) new-fft)
                           (map #(* fft-smoothing %) (:current state)))]
      (-> state
          (assoc :previous (:current state)
                 :max max-fft
                 :current current-fft
                 :scaled (map / current-fft max-fft)
                 :beat (.detect beat (.left audio-in))))
      )
    )
  )

(defn draw [state]
  ;; Plot the circles
  (let [size (*
              height
              (first (:scaled state))
              (if (= nil (.isOnset beat)) 1 4))]
        (let [data (circles (:scaled state))]
              (plot-circle (first data) size)
              (plot-circle (second data) size)))
  )

(q/defsketch geom
  :title "geom Audio Visualizer"
  :size [width height]
  :setup setup
  :draw draw
  :update update
  :middleware [m/fun-mode])
