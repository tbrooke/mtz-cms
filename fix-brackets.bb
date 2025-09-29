#!/usr/bin/env bb
;; fix-brackets.bb

(require '[clojure.string :as str]
         '[clojure.java.io :as io])

(defn find-bracket-mismatch [content]
  "Find mismatched brackets in content"
  (let [lines (str/split-lines content)
        stack (atom [])
        line-num (atom 0)]
    (doseq [line lines]
      (swap! line-num inc)
      (doseq [char line]
        (case char
          (\( \[ \{) (swap! stack conj {:char char :line @line-num})
          \) (if (and (seq @stack) (= (:char (peek @stack)) \())
               (swap! stack pop)
               (println "❌ Line" @line-num ": Unexpected )"))
          \] (if (and (seq @stack) (= (:char (peek @stack)) \[))
               (swap! stack pop)
               (println "❌ Line" @line-num ": Unexpected ]"))
          \} (if (and (seq @stack) (= (:char (peek @stack)) \{))
               (swap! stack pop)
               (println "❌ Line" @line-num ": Unexpected }"))
          nil)))
    (when (seq @stack)
      (doseq [unclosed @stack]
        (println "❌ Unclosed" (:char unclosed) "at line" (:line unclosed))))))

(defn fix-file [filepath]
  (println "\n📁 Checking:" filepath)
  (let [content (slurp filepath)]
    (find-bracket-mismatch content)))

;; Check specific problem files
(doseq [file ["mtz_cms/validation/schemas.clj"]]
  (when (.exists (io/file file))
    (fix-file file)))
