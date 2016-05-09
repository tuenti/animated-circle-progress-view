AnimatedCircleProgressView
==========================
Progress view that animates its state changes.

Usage
-----
Add it to your layout and customize its colors:

```xml
<com.tuenti.widget.AnimatedCircleProgressView
	android:id="@+id/progress"
	android:layout_width="200dp"
	android:layout_height="200dp"
	app:progress_stroke_width="12dp"
	app:progress_pending_color="#6000FF00"
	app:progress_first_phase_color="#FFFF0000"
	app:progress_second_phase_color="##FF00FF00"/>
```

Then simply change its progress:

```java
mCircledAnimatedProgressView = (AnimatedCircleProgressView) findViewById(R.id.progress);
mCircledAnimatedProgressView.setProgress(0.2f);
```

By default the view is in indeterminate state, you could set it again by calling:

```java
mCircledAnimatedProgressView.setIndeterminate();
```

Demo
----

![Sample gif][1]

Download
--------

Download via Maven:
```xml
<dependency>
  <groupId>com.tuenti.widget</groupId>
  <artifactId>animated-circle-progress-view</artifactId>
  <version>1.0.0</version>
</dependency>
```
or Gradle:
```groovy
compile 'com.tuenti.widget:animated-circle-progress-view:1.0.0'
```

License
-------

    Copyright 2015 Tuenti Technologies S.L.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

[1]: ./media/sample.gif