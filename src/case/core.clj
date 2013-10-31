(ns case.core
  (:gen-class)
  (:require [clojure.set :refer :all])
  )

;I hope you have rainbow parantheses on!

;some quick clojure notes:
;   To get an item from a map, (:key {:key :value}) is used,
;where the colon syntax represents a symbol. It is also a function
;when used to get an item from a list.
;   #(something %) is shorthand for a an anonymous function, where % is
;the parameter passed to it.
;   -> feeds through to the second operator of the next command
;so that (-> a b c d) === (d (c (b a)))
;   You probably know what a zipper is, but otherwise it's just
;an easy way to navigate around a tree
;   defmulti defines a multimethod (using multiple dispatch), dispatching to the
;correct method depending on the function given

(defn node [type & args] {:type type, :args (vec args)})
;(def simple (node :case (node :int 1) (node :alternatives 
;(node :alternative (node :int 0) (node :int 1)) 
;(node :alternative (node :int 1) (node :int 0)))))
(def simple (-> (slurp "resources/simple.ast") read-string eval))
(def input-file simple)

(defn merge-inv-map 
  [m] (merge m (clojure.set/map-invert m)))

(def types {:cint {:ident 0 :constructors (merge-inv-map {:int 0 :Integer 1})},
            :cbool {:ident 1 :constructors (merge-inv-map {:false 0 :true 1})},
            :cstring {:ident 2 :constructors (merge-inv-map {:string 0})},})


(defn node-type "Gets type from node n" [n] (:type n))
(defn get-code "Gets code from node n" [n] (when-let [code (:code n)] code))

(defn annotate "Annotates node n with code"[n code] (assoc n :code (str code)))
(defmulti annotatecode node-type)
(defmethod annotatecode :int [n] 
  (let [number (first (:args n))] 
    (annotate n (str "ldc " number \
                     "invokestatic NewType/cint(I)LType;"
                     ))))
(defmethod annotatecode :bool [n] ;TODO
  (let [value (:bool (first (:args n)))])
  )
(defmethod annotatecode :default [n] n)

(defn c-eval-int
  [node] (if (= :int (:type node)) 
           (assoc node :code 
                  (str "int " (first (:args node)))) 
           node))

(defn c-eval-t
  ([type val] (if (= type :int) (c-eval-int val) (println "not recognised"))))

;making a zipper for my AST type
(require '[clojure.zip :as zip])
(defn cbranch? [n] (and (not (= [] (:args n))) (some :type (:args n))))
(defn cchildren [n] (seq (:args n)))
(defn cmake-node [n children] (node (:type n) children))
(defn astzipper [n] (zip/zipper cbranch? cchildren cmake-node n))

(defn zip-right-down [loc] 
  "Zips to the bottom of the tree from this loc"
  (loop [p loc]
    (if (zip/branch? p)
      (do (def cnext-stack (cons p cnext-stack))
          (recur (zip/down p)))
      p)))

(defn printstack [stack]
  "For debugging purposes, prints a stack like cnext-stack."
  (when-not (empty? stack) 
    (loop [st stack]
        (when-not (empty? st) 
          (do 
            (println (str "st: "(let [x (first st)] (-> x zip/node :type)))) 
            (recur (rest st)))))))

(def cnext-stack (list)) ;visited nodes
(defn cnext
  "Case next - the next item in the case tree using post-order
  traversal. Uses a starting zipper location, and a stack to track
  previously entered locations."
  ([loc] (cnext loc cnext-stack))
  ([loc stack] 
   (if (= :end (loc 1))
     loc
     (or 
       ;branch node and visited before?
       (when (and (zip/branch? loc) (nil? (some #(= loc %) stack))) 
         (do (def cnext-stack (cons loc stack))
             (zip-right-down loc)))
       ;if sibling, go to it and zip down
       (when (zip/right loc) (-> loc zip/right zip-right-down)) 
       (if (zip/up loc)
         (zip/up loc)
         [(zip/node loc) :end])))))

(defn reset-search [] 
  "Call this to reset state between 'case.core.search calls"
  (do
    (def cnext-stack (list))
    (def first-iter-search true)))

(defn do-loc-edit 
  "Takes a zipper loc and performs the edit. Returns next item
  in stack."
  [loc edit]
  ;skip first round unless it's a leaf (so tree of one)
  (if (and 
        (or (not (zip/branch? loc))
            (not first-iter-search)) 
        edit) ;only edit if given edit function 
    (do 
      (if-let [edited (edit loc)] 
        ;return next item with replacement if done, or not otherwise.
        (cnext (zip/replace loc edited) cnext-stack)
        (cnext loc cnext-stack)))
    (do (def first-iter-search false)
        (cnext loc cnext-stack))))

(defn search 
  "Takes a zipper and optionally an edit function, and does a post-
  order traversal of the tree the zipper represents. Calls the edit
  function on each node and replaces the node accordingly in the
  resulting tree."
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
(def post-order (astzipper (node :top (node :left (node :leftleft)
   (node :leftright (node :leftrightleft) (node :leftrightright))) (node :right))))

;(search post-order #(println (:type (zip/node %))))

;Compile jasmin files
(use '[clojure.java.shell :only [sh]])
(def testfiles '("Int.j", "Runner.j"))
(defn compile-files [files] (map 
  (fn [file] 
    (sh "jasmin" (str "./output/" file) "-g" "-d" "./resources/")) 
  files))
(compile-files testfiles)

(defn tree-to-code "Returns code from the AST ast" [ast] 
  (-> (search (astzipper ast) #(annotatecode (zip/node %)))
      zip/node get-code))

;TODO change dynamically
(def stacksize 5)
(def varsize 6)

;Compile runner program
(defn spit-code  
  "Given an (optional) filename and string, this writes out the string as 
  Jasmin code."
  ([string] (spit-code "output/Runner.j" string))
  ([f string] 
   (let [strrpl clojure.string/replace] 
     (do
       (spit "output/tmpout" 
             ;pretty print!
             (str "\t" (strrpl string "\n" "\n\t")))
       ;some shell magic, concats runner around generated code
       ;also puts in logical values for classname, stacksize etc
       (let [cclass (strrpl (strrpl f #".*\/" "") ".j" "")] 
         (sh "sh" "-c" (str "sed \"s/___CLASS/" cclass "/; s/___STACKSIZE/" stacksize "/; s/___VARSIZE/\"" varsize "/ output/Runner.j1 | cat - output/tmpout output/Runner.j2 > " f "&& rm output/tmpout")))))))
       ;sorry for the unwieldy line --- clojure's (str) fn. automatically 
       ;puts new line characters if it's broken over multiple lines 

(defn get-int "Unboxes array type for testing" [ctype] 
  ;this is java interop, basically doing ctype.values[0]
  (aget (.values ctype) 0))

(defn run
  [runname tree]
  (do (spit-code (str "output/" runname ".j") (tree-to-code tree))
      (Thread/sleep 100)
      (println (compile-files [(str runname ".j")]))
      (Thread/sleep 100)
      (.run (eval (read-string (str "(new " runname ")"))))))

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
   (let [syntaxes (map #(-> (slurp %) read eval) args)]
      
     )))
