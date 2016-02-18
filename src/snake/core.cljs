(ns snake.core)

(enable-console-print!)

(defn by-id [id]
  (.getElementById js/document id))

(def canvas (by-id "canvas"))
(def canvas-ctx (.getContext canvas "2d"))
(set! (.-fillStyle canvas-ctx) "rgb(200,0,0)")

(defn draw-rect [ctx x y w h]
  (.fillRect ctx x y w h))

(defn clear-canvas [ctx]
  (.clearRect ctx 0 0 400 400))

(defonce app-state (atom {:text "Snake in Clojure"
                          :snake [[30 10]
                                  [20 10]
                                  [10 10]]
                          :dir "Right"
                          :food [70 90]
                          :counter 1}))

(def speed 10)
(def dir-mappings
  {"Right" [speed 0]
   "Down" [0 speed]
   "Left" [(- speed) 0]
   "Up" [0 (- speed)]})

(.addEventListener
 js/window
 "keydown"
 (fn [e]
   (let [dir (.-keyIdentifier e)]
     (if (get dir-mappings dir)
       (do
         (.preventDefault e)
         (swap! app-state assoc-in [:dir] dir))))))

(defn shift-coords [left right]
  (map (fn [i] (mod i 400))
       (map + left right)))

(defn move-snake [snake-cells dir]
  (let [shift (dir-mappings dir)
        new-head (shift-coords (first snake-cells) shift)]
    (concat [new-head] (drop-last 1 snake-cells))))

(defn food-collision? [food-coords snake]
  (= food-coords (first snake)))

(defn grow [snake]
  (concat [(first snake)] snake))

(defn rand-mod [increment max]
  ;; eg (rand-mod 10 400) -> 20, 50, 120, etc
  (* (int (/ (rand max) increment)) increment))

(defn new-food []
  [(rand-mod 10 400) (rand-mod 10 400)])

(defn draw-fn []
  (clear-canvas canvas-ctx)

  (let [[food-x food-y] (:food @app-state)]
    (set! (.-fillStyle canvas-ctx) "rgb(0,0,200)")
    (draw-rect canvas-ctx food-x food-y 10 10)
    (set! (.-fillStyle canvas-ctx) "rgb(200,0,0)"))

  (if (food-collision? (:food @app-state)
                       (:snake @app-state))
    (do
      (swap! app-state assoc :food (new-food))
      (swap! app-state update-in [:snake] grow))


  (let [current-dir (:dir @app-state)
        snake (:snake @app-state)]
    (swap! app-state
           update-in
           [:snake]
           move-snake
           current-dir))

  (doseq [[x y] (:snake @app-state)]
    (draw-rect canvas-ctx x y 10 10))
  (js/setTimeout (fn []
                   (.requestAnimationFrame js/window draw-fn))
                 70))

(.requestAnimationFrame js/window draw-fn)
