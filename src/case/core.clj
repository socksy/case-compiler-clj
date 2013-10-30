(ns case.core
  (:gen-class))

(defn node [type & args] {:type type, :args (vec args)})
;(def simple (node :case (node :int 1) (node :alternatives (node :alternative (node :int 0) (node :int 1)) (node :alternative (node :int 1) (node :int 0)))))
(def simple (-> (slurp "resources/simple.ast") read-string eval))
(def input-file simple)

(def types {:cint {:ident 0 :constructors {:int 0 :Integer 1}},
            :cbool {:ident 1 :constructors {:false 0 :true 1}}
            })


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

(defn do-loc-edit [loc edit]
       ;skip first round
       (if (and (not first-iter-search) 
                  edit) ;only edit if given edit function 
         (do 
             (if-let [edited (edit loc)] 
               ;return next item with replacement if done, or not otherwise.
               (cnext (zip/replace loc edited) cnext-stack)
               (cnext loc cnext-stack)))
         (do (def first-iter-search false)
             (cnext loc cnext-stack))))

(defn search 
  ([zipper] (search zipper nil)) 
  ([zipper edit]  
   (do 
     (reset-search)
     (loop [loc zipper]
       (if (zip/end? loc)
         ;(zip/node loc)
         loc
         (recur (do-loc-edit loc edit)))))))

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

;TODO change dynamically
(def stacksize 5)
(def varsize 6)

;Compile runner program
(defn spit-code  
  ([string] (spit-code "output/Runner.j" string))
  ([f string] (do
    (spit "output/tmpout" 
          ;pretty print!
          (str "\t"
               (clojure.string/replace string "\n" "\n\t")))
    ;some shell magic, concats runner around generated code
    (sh "sh" "-c" (str"sed \"s/___STACKSIZE/" stacksize "/; s/___VARSIZE/\"" varsize "/ output/Runner.j1 | cat - output/tmpout output/Runner.j2 > " f "&& rm output/tmpout")))))


;may break in later versions --- 'clojure.core/import* returns an op code
;contrast to (import blah) which is a macro. That was we can pass it the files
;(map #('clojure.core/import* %) files) 


;(Main/main nil)
(def myint (new Int))
('clojure.core/import "Type")
(new Type)
(.values (.run myint))


(defn -main
  "Main function"
 [& args]
  (
   (let [syntaxes (map #(-> (slurp %) read eval) args)])))
