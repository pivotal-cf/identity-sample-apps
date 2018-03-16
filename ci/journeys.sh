#!/bin/bash
set -euo pipefail

workspace_dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && cd ../.. && pwd )"

fly -t wings execute \
             --config $workspace_dir/identity-sample-apps/ci/tasks/journeys/task.yml \
             --input identity-sample-apps=$workspace_dir/identity-sample-apps \
             --input uaa=$workspace_dir/uaa
