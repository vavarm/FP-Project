#!/bin/bash

# Define the URLs
URLS=(
  "https://nvd.nist.gov/feeds/json/cve/1.1/nvdcve-1.1-2024.json.zip"
  "https://nvd.nist.gov/feeds/json/cve/1.1/nvdcve-1.1-2023.json.zip"
)

# Get the directory where the script is located
SCRIPT_DIR=$(dirname "$(realpath "$0")")

# Define the data folder
DATA_DIR="$SCRIPT_DIR/data-in"

# Create the data folder if it doesn't exist
mkdir -p "$DATA_DIR"

# Change to the data directory
cd "$DATA_DIR" || exit

# Download and extract each file
for URL in "${URLS[@]}"; do
  # Extract the filename from the URL
  FILENAME=$(basename "$URL")

  # Download the file
  echo "Downloading $URL..."
  curl -O "$URL"

  # Check if the file was downloaded
  if [[ -f "$FILENAME" ]]; then
    echo "Extracting $FILENAME..."
    unzip -o "$FILENAME"
    echo "Extraction complete: $FILENAME"
  else
    echo "Failed to download $URL"
  fi
done

echo "All files downloaded and extracted to $DATA_DIR."