Notice
=================

I have recently come to the understanding that the latest re-write of the library will not hold in the long run (mostly because of the need for translating positions). While the new version comes with a bunch of new features it makes it harder for me to keep bug free. So i will be trying to figure out a way to mix the benifits of v1 and the current state of the library to get the best of both worlds.

So i apologize in advance for any inconvenience this may cause (I will not be fixing any bugs for the uppcoming week/weeks when working on this)


StickyListHeaders
=================

StickyListHeaders is an Android library that makes it easy to integrate
section headers in your `ListView`. These section headers stick to the top like
in the new People app of Android 4.0 Ice Cream Sandwich. This behavior is also
found in lists with sections on iOS devices (if you just want section header
but not that they stick to the top, this feature can be turned off via a call to setAreHeadersSticky(false)).

Use classes in library project, it is compatible with versions of Android
down to 2.1 (it will probably work down to 1.6 but i have not tested it).
There is a test app in the downloads section which is just a compiled version
of the sample project.


Usage
-----

Instead of a normal `ListView` you will want to use `StickyListHeadersListView`,
it can be used just as you would a normal `ListView`. To do so, your `ListAdapter`
has to implement the `StickyListHeadersAdapter` interface.

The `headerId` indicates when you want to switch header. In an alphabetical
list where the first letter is shown in the header you might return
the first characters integer value as the `headerId` for example.

If you want to recieve callback for clicks on the header, 
call `setOnHeaderClickListener()` on `StickyListHeadersListView`.


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
