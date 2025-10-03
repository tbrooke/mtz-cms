#!/bin/bash
# Start Mount Zion CMS Server with new navigation system

# Navigate to project root (3 levels up from admin/scripts/Bash)
cd "$(dirname "$0")/../../.."

echo "🚀 Starting Mount Zion CMS..."
echo "📍 Project root: $(pwd)"
echo ""

# Check SSH tunnel
if ! pgrep -f "ssh.*trust" > /dev/null; then
    echo "⚠️  SSH tunnel not running. Starting..."
    ssh -L 8080:localhost:8080 -N -f tmb@trust
    sleep 2
fi

echo "✓ SSH tunnel active"
echo ""
echo "Starting Clojure REPL..."
echo "Once loaded, run: (require 'user) (user/start)"
echo ""

clojure -M:dev
