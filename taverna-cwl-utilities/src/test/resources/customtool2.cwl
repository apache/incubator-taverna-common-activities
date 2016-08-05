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
cwlVersion: v1.0
class: CommandLineTool

$namespaces:
 edam: http://edamontology.org/
 edam2: http://edamontologytest.org/

inputs:
  input_1:
    type: array

  input_2:
    type: boolean[]
    label: input 1 testing label cwl v1.0
    description: this is a short description of input 1 cwl v1.0
    format: edam:format_2323

  input_3: int
   
  input_4: string[]

  input_5: 
    type: ["null",double]
outputs: 
 output_1:
    type: int?
 
baseCommand: cat
