# File Splitter

[![Build Status](https://travis-ci.org/amanjpro/file-splitter.svg?branch=master)](https://travis-ci.org/amanjpro/file-splitter) [![codecov](https://codecov.io/gh/amanjpro/file-splitter/branch/master/graph/badge.svg)](https://codecov.io/gh/amanjpro/file-splitter)

A very simple utility to split large files into smaller ones. This utility is
usually useful when a thirdparty data provider sends a gigantic gzip file, and
you want to process it in Spark (or any other big data tool). As it is a waste
of resources, to have the whole cluster started to re-partition this file then
perform the rest of the ETL logic on it, you can first use this tool to split
the file into smaller pieces and then start a resource hungry job to process
it.

## Supported IO (file?) systems:
  - Local file system
  - SFTP
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

## Development

### Requirements

- Java 8
- SBT 1.3
- Shellcheck (for testing) found [here](https://github.com/koalaman/shellcheck)
- Docker (for integration testing) found [here](https://www.docker.com)

### Building from the source

SBT takes care of building/testing and integration testing:

- First run `ci/env-setup`. This starts the docker instances for S3mock, Hadoop
  and SFTP for integration testing
- Then: `sbt clean test it:test package`
- Once done, run `ci/env-teardown`

## Supported options

```sh
$ bin/splitter --help
splitter 0.6.0-SNAPSHOT
Usage: splitter [options]

  -i <value> | --input <value>
        The file to be splitted. At this point, S3, local FS, Sftp
        and HDFS are supported. The job can also read from stdin
        by simply passing 'stdin' as the input.
        Exmples: hdfs://..., s3://..., sftp://... and file://...
  -o <value> | --output <value>
        The directory where the splitted parts should go.
        At this point, S3, local FS, Sftp and HDFS are supported.
        The job can also write to stdout by simply passing
        'stdout' here. Exmples: hdfs://..., s3://..., sftp://...
        and file://...
  -x <value> | --input-compression <value>
        Input file compression formatSupported compressions:
        none, gzip. Default: none
  -z <value> | --output-compression <value>
        Output file compression format Supported compressions:
        none, gzip Default: none
  --input-sftp-username <value>
        Input sftp username. Required when input is sftp.
  --input-sftp-password <value>
        Input sftp password, optional.
  --output-sftp-username <value>
        Output sftp username. Required when output is sftp.
  --output-sftp-password <value>
        Output sftp password, optional.
  --s3-input-region <value>
        Input S3 Region. Required when dealing with S3 paths only.
  --s3-output-region <value>
        Output S3 Region. Required when dealing with S3 paths only.
  --input-hdfs-root-uri <value>
        Input HDFS root URI. Default: hdfs://localhost:8020
  --input-hdfs-user <value>
        Input HDFS user. Default: hdfs
  --input-hdfs-home-dir <value>
        Input HDFS home directory. Default: /
  --output-hdfs-root-uri <value>
        Output HDFS root URI. Default: hdfs://localhost:8020
  --output-hdfs-user <value>
        Output HDFS user. Default: hdfs
  --output-hdfs-home-dir <value>
        Output HDFS home directory. Default: /
  --keep-order <value>
        Keep the order of the input lines. That is first n
        lines go to the first file and so on. This might
        generate files with uneven sizes
  -n <value> | --number-of-files <value>
        Number of output files, default is 1.
  --help
        prints this usage text

By default the Sftp module, looks for the known_hosts in
/Users/amanj/.ssh/known_hosts. You can change it by setting up
KNOWN_HOSTS environment variable, something like:
KNOWN_HOSTS=/new/path bin/splitter ...

You can also specify the default location(s) for public key
when dealing with Sftp. By default the splitter looks for
the defined public/private keys in /Users/amanj/.ssh/id_rsa.
But you can override it by setting PUBLIC_KEY_LOCATIONS
environment variable, like:
PUBLIC_KEY_LOCATIONS=/new/path bin/splitter ...
```
