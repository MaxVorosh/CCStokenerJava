# CCStokenerJava

## About

This repository contains CCStokener implementation, written on Java.  

## Build and Run

Working with Java 21 and Gradle 8.5. Previous versions might not be supported.

If you want simply run this programm, you can use [build_run](./build_run.sh) script like this

```
sh build_run.sh <path_to_src_directory>
```

If you want to tune java running arguments, you can use gradle commands.

## Test

You can run tests with [build_test](./build_test.sh) script like this

```
sh build_test.sh
```

If you want, you can use gradle commands instead.

Also, [test directory](./src/test/testResults/) contains example of intermediate files format and result file example.

## Current results

Right now results are far behind original tool. In particular, because of hidden n-gram hyperparameter.

| Tool | T1 | T2 | VST3 | ST3 | MT3 | WT3/T4 |
|------|----|----|------|-----|-----|--------|
|CCStokener| 0.76 | 0.98 | 0.98 | 0.83 | 0.5 | 0.02
|CCStokenerJava| 0.51 | 0.76 | 0.63 | 0.16 | 0.01 | 6e-5

## Working with BCB
This tool has similar to original CCStokener output format. It generates clonepairs.txt file with all found clone pairs. But this format is incompatible with Big Clone Bench. To fix it, there is [fix_report](./report_fix.py) python script. 

```
python report_fix.py <path to dir with clonepairs.txt>
```

Note, that it accepts path to dir as a parameter, not to clonepairs.txt

This script will generate fix_clonepairs.csv file, that is compatible with BCB.

In future, this python script will be incorporated in the main programm.

## Links
[CCStokener article](https://www.sciencedirect.com/science/article/abs/pii/S0164121223000134)

[CCStokener original implementation](https://github.com/CCStokener/CCStokener)

[Big Clone Bench](https://github.com/clonebench/BigCloneBench)