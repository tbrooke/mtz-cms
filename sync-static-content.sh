#!/bin/bash
# Sync Static Content from Alfresco
# Can be run from anywhere - will navigate to project root

# Navigate to project root (where this script is located)
cd "$(dirname "$0")"

echo "📍 Project root: $(pwd)"
echo ""

# Check if tunnel is active
if ! pgrep -f "ssh.*trust" > /dev/null; then
    echo "⚠️  SSH tunnel not running!"
    echo "   Start with: tunnel"
    echo "   Or: ssh -L 8080:localhost:8080 -N -f tmb@trust"
    echo ""
    read -p "Start tunnel now? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        ssh -L 8080:localhost:8080 -N -f tmb@trust
        sleep 2
        echo "✅ Tunnel started"
        echo ""
    else
        echo "❌ Exiting - tunnel required"
        exit 1
    fi
fi

# Run the sync
clojure -M -e "(load-file \"src/sync_content.clj\") (sync-content/sync-all!)"
