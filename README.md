# Padraig #

PADRAIG (Precise AnDRoid Automated Input Generation) is a Java command line application that generates a test 
consisting of a sequence of inputs for an Android application. The application under test is launched on an 
Android emulator and one of three supported generation methods is used to apply test inputs: 

* **Random**: Inputs are chosen randomly, without repetition, from a set of available inputs, i.e. a random input is 
  chosen until an unused input is selected. Once chosen the input is added to the generated test sequence.
* **Systematic**: Inputs are chosen systematically without repetition, i.e. the next available input is chosen assuming 
  it has not already been used. Once chosen the input is added to the generated test sequence.
* **Model**: An extended control flow model created by [Droid Graph](https://github.com/jordan2doyle1/DroidGraph) is used 
  to determine the potential coverage gain provided by an input. Inputs with the highest coverage gain are chosen 
  and added to the generated test sequence.

### Dependencies ###

The model-based approach used by PADRAIG relies on the extended control flow model created by [Droid Graph](https://github.com/jordan2doyle1/DroidGraph) 
as well as the API it provides to query and traverse the model. Clone the [Droid Graph](https://github.com/jordan2doyle1/DroidGraph) 
repository and install the dependency using the maven install command below:

```
$ cd DroidGraph
$ maven install 
```

### Build & Run ###

This is a Maven project developed in JetBrains Intellij IDE. You can clone this project and open it in JetBrains 
Intellij IDE as a maven project, or you can clone the project and build a JAR file using the maven package command 
below:

```
$ cd Padraig
$ mvn package
```

The maven package command will build a JAR file with dependencies included. Run the project using the JAR file 
and the sample input files using the following commands:

```
$ cd target
$ java -jar Padraig-1.0-SNAPSHOT-jar-with-dependencies.jar -a "samples/activity_lifecycle_1.apk" -i "samples/activity_lifecycle_1.gml" -l "samples/app_control_flow_graph.json" -z "samples/flow_droid_callbacks"
```
