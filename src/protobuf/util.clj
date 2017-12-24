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

(defn into-set
  "Update the given set using an existence map."
  [set map]
  (if (map? map)
    (reduce (fn [set [k v]] ((if v conj disj) set k))
            set map)
    (into set map)))

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

(defn to-fix
  "A \"curried\" version of fix, which sets the clauses once, yielding a
  function that calls fix with the specified first argument."
  [& clauses]
  (fn [x]
    (apply fix x clauses)))

(defmacro given
  "A macro combining the features of fix and fixing, by using parentheses to group the
   additional arguments to each clause:
   (-> x
       (given string? read-string
              map? (dissoc :x :y :z)
              even? (/ 2)))"
  [x & clauses]
  (let [[clauses default] (if (even? (count clauses))
                            [clauses `identity]
                            [(butlast clauses) (last clauses)])]
    `(fix ~x ~@(for [[pred transform] (partition 2 clauses)
                     arg [pred `#(-> % ~transform)]]
                 arg)
          ~default)))

(defmacro map-entry
  "Create a clojure.lang.MapEntry from a and b. Equivalent to a cons cell.
  flatland.useful.experimental.unicode contains a shortcut to this, named ·."
  [a b]
  `(clojure.lang.MapEntry. ~a ~b))

(defn update-in*
  "Updates a value in a nested associative structure, where ks is a sequence of keys and f is a
  function that will take the old value and any supplied args and return the new value, and returns
  a new nested structure. If any levels do not exist, hash-maps will be created. This implementation
  was adapted from clojure.core, but the behavior is more correct if keys is empty and unchanged
  values are not re-assoc'd."
  [m keys f & args]
  (if-let [[k & ks] (seq keys)]
    (let [old (get m k)
          new (apply update-in* old ks f args)]
      (if (identical? old new)
        m
        (assoc m k new)))
     (apply f m args)))

(defn update
  "Update a value for the given key in a map where f is a function that takes the previous value and
  the supplied args and returns the new value. Like update-in*, unchanged values are not
  re-assoc'd."
  [m key f & args]
  (apply update-in* m [key] f args))

(defn map-vals
  "Create a new map from m by calling function f on each value to get a new value."
  [m f & args]
  (when m
    (into {}
          (for [[k v] m]
            (map-entry k (apply f v args))))))

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

(defn adjoin
  "Merge two data structures by combining the contents. For maps, merge recursively by
  adjoining values with the same key. For collections, combine the right and left using
  into or conj. If the left value is a set and the right value is a map, the right value
  is assumed to be an existence map where the value determines whether the key is in the
  merged set. This makes sets unique from other collections because items can be deleted
  from them."
  [a b]
  (adjoin-onto a b))

(defn catbytes [& args]
  (let [out-buf (byte-array (loop [len 0, args (seq args)]
                              (if-let [[arg & args] args]
                                (recur (+ len (alength ^bytes arg)) args)
                                len)))]
    (loop [offset 0, args args]
      (if-let [[^bytes array & more] (seq args)]
        (let [size (alength array)]
          (System/arraycopy array 0
                            out-buf offset size)
          (recur (+ size offset) more))
        out-buf))))

(defn walk
  "Traverse all child types of the given schema, calling inner on each, then call outer on the result."
  [inner outer schema]
  (outer
   (case (:type schema)
     :struct           (update schema :fields map-vals inner)
     (:set :list :map) (-> schema
                           (given :values (update :values inner))
                           (given :keys   (update :keys   inner)))
     schema)))

(defn postwalk
  "Perform a depth-first, post-order traversal of all types within the given schema, replacing each
  and type with the result of calling f on it."
  [f schema]
  (walk (partial postwalk f) f schema))

(defn prewalk
  "Like postwalk, but do a pre-order traversal."
  [f schema]
  (walk (partial prewalk f) identity (f schema)))

(defn dissoc-fields
  "Traverse the given schema, removing the given fields at any level."
  [schema & fields]
  (prewalk (to-fix #(= :struct (:type %))
                   #(apply update % :fields dissoc fields))
           schema))
