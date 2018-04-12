#!/bin/bash
set -euo pipefail
IFS=$'\n\t'

script_dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

fly -t wings set-pipeline \
    -p sample-apps \
    -c "${script_dir}/pipeline.yml" \
    -l <(lpass show 'Sample Apps Concourse Secrets' --notes)
