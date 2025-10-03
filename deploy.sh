#!/bin/bash
# Wrapper for admin/scripts/Bash/deploy.sh
exec "$(dirname "$0")/admin/scripts/Bash/deploy.sh" "$@"
