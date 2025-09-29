FROM clojure:temurin-21-tools-deps-jammy

# Set working directory
WORKDIR /app

# Copy deps.edn first for dependency caching
COPY deps.edn /app/

# Download dependencies
RUN clojure -P -M:dev

# Copy the rest of the application
COPY . /app/

# Expose the application port
EXPOSE 3000

# Start the application
CMD ["clojure", "-M", "-m", "mtz-cms.core"]