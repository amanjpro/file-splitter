# File Splitter

A very simple utility to split large files into smaller ones.

## Supported IO (file?) systems:
  - Local file system
  - HDFS
  - S3
  - StdIn/StdOut

## Supported compressions:
  - No compression
  - Gzip

## Usage example:

- Download the latest `file-splitter-distribution-$VERSION.tar.gz` [file](https://github.com/amanjpro/file-splitter/releases)
- Extract the file `tar -xzf file-splitter-distribution-$VERSION.tar.gz`
- `cd` to the extracted directory
- Run the binary:
  ```sh
  bin/splitter -i file:///tmp/path-to-input.gz -o \
    s3://my-bucket/my-key --s3-output-region us-west-2 -n 3 -x gzip -z gzip
  ```
- You can leverage stdin/stdout to apply transformations on the lines:
  ```sh
  bin/splitter -i file:///tmp/path-to-input.txt -o stdout | \
    sed s/splitter/file-splitter/g | \
    bin/splitter -i stdin -o file:///tmp/parts -n 9
  ```
