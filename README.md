StickyListHeaders
=================

StickyListHeaders is an Android library that makes it easy to integrate
section headers in your `ListView`. These section headers stick to the top like
in the new people app of Android 4.0 Ice Cream Sandwich. This behavior is also
found in lists with sections on iOS devices (If you just want section header
but not that they stick to the top, this feature can be turned off via xml).

Use classes in library project, It is compatible with versions of Android
down to 2.1. (It will probably work down to 1.6 but i have not tested it)
There is a test app in the downloads section which is just a compiled version
of the sample project.


Usage
-----

Instead of a normal `ListView` you will want to use `StickyListHeadersListView`,
It can be used just as you would a normal `ListView`. Your `ListAdapter` has
to implement `StickyListHeadersAdapter`, there are two superclasses that are
easy to use, one is a `BaseAdapter` subclass and one is a `CursorAdapter` subclass.

The `headerId` indicates when you want to switch header. In an alphabetical
list where the first letter is shown in the header you might return
the first characters integer value as the headerId for example.


License
-------

    Copyright 2012 Emil Sjölander

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
