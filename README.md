# case
A compiler written in clojure for the JVM.


## Usage

If leiningen is installed, then it is preferable to open a repl in that using `lein repl'.

In this situation, code can be run using (case.core/run filename ast), where ast is an abstract syntax tree (described below) and filename is the location where the file will be 

However, failing that:

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
The code is compiled with shell access to jasmin (which needs to be installed and in the path for the program to run). Any data types were boxed using a java class "Type". This was tagged with both a constructor and data tag, which would allow opening for extending to pattern matching and ADTs. Some helper functions to create primitive types in a boxed form were in NewTypes.java.

After the code is compiled in jasmin, I took advantage of the fact that Clojure is on the JVM --- as such, it is possible to load the class files produced by Jasmin and run them, unboxing the values to compare against preset tests. This is done in tests/case/core\_text.clj.

The output from these tests has been outputted to tests_output.txt, and can be regenerated using `lein test'.

###Not done
Abstract data types and pattern matching on them --- but the structure is all in place for them to be used. Certainly, the hard work in boxing and unboxing values (and debugging accordingly) has been taken place, so whilst only mathematical and boolean operators really work properly, the others are supported inprinciple. 

Accordingly, there are internal data structures to the compiler mapping the different constructors to different types (and vice versa). The boolean type effectively works like this, by assigning a different constructor tag to true and false.

To do pattern matching, the match function written on the Type class is used to see if there's a match with the provided expression (which would have to return a boxed type).

#Resources
http://www.ibm.com/developerworks/library/j-treevisit/

#Help
Much discussion was had with classmates Martin and Conrad, specifically in looking at ways to evaluate the specification, and to bounce off various ideas in how to use the JVM.
