# Spider
Basic goal is to be able to take a jar file and break it down to the classes' main components for simple bug-fixing purposes when you don't have access to the source code.
Allows you to setup "fake" environments with all of the jar's classes having unsupported implementations so you can write your own implementations for specific classes without having to worry about buggy decompilers not producing compilable code or messing something up in another class. With the newly compiled code you can drag and drop your new class into the jar archive.