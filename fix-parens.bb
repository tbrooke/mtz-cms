#!/usr/bin/env bb

(require '[babashka.process :as p]
         '[clojure.string :as str])

(defn fix-file [filepath]
  ;; Format first - correct syntax for -T tools
  (print "Formatting... ")
  (let [fmt-result (p/shell {:continue true :err :string}
                            "clojure" "-Tcljfmt" "fix"
                            (str "{:paths [\"" filepath "\"]}"))]
    (if (zero? (:exit fmt-result))
      (println "✅")
      (println "❌" (:err fmt-result))))

  ;; Then check
  (print "Checking... ")
  (let [result (p/shell {:continue true :out :string}
                        "clj-kondo" "--lint" filepath)]
    (if (zero? (:exit result))
      (println "✅ No errors!")
      (println "\n" (:out result)))))

(if-let [file (first *command-line-args*)]
  (fix-file file)
  (println "Usage: bb fix-parens.bb <file.clj>"))
