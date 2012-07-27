SIREn Benchmark README file
===========================

Provide classes for benchmarking SIREn

Author: Renaud Delbru
email: renaud.delbru@deri.org

Author: Stephane Campinas
email: stephane.campinas@deri.org

--------------------------------------------------------------------------------

GETTING STARTED
===============

The benchmark platform is pre-configured to work with the Sindice-2011 dataset
archives. The platform provides four bash scripts located in the "./scripts" 
folder. Each script is performing one step of the benchmark process: fetching
the dataset, creating a term lexicon, creating the index and executing the
suite of queries. Each of these steps is described below.

CONFIGURATION
=============

The file "./scripts/scripts-env" contains environment variables for the proper 
execution of the four scripts. You might have to check the JAVA_HOME variable
and change it accordingly with your environment. You can also modify the 
JAVA_PARAM variable for tuning the parameters of the Java Virtual Machine.

To enable the benchmark platform to flush the OS cache before each query
execution (on Linux only), you have to copy the flush-fs-cache script located in
'./src/main/resources/' to '/usr/sbin/'

$ sudo cp ./src/main/resources/flush-fs-cache /usr/sbin/

INSTRUCTIONS
============

1. Fetching a sample of the Sindice-2011 dataset
------------------------------------------------

The script "./scripts/fetcher" fetches a sample of the Sindice-2011 dataset in 
a given directory.

Example: To fetch a sample composed of 20 archives (of 64MB each) and store them
to the directory '/tmp/sindice-dataset-20/'

$ ./scripts/fetcher --output /tmp/sindice-dataset-20/ --size 20

2. Creating the term lexicon
----------------------------

The term lexicon is a dictionary of terms found in the dataset. The terms are 
organised by frequency ranges (HIGH, MEDIUM, LOW). The term lexicon is used 
by the benchmark platform to generate random queries composed of terms with a
given frequency ranges. The term lexicon needs to be created only once per 
dataset.

The script "./scripts/lexicon" reads a dataset and generates the term lexicon
in a given directory. 

Example: To create a term lexicon in the directory '/tmp/sindice-lexicon-20/'
based on the previously fetched dataset sample

$ ./scripts/lexicon --document Sindice --input /tmp/sindice-dataset-20/ 
  --output /tmp/sindice-lexicon-20/
  
The term lexicon is composed of multiple term lexicon files which are organised
in a directory hierarchy:
- ${output_dir}/object/term
Directory containing the lexicon files for single terms in object position
- ${output_dir}/object/phrase
Directory containing the lexicon files for phrase terms in object position
- ${output_dir}/predicate/term
Directory containing the lexicon files for single terms in predicate position
- ${output_dir}/predicate/phrase
Directory containing the lexicon files for phrase terms in predicate position

3. Creating the index
---------------------

The script "./scripts/index" reads a dataset and creates an index given an index
type in a given directory. 

Example: To create a SIREn 1.0 index in the directory '/tmp/sindice-index-20/'
based on the previously fetched dataset sample

$ ./scripts/index --document Sindice --input /tmp/sindice-dataset-20/ 
  --index Siren10 --output /tmp/sindice-index-20/

The index files are located into a sub-directory '${output_dir}/index/'.

During the indexing process, the benchmark platform records the time to commit 
every 100K documents and the time to optimise the index at the end of the 
process. The time logs are written into the sub-directory 
'${output_dir}/time-logs/'.

4. Executing the suite of queries
---------------------------------

The script "./scripts/query" executes a suite of queries over a given index. A
list of default query specifications can be found in 
"./src/main/resources/query/spec/". A query specification is defined using a
JSON syntax and can be used to generate tree-shaped queries.

Example: To execute the default suite of queries using the previously created
index and term lexicon

$ ./scripts/query --index Siren10 --input /tmp/sindice-index-20/ 
  --lexicon /tmp/sindice-lexicon-20/

To enable the benchmark platform to flush the OS cache before each query 
execution, you have to execute this command with sudo.

The benchmark platform executes each query specification in a separate JVM for 
minimising bias in the measurements.
For each query specification, the benchmark generates a number of random 
queries. The number of queries that will be generated is defined in the JSON
query specification with the attribute 'size'.
The benchmark performs 60 measurements. Each measurement records the time to
execute the generated queries in sequence.
At the end of the execution, the benchmark outputs the measurement results to
'${input_dir}/benchmark/'. The measurement results include the raw measurements, 
the average time, the average query rate and the number of hits.

JSON Query Specification Syntax:

  TREE      := { root: ATTRIBUTE+ , ancestors: TREE* }
  ATTRIBUTE := { attribute: PRIMITIVE , value: PRIMITIVE }
  PRIMITIVE := EMPTY | PHRASE | BOOLEAN
  EMPTY     := NULL
  PHRASE    := { phrase: GROUP }
  BOOLEAN   := { boolean: GROUP:OCCUR+ }
  GROUP     := HIGH | MEDIUM | LOW
  OCCUR     := MUST | SHOULD | MUST_NOT

5. Exporting the benchmark results
----------------------------------

The script "./scripts/export" exports the results of a benchmark run into a
human readable format. By default, it is exported as a HTML table.

Example: To export the previous run results

$ ./scripts/export --index Siren10 --index-dir sindice-index-20/
  --q-results-dir sindice-index-20/benchmark/

It is possible to export results from several indexes by removing the --index
option and by using the following file hierarchies:

- Query benchmark results file structure:

  <index>/
  <index>/<query-spec>/
  <index>/<query-spec>/hits-{WARM,COLD}
  <index>/<query-spec>/time-{WARM,COLD}
  <index>/<query-spec>/rate-{WARM,COLD}
  <index>/<query-spec>/measurement-{WARM,COLD}

<query-spec> is a query specification file name, e.g., low-phrase.

- Index file structure (extracts time-logs + index size):

  <index>/
  <index>/time-logs/
  <index>/time-logs/commit.out
  <index>/time-logs/optimise.out
  <index>/index/

<index> is an Index type, e.g., Siren10.

--------------------------------------------------------------------------------

REFERENCE
=========

If you are using the Sindice-2011 dataset in your scientific work, please cite 
the following article:

S. Campinas, D. Ceccarelli, T. E. Perry, R. Delbru, K. Balog, and G. Tummarello. 
The Sindice-2011 Dataset for Entity-Oriented Search in the Web of Data. In EOS, 
SIGIR 2011 workshop, July 28, Beijing, China.

--------------------------------------------------------------------------------

FILES
=====

src/main/java
  The SIREn Benchmark source code.

src/test/java
  The SIREn Benchmark unit tests.
  
scripts
  The SIREn Benchmark scripts.

--------------------------------------------------------------------------------

This file was written by Renaud Delbru <renaud.delbru@deri.org> for SIREn.

Copyright (c) 2010, 2012
Renaud Delbru,
Digital Enterprise Research Institute,
National University of Ireland, Galway.
All rights reserved.

