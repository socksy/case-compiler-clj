# case
A compiler written in clojure for the JVM.


## Usage

    $ java -jar case-0.1.0-standalone.jar ./resources/simple.ast


### Implementation
	
####Abstract Syntax Tree
The abstract syntax tree was represented in clojure with the use of maps, from symbols as keywords to attributes --- which could include other nodes (hence making a tree.

This was wrapped with a creator in a "node" function, which meant that the tree could be represented in s-expressions. These went a bit like this:

		(node :add (node :int 2) (node :int 2))

The second argument is the :type symbol, and any other arguments are added to the list of values stored under :args.

This format was chosen due to being easy for a human to read, easy for a computer to read (they're s-expressions). Also, this would allow support for different data constructor to the type name - so I could have a (node :either :left a), for example.

#####Annotations
The tree was traversed with the aid of a functional zipper. As it went through the tree, it would run a multimethod against each node to see how to append a :code value, which would generally require knowledge of the below nodes. The final :code annotation at the top of the tree was used inbetween some runner jasmin code.

####Compilations
The code is compiled with shell access to jasmin (which needs to be installed and in the path for the program to run). Any data types were boxed 

#Resources
http://www.ibm.com/developerworks/library/j-treevisit/

