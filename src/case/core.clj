(ns case.core
  (:gen-class))

(defn node [type & args] {:type type, :args (vec args)})
(def simple (node :case (node :int 1) (node :alternatives (node :alternative (node :int 0) (node :int 1)) (node :alternative (node :int 1) (node :int 0)))))


(defn node-type [n] (:type n))

(defn annotate [n code] (assoc n :code (str code)))
(defmulti annotatecode node-type)
(defmethod annotatecode :int [n] (annotate n (str "bipush " (first (:args n)))))
(defmethod annotatecode :default [n] n)

(defn c-eval-int
  [node] (if (= :int (:type node)) 
           (assoc node :code 
                  (str "int " (first (:args node)))) 
           node))

(defn c-eval-t
  ([type val] (if (= type :int) (c-eval-int val) (println "not recognised"))))

;zipping
(require '[clojure.zip :as zip])
(defn cbranch? [n] (and (not (= [] (:args n))) (some :type (:args n))))
(defn cchildren [n] (seq (:args n)))
(defn cmake-node [n children] (node (:type n) children))
(defn astzipper [n] (zip/zipper cbranch? cchildren cmake-node n))
(def myzip (astzipper simple))

(defn zip-down [loc] 
  "Zips to the bottom of the tree from this loc"
  (loop [p loc]
    (if (zip/branch? p)
      (do (def cnext-stack (cons p cnext-stack))
          (recur (zip/down p)))
      p)))

(defn printstack [stack]
  (when-not (empty? stack) 
    (loop [st stack]
        (when-not (empty? st) 
          (do 
            (println (str "st: "(let [x (first st)] (-> x zip/node :type)))) 
            (recur (rest st)))))))

(def cnext-stack (list)) ;visited nodes
(defn cnext
  ([loc] (cnext loc cnext-stack))
  ([loc stack] 
   (if (= :end (loc 1))
     loc
     (or 
       ;branch node and visited before?
       (when (and (zip/branch? loc) (nil? (some #(= loc %) stack))) 
         (do (def cnext-stack (cons loc stack))
             (zip-down loc)))
       ;if sibling, go to it and zip down
       (when (zip/right loc) (-> loc zip/right zip-down)) 
       (if (zip/up loc)
         (zip/up loc)
         [(zip/node loc) :end])))))

(defn reset-search [] 
  (do
    (def cnext-stack (list))
    (def first-iter-search true)))

(defn search 
  ([zipper] (search zipper nil)) 
  ([zipper edit]  
   (do 
     (reset-search)
     (loop [loc zipper]
       (if (zip/end? loc)
         ;(zip/node loc)
         loc
         (do 
           (if-let ;have we done an edit?
             [editedloc 
              ;skip first round
              (when (and (not first-iter-search) 
                         edit) ;only edit if given edit function 
                (if-let [edited (edit loc)] 
                  (zip/replace loc edited)))]
             (do (def first-iter-search false) 
                 (recur (cnext editedloc cnext-stack)))
             (do (def first-iter-search false) 
                 (recur (cnext loc cnext-stack))))))))))

;test zipper for post-order traversal
(def post-order (astzipper (node :top (node :left (node :leftleft) (node :leftright (node :leftrightleft) (node :leftrightright))) (node :right))))

(search post-order #(println (:type (zip/node %))))

;Compile jasmin files
(use '[clojure.java.shell :only [sh]])
(def files '("Int"))
(map 
    (fn [file] 
      (sh "jasmin" (str "./output/" file ".j") "-g" "-d" "./resources/")) 
    files)

;may break in later versions --- 'clojure.core/import* returns an op code
;contrast to (import blah) which is a macro. That was we can pass it the files
;(map #('clojure.core/import* %) files) 
 
;(Main/main nil)
(def myint (new Int))
(.run myint)


(defn -main
  "Main function"
 [& args]
  (println simple))
