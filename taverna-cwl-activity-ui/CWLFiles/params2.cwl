class: CommandLineTool
cwlVersion: cwl:draft-3
requirements:
  - class: InlineJavascriptRequirement

inputs: {"$import": params_inc.yml}

outputs: {"$import": params_inc.yml}

baseCommand: "true"
