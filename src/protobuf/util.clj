(ns protobuf.util
  (:import
    (clojure.lang IDeref
                  ISeq
                  IPersistentMap
                  IPersistentSet
                  IPersistentCollection)))

(defn as-fn
  "Turn an object into a fn if it is not already, by wrapping it in constantly."
  [x]
  (if (ifn? x) x, (constantly x)))

(defn fix
  "Walk through clauses, a series of predicate/transform pairs. The
  first predicate that x satisfies has its transformation clause
  called on x. Predicates or transforms may be values (eg true or nil)
  rather than functions; these will be treated as functions that
  return that value.
  The last \"pair\" may be only a transform with no pred: in that case it
  is unconditionally used to transform x, if nothing previously matched.
  If no predicate matches, then x is returned unchanged."
  [x & clauses]
  (let [call #((as-fn %) x)]
    (first (or (seq (for [[pred & [transform :as exists?]] (partition-all 2 clauses)
                          :let [[pred transform] ;; handle odd number of clauses
                                (if exists? [pred transform] [true pred])]
                          :when (call pred)]
                      (call transform)))
               [x]))))

(defprotocol Adjoin
  (adjoin-onto [left right]
    "Merge two data structures by combining the contents. For maps, merge recursively by
  adjoining values with the same key. For collections, combine the right and left using
  into or conj. If the left value is a set and the right value is a map, the right value
  is assumed to be an existence map where the value determines whether the key is in the
  merged set. This makes sets unique from other collections because items can be deleted
  from them."))

(extend-protocol Adjoin
  IPersistentMap
  (adjoin-onto [this other]
    (merge-with adjoin-onto this other))

  IPersistentSet
  (adjoin-onto [this other]
    (into-set this other))

  ISeq
  (adjoin-onto [this other]
    (concat this other))

  IPersistentCollection
  (adjoin-onto [this other]
    (into this other))

  Object
  (adjoin-onto [this other]
    other)

  nil
  (adjoin-onto [this other]
    other))
