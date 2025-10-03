#!/bin/bash
# Wrapper for admin/scripts/Bash/start-server.sh
exec "$(dirname "$0")/admin/scripts/Bash/start-server.sh" "$@"
