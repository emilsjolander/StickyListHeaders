StickyListHeaders
=================
StickyListHeaders is an Android library that makes it easy to integrate section headers in your `ListView`. These section headers stick to the top like in the new People app of Android 4.0 Ice Cream Sandwich. This behavior is also found in lists with sections on iOS devices. This library can also be used without the sticky functionality if you just want section headers.

StickyListHeaders actively supports android versions 2.3 (gingerbread) and above.
That said, it works all the way down to 2.1 but is not actively tested or working perfectly.

Here is a short gif showing the functionality you get with this library:

![alt text](https://github.com/emilsjolander/StickyListHeaders/raw/master/demo.gif "Demo gif")


Goal
----
The goal of this project is to deliver a high performance replacement to `ListView`. You should with minimal effort and time be able to add section headers to a list. This should be done via a simple to use API without any special features. This library will always priorities general use cases over special ones. This means that the library will add very few public methods to the standard `ListView` and will not try to work for every use case. While I will want to support even narrow use cases I will not do so if it compromises the API or any other feature.


Installing
---------------
###Gradle
Add the following gradle dependency exchanging `x.x.x` for the latest release.
```groovy
dependencies {
    compile 'se.emilsjolander:stickylistheaders:x.x.x'
}
```

###Cloning
First of all you will have to clone the library.
```shell
git clone https://github.com/emilsjolander/StickyListHeaders.git
```

Now that you have the library you will have to import it into Android Studio.
In Android Studio navigate the menus like this.
```
File -> Import Project ...
```
In the following dialog navigate to StickyListHeaders which you cloned to your computer in the previous steps and select the `build.gradle`.

Getting Started
---------------
Ok lets start with your activities or fragments xml file. It might look something like this.
```xml
<se.emilsjolander.stickylistheaders.StickyListHeadersListView
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

    @Override 
    public View getView(int position, View convertView, ViewGroup parent) {
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

    @Override 
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
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
        TextView text;
    }

    class ViewHolder {
        TextView text;
    }
    
}
```

That's it! Look through the API docs below to get know about things to customize and if you have any problems getting started please open an issue as it probably means the getting started guide need some improvement!

Upgrading from 1.x versions
---------------------------
First of all the package name has changed from `com.emilsjolander.components.stickylistheaders` -> `se.emilsjolander.stickylistheaders` so update all your imports and xml files using StickyListHeaders!

If you are Upgrading from a version prior to 2.x you might run into the following problems.
1. `StickyListHeadersListView` is no longer a `ListView` subclass. This means that it cannot be passed into a method expecting a ListView. You can retrieve an instance of the `ListView` via `getWrappedList()` but use this with caution as things will probably break if you start setting things directly on that list.
2. Because `StickyListHeadersListView` is no longer a `ListView` it does not support all the methods. I have implemented delegate methods for all the usual methods and gladly accept pull requests for more.

API
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
public boolean areHeadersSticky();
```

A `OnHeaderClickListener` is the header version of OnItemClickListener. This is the setter for it and the interface of the listener. The currentlySticky boolean flag indicated if the header that was clicked was sticking to the top at the time it was clicked.
```java
public void setOnHeaderClickListener(OnHeaderClickListener listener);

public interface OnHeaderClickListener {
    public void onHeaderClick(StickyListHeadersListView l, View header, int itemPosition, long headerId, boolean currentlySticky);
}
```

A `OnStickyHeaderOffsetChangedListener` is a Listener used for listening to when the sticky header slides out of the screen. The offset parameter will slowly grow to be the same size as the headers height. Use the listeners callback to transform the header in any way you see fit, the standard android contacts app dims the text for example.
```java
public void setOnStickyHeaderOffsetChangedListener(OnStickyHeaderOffsetChangedListener listener);

public interface OnStickyHeaderOffsetChangedListener {
    public void onStickyHeaderOffsetChanged(StickyListHeadersListView l, View header, int offset);
}
```

A `OnStickyHeaderChangedListener` listens for changes to the header.  This enables UI elements elsewhere to react to the current header (e.g. if each header is a date, then the rest of the UI can update when you scroll to a new date).
```java
public void setOnStickyHeaderChangedListener(OnStickyHeaderChangedListener listener);

public interface OnStickyHeaderChangedListener {
    void onStickyHeaderChanged(StickyListHeadersListView l, View header, int itemPosition, long headerId);
}
```

Here are two methods added to the API for inspecting the children of the underlying `ListView`. I could not override the normal `getChildAt()` and `getChildCount()` methods as that would mess up the underlying measurement system of the `FrameLayout` wrapping the `ListView`.
```java
public View getListChildAt(int index);
public int getListChildCount();
```

This is a setter and getter for an internal attribute that controls if the list should be drawn under the stuck header. The default value is true. If you do not want to see the list scroll under your header you will want to set this attribute to `false`.
```java
public void setDrawingListUnderStickyHeader(boolean drawingListUnderStickyHeader);
public boolean isDrawingListUnderStickyHeader();
```

Contributing
------------
Contributions are very welcome. Now that this library has grown in popularity i have a hard time keeping upp with all the issues while tending to a multitude of other projects as well as school. So if you find a bug in the library or want a feature and think you can fix it yourself, fork + pull request and i will greatly appreciate it!

I love getting pull requests for new features as well as bugs. However, when it comes to new features please also explain the use case and way you think the library should include it. If you don't want to start coding a feature without knowing if the feature will have chance of being included, open an issue and we can discuss the feature!
