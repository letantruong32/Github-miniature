# Gitlet Design Document

**Name**: Truong Le


## Classes and Data Structures


### Commit Class
#### Instance Variables
* Message   --> contains the message of a commit. 
* Timestamp --> time at which the commit was created. Assigned by the constructor.
* Parent    --> the parent commit of commit object.
* Parent2   --> for merge
* commitID  --> the ID of the commit using SHA-1
* blobPtrs  --> the contents of files currently pointing to



### Stage Class
#### Instance Variables







## Algorithms
Git is a content-addressable filesystem. It means that at the core of Git 
is a simple key-value data store. What this means is that you can insert 
any kind of content into a Git repository, for which Git will hand you back
a unique key you can use later to retrieve that content.


### Commit Class
* HashMap<String, String> blobs: Save the state of the blobs and populate 
the forward and backward HashMaps to contain the blobs ID using SHA-1
* SHA-1 HashCode provided in Utils.java: Use all contents of a file to get
its commit ID. 


### Stage Class
* SHA-1 HashCode provided in Utils.java: Use all contents of a file to get
  its commit ID. 





## Persistence
In order to persist the settings of the machine, we will need to save the 
state of the rotors after each call to the enigma machine. To do this,

* Write the Commit HashMaps to disk. We can serialize them into bytes 
that we can eventually write to a specially named file on disk. This can be 
done with writeObject method from the Utils class.

* Write all the Blob objects to disk. We can serialize the Blob objects and
write them to files on disk (for example, “Blob1” file, “Blob2” file, etc.).
This can be done with the writeObject method from the Utils class. We will make
sure that our Rotor class implements the Serializable interface.


In order to retrieve our state, before executing any code, we need to search for
the saved files in the working directory (folder in which our program exists) and
load the objects that we saved in them. Since we set on a file naming convention
(“Blob 0”, etc.) our program always knows which files it should look for.
We can use the readObject method from the Utils class to read the data of files
as and deserialize the objects we previously wrote to these files.


