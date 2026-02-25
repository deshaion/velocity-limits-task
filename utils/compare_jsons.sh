#!/bin/bash

# Check if two arguments are provided
if [ "$#" -ne 2 ]; then
    echo "Usage: $0 file1.json file2.json"
    exit 1
fi

FILE1=$1
FILE2=$2

# Check if jq is installed
if ! command -v jq &> /dev/null; then
    echo "Error: 'jq' is not installed. Please install it (e.g., sudo apt install jq)."
    exit 1
fi

# -S: Sorts keys alphabetically
# -c: Compact output (keeps each JSON object on one line)
diff --color -u <(jq -S -c . "$FILE1") <(jq -S -c . "$FILE2")