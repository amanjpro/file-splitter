# File Splitter

A very simple utility to split large files into smaller ones.

## Supported file systems:
  - Local file system
  - HDFS
  - S3

## Supported compressions:
  - No compression
  - Gzip

## Usage example:

`bin/splitter -i file:///tmp/path-to-input.gz -o s3://my-bucket/my-key --s3-output-region us-west-2 -n 3 -x gzip -z gzip`
