(ns protobuf.common)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Utility Functions   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-class
  [this]
  (:protobuf-class (.contents this)))

(defn get-instance
  [this]
  (:instance (.contents this)))

(defn get-wrapper
  [this]
  (:java-wrapper (.contents this)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Behaviours   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def associative-behaviour
  {:containsKey (fn [this data] (.containsKey (get-instance this) data))
   :entryAt (fn [this data] (.entryAt (get-instance this) data))})

(def iterable-behaviour
  {:forEach (fn [this consumer] (.forEach (get-instance this) consumer))
   :iterator (fn [this] (.iterator (get-instance this)))
   :spliterator (fn [this] (.spliterator (get-instance this)))})

(def lookup-behaviour
  {:valAt (fn ([this k] (.valAt (get-instance this) k))
              ([this k fallback] (.valAt (get-instance this) k fallback)))})

(def persistent-collection-behaviour
  {:cons (fn [this o] (.cons (get-instance this) o))
   :count (fn [this] (.count (get-instance this)))
   :empty (fn [this] (.empty (get-instance this)))
   :equiv (fn [this o]
           (and (= (get-class this) (get-class o))
                (.equiv (get-instance this) (get-instance o))))})

(def persistent-map-behaviour
  {:assoc (fn [this k v] (.assoc (get-instance this) k v))
   :assocEx (fn [m k v] (throw (new Exception)))
   :without (fn [this k] (.without (get-instance this) k))})

(def printable-behaviour
  {:toString (fn [this] (.toString (get-instance this)))})

(def seqable-behaviour
  {:seq (fn [this] (.seq (get-instance this)))})
