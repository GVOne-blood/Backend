#!/bin/bash

# Define the directory where the .proto files are located
proto_folder="proto"

# Check if the script is being run from the correct directory (proto folder)
if [[ $(basename "$PWD") != "$proto_folder" ]]; then
    echo "Error: You must run this bash script in the '$proto_folder' folder."
    exit 1
fi

# List all .proto files in the entity folder
for proto_file in entity/*.proto; do
    if [[ -f "$proto_file" ]]; then
        # Run the protoc command for each .proto file
        protoc --include_imports --descriptor_set_out="apisix/$(basename "$proto_file" .proto).pb" "$proto_file"

        # Check if the protoc command was successful
        if [[ $? -ne 0 ]]; then
            echo "Try specify the path to protoc in the bash script"
        fi
    fi
done