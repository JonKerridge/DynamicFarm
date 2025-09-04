### Introduction

The software is used to build Farmer/Worker style computing networks.
The network is defined by a DSL (Domain Specific Language) that uses a
command line interface based on picocli (https://picocli.info/).
The user has to supply at least two classes;
one that defines the data to be processed and
the other that defines what happens to the final results of processing.
Additionally, a further two classes can be defined which
define; source data than can be read in as source data for the application and
data that can be incorporated into the processing stage of the application.

The library has been compiled using Groovy 3 and Java 11.

The goal of the library is to create a parallel processing structure which takes
an existing sequential application code and enables its parallelisation
with a minimum of effort on behalf of the programmer.  The resulting parallel
architecture comprises a Farmer node together with any number of Worker nodes.  
The classes the user has to specify all
use an Interface definition to ensure correct interaction with the framework.

The Farmer node is responsible for sending data to be processed to any of the Worker nodes.  The Farmer
is then responsible for collating the processed data.  The Worker nodes can be added dynamically as 
resources become available.  The code that is run in Worker nodes is **NOT** application specific and 
can be used to run any application.  The required class files are sent from the Farmer to a 
Worker node on an as needed basis.

The following is the DSL for a simple system with no source data or worker data requirements.

    version 1.0.0
    data -p int,int!0,100000
    work -m updateMethod -p int!500
    result -p String!Test1Results

The *version* command specifies the software version in use.  This ensures that the software version 
used is  the same  on the Farmer and Worker nodes and that the DSL specification was processed
using the same Parser version.  

The *data* command specifies any parameters required by the data class
constructor.  The parameter specification follows the **-p** option and comprises two comma separated strings
separated by an **!**.  The first string specifies the parameter type and the second the corresponding values.

The *work* command specifies the name of the work method after the -m option, together with any required parameters.
These parameter values will be passed to all Worker in all the Worker nodes.

The *result* command specifies any parameters to be passed to the constructor of the class
that collates the resulting processed data.  In this case the parameter is the name of a file to 
which the output will be printed.

All parameter values are passed as a single *List* so that any number of parameters of different types can be passed. The
types of parameters are limited to: int, float, String, double, long, and boolean.

The following is a DSL specification for an application that has both Source and Work data requirements.

    version 1.0.0
    data -f ./data/areas1000.loc
    work -m distance -p double!3.0 -f ./data/pois250.loc
    result -p String!./data/Test1Results

In this case the *data* command specifies the name of a file which contains all the data to be processed, 
following the -f option.  The *work* command specifies, in addition to the name of the work method, the 
parameter values to be passed to the work method and the name of a file which contains data to be
used in the processing of incoming data.  It is assumed that each item of data will need to access some of the
work data.  The work data will be read into each work node and then shared between each of the workers in the node.
Such work data can only be read.

The project structure assumes the existence of a **data** folder/directory at the top level of the project.

### Interfaces

There are four interfaces the user needs to be aware of: *EmitInterface*, *CollectInterface*, *SourceDataInterface*, and the
*WorkDataInterface*.  The following is a brief overview, the interface documentation gives more detail.

#### Data Interface

    interface DataInterface <T> extends Serializable {
    T create(Object sourceData)
    }

The Farmer creates objects that implement the *DataInterface* where *T* is the class name of the emitted object.
The *sourceData* parameter will be passed as *null* if no source data file is used.

#### Result Interface

    interface ResultInterface <T> {
    void collate (T data, List params)
    void finalise(List params)
    }

The *ResultInterface* is used by the Farmer and processes incoming processed data of type *T* which is the class that
is emitted from the Farmer.  The *collate* method is used to process incoming data objects to
create some local transitory collation of the results.  The *finalise* method is used to present the
collated transitory results directly to the user in terminal output.  The DSL -p parameter specifies
the parameters that are to be passed to the class constructor.
In all cases the *List params* is obtained from the DSL specification.

#### SourceDataInterface

    interface SourceDataInterface <T> {
      T getSourceData()
    }

The Farmer process will call the method *getSourceData* when required.  The method returns *null* when
all the data have been obtained from the associated data source.  The returned object will be used in the emitted
object's *create* method, see Data Interface above. The method is defined in the object that implements
the *SourceDataInterface*.  This class will have a constructor that is used to read the
source data file specified in the DSL specification.


#### WorkDataInterface

    interface WorkDataInterface <T> {
    T getFilteredWorkData(int index, List filterValues)
    int getWorkDataSize()
    }

A worker node reads a file of data that can be accessed for reading only by all the worker processes in that node as an iterable collection.
The number of elements in the collection can be obtained using the method *getWorkDataSize*
The constructor contained within a class implementing this interface will be passed the required file name.
The type *T* is the type of the data that will be returned by the method *getFilteredWorkData* starting at the specified subscript *index*.
Items not satisfied by the *filterValues* will be skipped until a file record is found that satisfies the filter values or the end of data is found.
It is up to the user to decide where and if checking of the iterator bound values is undertaken. The method *getFilteredWorkData*
may return *null* to indicate the end of the work data collection if required.

### Using the Framework

The framework has been designed so that the accuracy of the application can be checked by running the application classes in
a sequential format.  The application can then be tested in parallel using a multicore PC using a loop-back IP network using
addresses of the form 127.0.0.x.  Finally, the architecture can be run on a network of PCs or an HPC system, without
change to the application code, but by simply the manner in which the code is run.  On a PC cluster by using *Remote Desktop* and
on an HPC by using *SLURM* batch files.  To aid in this *jar* files are provided to run a Farmer node and the other worker nodes.

### Parsing a DSL file

A parser is provided that takes a DSL specification, with the extension *.df*, and transforms it into
an object file with the extension *.dfstruct*. This can be input into a host node from which the rest
of the application architecture is created, with no user intervention.  The resulting application process
structure has a formal proof of correctness, meaning that no errors will be introduced as a result of the
parallelisation of the application.

### DSL options

A typical DSL specification comprises a number of lines that appear in a specific order as shown below.

    version 1.0.0
    data -file ./data/sourceDataFile  -p int!-1
    work -method distance -p double!3.0 -f ./data/workDataFile
    result -p String!./data/X4Results

The first line is always the *version* specification.  This must match the version of the software being used.
The second line always specifies the data used in the Farmer to create the data to be processed.
The third line specifies a work node.
The last line always specifies the result processing carried out in the Farmer process.

Some of the specification options are common to all the specifications.
They are all introduced  with a - (minus) character and take the form used in a command line interface.

f(ile) specifies a file name, several file names may be specified separated by a comma

m(ethod) specifies the name of a method to be used in a work cluster

p specifies a set of parameter values.  These take the form of a string of values separated by !
>The first string comprises a comma separated string of parameter types (int, float, double, long, String, boolean), one per required parameter value.
>Subsequent strings comprise the values to be passed as parameters.  The n'th value must correspond in type to the n'th parameter type
>The use of parameters varies depending on the cluster being specified.

cp specifies any parameters for the *collate* method in the *ResultInterface*

fp specifies any parameters for the *finalise* method in the *ResultInterface*


#### emit specification

-p used to specify parameters for the emit class constructor.  There must be *nodes* x *workers* parameter value strings.

-f used to specify file names that hold source data files. There must be *nodes* x *workers* filenames

#### work specification

-m the name of the method to be used in this cluster

-p a parameter string which has the type string and ONLY one set of parameter values, passed to the work method,
all the workers in a Work node have the same parameter values

-f a single file name giving the name of the file holding the work data, all nodes access the same file

#### collect specification

-p a parameter string which has the type string and **ONE** set of parameter 
values passed to the class constructor.

-cp a parameter string which has the type string and **ONE** set of parameter 
values passed to the *collate* method.

### Initial Sequential Testing

The classes used in the application can be executed in a sequential manner to ensure they
undertake the required processing correctly.

An example of this can be found in */test/groovy/areaPoiTests/Sequential/RunSeq* where the  code
 invokes the classes in the same order as the multi-node version.  The sequential codes 
iterates through all the source data, processing it and then collecting it as a sequence of operations.
The multi-node version still iterates through the source data in sequence but each worker process in a
Work node will be processing source data items at the same time.

### Testing on a single multicore PC workstation

#### The Farmer host

In this case the test takes place in a single PC using the TCP/IP loopback mechanism.  The Ramer is assumed
to run on IP 127.0.0.1 and the work nodes to run on addresses 127.0.0.n, where n > 1.  An example of this
can be found in */test/groovy/areaPoiTests/testFiles/T1Farmer*.

    class T1Farmer {
        static void main(String[] args) {
            String structure =  "./src/test/groovy/areaPoiTests/testFiles/Test1"
            Class dataClass = AreaData
            Class sourceDataClass = AreaLocales
            Class workDataClass = PoILocales
            Class resultClass = AreaPoICollect
            new Farmer( structure, dataClass, sourceDataClass, workDataClass,
                resultClass,"Local", false ).invoke()
        }
    }

The *structure* property specifies the DSL specification to be used.
The *dataClass* property specifies the data class to be used.
The *sourceDataClass* property specifies the class used for loading any 
source data or *null* if none is required.
The *workDataClass* property specifies the class used for loading any
work data into worker nodes or *null* if none is required.
The *resultClass* property specifies the result class to be used.

A check is made during system loading to check that any files required to load data into either the
*sourceDataClass* or *workDataClass* is defined in the DSL specification.

The value *"Local"* signifies that the host will run on IP 127.0.0.1

If the boolean value *false* is replaced by *true* more verbose output will be generated to help solve any 
processing problems.

#### A Work node

Example node invocations are shown in */test/groovy/areaPoiTests/invokeNodes*.

    class Node2 {
        static void main(String[] args) {
            new WorkNode("127.0.0.1", 2, "127.0.0.2", false).invoke()
        }
    }

The class WorkNode requires four parameters.

The first is the IP-address of the Farmer node
The second specifies the number of worker processes to be created at the node.  When running in 
a real network using separate PCs for each node this can be passed as 0 (zero), which will cause the
node to determine the total number of available cores and allocate that number minus 1 worker processes.  
The remaining core will be used to implement input and output buffers in the node.
The third parameter is the IP-address of the node being created, which would be omitted when 
running on a real network.  
The final parameter , if *true* would create more output during system initialisation
to help with solving any communication problems.

### Running on a network

The package */test/groovy/areaPoiTests/Jars* contains examples of the jars that are required
to run the application on a real network.

The *WorkNodeJar* is application independent and can be used repeatedly.  It just requires the
IP-address of the host as a program parameter.  The Farmer will print its IP-address when 
it is first executed.  This version of the jar will use as many cores as is possible on a work node.

The *FarmerJar* is application dependent, as reference is required to the classes used in the application.

### Further examples

The repo https://github.com/JonKerridge/DynamicFarmExperiments has more realistic 
examples of the use of the framework.