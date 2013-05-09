StickyListHeaders
=================
StickyListHeaders is an Android library that makes it easy to integrate section headers in your `ListView`. These section headers stick to the top like in the new People app of Android 4.0 Ice Cream Sandwich. This behavior is also found in lists with sections on iOS devices. This library can also be used for without the sticky functionality if you just want section headers.

StickyListHeaders actively supports android versions 2.3 (gingerbread) and above
That said, it should be compatible with much older versions of android as well but these are not actively tested.

Here is a short gif showing the functionality you get with this library:

![alt text](https://github.com/emilsjolander/StickyListHeaders/raw/master/demo.gif "Demo gif")


Goal
----
The goal of this project is to deliver a high performance replacement to `ListView`. You should with minimal effort and time be able to add section headers to a list. This should be done via a simple to use api without any special features. This library will always priorities general use cases over special ones. This means that the library will add very few public methods to the standard `ListView` and will not try to work for every use case. While i will want to support even narrow use cases i will not do se if it comprimises the api or any other feature.


Getting started
---------------
###Installing the library
First of all you will have to clone the library.

If you are using a unix-like terminal first use the following command to navigate to the directory or you choosing.
```shell
cd ~/my/directory
```
After you are in the directory you want to clone the library to, use this command to clone StickyListHeaders.
```shell
git clone https://github.com/emilsjolander/StickyListHeaders.git
```

Now that you have the library you will have to import it into eclipse (or any other ide but this is how you do it in eclipse).
Inn eclipse navigate the menus like this.
```
file -> new -> project -> android project from existing source
```
In the following dialog navigate to StickyListHeaders which you cloned to your computer in the previous steps.
Press finish and you should have the library in your workspace.

Now right click the project you want to use StickyListHeaders in and click on `properties`. In the dialog that apears you should navigate to `android` in the side menu. Now press the ass button on the bottom right of the dialog and choose StickyListHeaders. Press ok and you are good to go!


###Code
Ok lets start with your activities or fragments xml file. It might look something like this.
```xml
<?xml version="1.0" encoding="utf-8"?>
<com.emilsjolander.components.stickylistheaders.StickyListHeadersListView
    android:id="@+id/list"
    android:layout_width="match_parent"
    android:layout_height="match_parent"/>
```

Now in your activities `onCreate()` or your fragments `onCreateView()` you would want to do something like this
```java
StickyListHeadersListView stickyList = (StickyListHeadersListView) findViewById(R.id.list);
MyAdapter adapter = new MyAdapter(this);
stickyList.setAdapter(adapter);
```

`MyAdapter` in the above example would look something like this if your list was a list of countries where each header was for a letter in the alphabet.
```java
public class MyAdapter extends BaseAdapter implements StickyListHeadersAdapter {

    private String[] countries;
    private LayoutInflater inflater;

    public TestBaseAdapter(Context context) {
        inflater = LayoutInflater.from(context);
        countries = context.getResources().getStringArray(R.array.countries);
    }

    @Override
    public int getCount() {
        return countries.length;
    }

    @Override
    public Object getItem(int position) {
        return countries[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.test_list_item_layout, parent, false);
            holder.text = (TextView) convertView.findViewById(R.id.text);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.text.setText(countries[position]);

        return convertView;
    }

    @Override public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;
        if (convertView == null) {
            holder = new HeaderViewHolder();
            convertView = inflater.inflate(R.layout.header, parent, false);
            holder.text = (TextView) convertView.findViewById(R.id.text);
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }
        //set header text as first char in name
        String headerText = "" + countries[position].subSequence(0, 1).charAt(0);
        holder.text.setText(headerText);
        return convertView;
    }

    @Override
    public long getHeaderId(int position) {
        //return the first character of the country as ID because this is what headers are based upon
        return countries[position].subSequence(0, 1).charAt(0);
    }

    class HeaderViewHolder {
        TextView text1;
    }

    class ViewHolder {
        TextView text;
    }
    
}
```

Thats it! look through the api docs below to get know about things to customize and if you have any problems getting started please open an issue as it probably means the getting started guide need some improvement!


Api
---
###StickyListHeadersAdapter
```java
public interface StickyListHeadersAdapter extends ListAdapter {
    View getHeaderView(int position, View convertView, ViewGroup parent);
    long getHeaderId(int position);
}
```
Your adapter must implement this interface to function with `StickyListHeadersListView`.
`getHeaderId()` must return a unique integer for every section. A valid implementation for a list with alphabetical sections is the return the char value of the section that `position` is a part of.

`getHeaderView()` works exactly like `getView()` in a regular `ListAdapter`.


###StickyListHeadersListView
Headers are sticky by default but that can easily be changed with this setter. There is of course also a matching getter for the sticky property.
```java
public void setAreHeadersSticky(boolean areHeadersSticky);
public boolean getAreHeadersSticky();
```

A OnHeaderClickListener is the header version of OnItemClickListener. This is the setter for it and the interface of the listener. The currentlySticky boolean flag indicated if the header that was clicked was sticking to the top at the time it was clicked.
```java
public void setOnHeaderClickListener(OnHeaderClickListener onHeaderClickListener);

public interface OnHeaderClickListener {
    public void onHeaderClick(StickyListHeadersListView l, View header, int itemPosition, long headerId, boolean currentlySticky);
}
```

StickyListHeaders wraps the adapter passed to `setAdapter()` is it's own adapter, so `getAdapter()` will not return the adapter that `setAdapter()` was passed. It is often recomended that you keep a reference to the adapter in your activity/fragment but if this does not fit you there is a method to retrieve the original adapter.
```java
public StickyListHeadersAdapter getWrappedAdapter();
```

This is a setter and getter for an internal attribute that controlls if the list should be drawn under the stuck header. The default value is false. If you want to see the list scroll under your header(the header should have a semi-transparent background) you will want to set this attribute to `true`.
```java
public void setDrawingListUnderStickyHeader(boolean drawingListUnderStickyHeader);
public boolean isDrawingListUnderStickyHeader();
```


Limitations
-----------
There is currently two limitations with this library, they both have to do with what kind of views the header can contain and the both only apply for when sticky header are activated.

The first limitation is that the header can as of now not contain anything that animates, the list will not crash but the animation will just not run as expected while the header is stuck. The other limitation is that it is currently not possible to have interactive elements in the header, Buttons, switches, etc. will only work when the header is not stuck.


Contributing
------------
Contributions are very welcome. Now that this library has grown in popularity i have a hard time keeping upp with all the issues while tending to a multitude of other projects as well as school. So if you find a big in the library or want a feature and think you can fix it yourself, fork + pull request and i will greatly appreciate it!

I love getting pull requests for new features as well as bugs. However, when it comes to new features please also explain the use case and way you think the library should include it. If you don't want to start coding a feature without knowing if the feature will have chance of being included, open an issue and we can discuss the feature!


License
-------

    Copyright 2013 Emil Sjölander

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.