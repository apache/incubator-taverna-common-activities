################################################################################
#  Licensed to the Apache Software Foundation (ASF) under one or more
#     contributor license agreements.  See the NOTICE file distributed with
#     this work for additional information regarding copyright ownership.
#     The ASF licenses this file to You under the Apache License, Version 2.0
#     (the "License"); you may not use this file except in compliance with
#     the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
#     Unless required by applicable law or agreed to in writing, software
#     distributed under the License is distributed on an "AS IS" BASIS,
#     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#     See the License for the specific language governing permissions and
#     limitations under the License.
#################################################################################


#!/usr/bin/env cwl-runner

class: CommandLineTool
cwlVersion: cwl:draft-3

$namespaces:
 edam: http://edamontology.org/
  
inputs:
  - id: input_1
    type: int
    label: input 1 testing label
    description: this is a short description of input 1
    format: edam:format_2323

  - id: input_2
    type:
      type: array
      items: int
    label: input 2 testing label
    description: this is a short description of input 2
    format: $expression

  - id: input_3
    type: ["null",int]
    label: input 3 testing label
    description: this is a short description of input 3
    format: noNameSpace:testing

outputs:
  - id: output_1
    type: int
    label: output 1 testing label
    description: this is a short description of output 1
    format: just a string

  - id: output_2
    type: String
    label: output 2 testing label
    description: this is a short description of output 2
    format: ["edam:format_2323", just a string]
    
label: This is a short description of the tool

description: |
 This is a much longer description of the tool. This can be displayed in
 the service detail panel. A Command Line Tool is a non-interactive executable 
 program that reads some input, performs a computation, and terminates after 
 producing some output. Command line programs are a flexible unit of code sharing
 and reuse, unfortunately the syntax and input/output semantics among command 
 line programs is extremely heterogeneous. A common layer for describing the 
 syntax and semantics of programs can reduce this incidental complexity by providing a consistent way to connect programs together.	 	 
 
basecommand: echo
