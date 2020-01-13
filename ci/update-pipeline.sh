#!/bin/bash
set -euo pipefail
IFS=$'\n\t'

script_dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

fly -t hush-house set-pipeline \
    -p identity-sample-apps-2.1 \
    -c "${script_dir}/pipeline.yml" \
    -l <(lpass show 'Sample Apps Concourse Secrets' --notes)
