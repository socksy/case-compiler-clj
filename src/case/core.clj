(ns case.core
  (:gen-class))

(defn node [type & args] {:type type, :args (vec args)})
(def simple (node :case (node :int 1) (node :alternatives (node :alternative (node :int 0) (node :int 1)) (node :alternative (node :int 1) (node :int 0)))))


(println simple)
(:type simple)

(defn -main
  "Main function"
 [& args]
  (println simple))

(defn c-eval-int
  [node] (do (if (= :int (:type node)) (println (str "int " (first (:args node)))) ()) (node)))

(defn c-eval-t
  ([type val] (if (= type :int) (c-eval-int val) (println "not recognised"))))

;zipping
(require '[clojure.zip :as zip])
(defn cbranch? [n] (not (nil? (:type n))))
(defn cchildren [n] (seq (:args n)))
(defn cmake-node [n children] (node (:type n) (conj (:args n) children)))
(defn myzipper [n] (zip/zipper cbranch? cchildren cmake-node n))
(def myzip (myzipper simple))
(c-eval-int (-> myzip zip/down zip/right zip/down zip/down zip/node))

(defn search ([zipper] (search zipper nil)) 
  ([zipper edit]  
  (loop [loc zipper]
    (if (zip/end? loc)
      (zip/root loc)
      (do 
        (when edit (when-let [edited (edit loc)] 
          (zip/replace loc edited))) 
        (recur (zip/next loc)))))))

;Compile jasmin files
(use '[clojure.java.shell :only [sh]])
(def files '("Main"))
(map 
    (fn [file] 
      (sh "jasmin" (str "./output/" file ".j") "-g" "-d" "./resources/")) 
    files)

;may break in later versions --- 'clojure.core/import* returns an op code
;contrast to (import blah) which is a macro. That was we can pass it the files
;(map #('clojure.core/import* %) files) 
 
(Main/main nil)
(def myint (new Int))
(.run myint)
